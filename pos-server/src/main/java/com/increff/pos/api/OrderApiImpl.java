package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderApiImpl {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, OrderStatus> add(OrderPojo orderPojo) throws ApiException {

        createOrderItemIds(orderPojo);
        Map<String, OrderStatus> statuses = new HashMap<>();

        Boolean validationFailure = false;

        for (OrderItem item : orderPojo.getOrderItems()) {
            try {
                validateItem(item); // barcode + price only
            } catch (ApiException e) {
                OrderStatus status = new OrderStatus();
                status.setOrderItemId(item.getOrderItemId());
                status.setStatus("INVALID INPUT");
                status.setMessage(e.getMessage());
                statuses.put(item.getOrderItemId(), status);
                validationFailure = true;
            }

        }

        if (validationFailure) return statuses;

        // 2. INVENTORY CHECK (soft validation)
        boolean allFulfillable = true;

        for (OrderItem item : orderPojo.getOrderItems()) {
            int available = inventoryDao.getQuantity(item.getBarcode());
            int required = item.getOrderedQuantity();

            OrderStatus status = new OrderStatus();
            status.setOrderItemId(item.getOrderItemId());

            if (available < required) {
                item.setOrderItemStatus("UNFULFILLABLE");
                status.setStatus("UNFULFILLABLE");
                status.setMessage("Insufficient inventory");

                allFulfillable = false;
            } else {
                item.setOrderItemStatus("FULFILLABLE");
                status.setStatus("FULFILLABLE");
                status.setMessage("OK");
            }

            statuses.put(item.getOrderItemId(), status);
        }

        // 3. Set order-level status
        orderPojo.setOrderStatus(
                allFulfillable ? "FULFILLABLE" : "UNFULFILLABLE"
        );

        // 4. Persist order ALWAYS if we reached here
        orderDao.save(orderPojo);

        // 5. Update inventory only if fully fulfillable
        if (allFulfillable) {
            for (OrderItem item : orderPojo.getOrderItems()) {
                applyInventoryUpdate(item);
            }
        }

        return statuses;
    }


    private OrderPojo createOrderItemIds(OrderPojo orderPojo) {
        for (OrderItem item : orderPojo.getOrderItems()) {
            item.setOrderItemId(UUID.randomUUID().toString());
        }
        return orderPojo;
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

        int available = inventoryDao.getQuantity(item.getBarcode());
        int required = item.getOrderedQuantity();

        inventoryDao.updateInventory(pojo);
    }

    public Page<OrderPojo> getAll(int page, int size) {
        logger.info("Fetching orders page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderDao.findAll(pageRequest);
    }
}
