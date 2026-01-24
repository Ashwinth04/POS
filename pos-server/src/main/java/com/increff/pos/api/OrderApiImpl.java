package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.storage.StorageService;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class OrderApiImpl implements OrderApi {
    private static final Logger logger = LoggerFactory.getLogger(OrderApiImpl.class);

    private final OrderDao orderDao;

    private final ProductDao productDao;

    private final InventoryDao inventoryDao;

    private final StorageService storageService;

    public OrderApiImpl(OrderDao orderDao, ProductDao productDao, InventoryDao inventoryDao, StorageService storageService) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.inventoryDao = inventoryDao;
        this.storageService = storageService;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, OrderStatus> createOrder(OrderPojo orderPojo) throws ApiException {

        createOrderItemIds(orderPojo);

        Map<String, OrderStatus> statuses = new LinkedHashMap<>();

        boolean hasValidationErrors = validateAllOrderItems(orderPojo, statuses);
        if (hasValidationErrors) return statuses;

        boolean allFulfillable = checkInventoryAndUpdateItemStatus(orderPojo, statuses);

        setOrderLevelStatus(orderPojo, allFulfillable);

        orderDao.save(orderPojo);

        updateInventoryIfOrderIsFulfillable(orderPojo, allFulfillable);

        return statuses;
    }

    public byte[] getInvoice(String orderId) throws ApiException {

        try {
            return storageService.readInvoice(orderId);
        } catch (IOException e) {
            throw new ApiException("Failed to fetch invoice for order: " + orderId);
        }
    }

    private boolean validateAllOrderItems(OrderPojo orderPojo, Map<String, OrderStatus> statuses) {

        boolean validationFailure = false;

        for (OrderItem item : orderPojo.getOrderItems()) {
            try {
                validateItem(item);
                addValidItemStatus(item, statuses);
            } catch (ApiException e) {
                addInvalidItemStatus(item, e.getMessage(), statuses);
                validationFailure = true;
            }
        }

        return validationFailure;
    }

    private void addValidItemStatus(OrderItem item, Map<String, OrderStatus> statuses) {

        OrderStatus status = new OrderStatus();
        status.setOrderItemId(item.getOrderItemId());
        status.setStatus("VALID");
        status.setMessage("OK");
        statuses.put(item.getOrderItemId(), status);
    }

    private void addInvalidItemStatus(OrderItem item, String errorMessage, Map<String, OrderStatus> statuses) {

        OrderStatus status = new OrderStatus();
        status.setOrderItemId(item.getOrderItemId());
        status.setStatus("INVALID");
        status.setMessage(errorMessage);
        statuses.put(item.getOrderItemId(), status);
    }

    private boolean checkInventoryAndUpdateItemStatus(OrderPojo orderPojo, Map<String, OrderStatus> statuses) throws ApiException {

        boolean allFulfillable = true;

        for (OrderItem item : orderPojo.getOrderItems()) {
            boolean fulfillable = isItemFulfillable(item);
            updateItemAndStatus(item, fulfillable, statuses);

            if (!fulfillable) allFulfillable = false;
        }

        return allFulfillable;
    }

    private boolean isItemFulfillable(OrderItem item) throws ApiException {

        int available = inventoryDao.getQuantity(item.getBarcode());
        int required = item.getOrderedQuantity();
        return available >= required;
    }

    private void updateItemAndStatus(OrderItem item, boolean fulfillable, Map<String, OrderStatus> statuses) {

        OrderStatus status = new OrderStatus();
        status.setOrderItemId(item.getOrderItemId());

        if (fulfillable) {
            item.setOrderItemStatus("FULFILLABLE");
            status.setStatus("FULFILLABLE");
            status.setMessage("OK");
        } else {
            item.setOrderItemStatus("UNFULFILLABLE");
            status.setStatus("UNFULFILLABLE");
            status.setMessage("Insufficient inventory");
        }

        statuses.put(item.getOrderItemId(), status);
    }

    private void setOrderLevelStatus(OrderPojo orderPojo, boolean allFulfillable) {
        orderPojo.setOrderStatus(
                allFulfillable ? "FULFILLABLE" : "UNFULFILLABLE"
        );
    }

    private void updateInventoryIfOrderIsFulfillable(OrderPojo orderPojo, boolean allFulfillable) throws ApiException{
        if (!allFulfillable) return;

        for (OrderItem item : orderPojo.getOrderItems()) {
            applyInventoryUpdate(item);
        }
    }

    private void createOrderItemIds(OrderPojo orderPojo) {
        for (OrderItem item : orderPojo.getOrderItems()) {
            item.setOrderItemId(UUID.randomUUID().toString());
        }
    }

    private void validateItem(OrderItem item) throws ApiException {
        String barcode = item.getBarcode();

        ProductPojo product = productDao.findByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Invalid barcode: " + barcode);
        }

        if (item.getSellingPrice() > product.getMrp() || item.getSellingPrice() <= 0) {
            throw new ApiException("Selling price exceeds MRP for barcode: " + barcode);
        }
    }

    private void applyInventoryUpdate(OrderItem item) throws ApiException {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setBarcode(item.getBarcode());
        pojo.setQuantity(-item.getOrderedQuantity());

        inventoryDao.updateInventory(pojo);
    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        logger.info("Fetching orders page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderDao.findAll(pageRequest);
    }
}
