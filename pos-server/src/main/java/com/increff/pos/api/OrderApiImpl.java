package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.storage.StorageService;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
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

    private final StorageService storageService;

    public OrderApiImpl(OrderDao orderDao, StorageService storageService) {
        this.orderDao = orderDao;
        this.storageService = storageService;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, OrderStatus> placeOrder(OrderPojo orderPojo, Map<String, OrderStatus> statuses, boolean isFulFillable) throws ApiException {

        orderPojo.setOrderStatus(isFulFillable ? "FULFILLABLE" : "UNFULFILLABLE");

        orderDao.save(orderPojo);

        return statuses;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, OrderStatus> editOrder(OrderPojo orderPojo, Map<String, OrderStatus> statuses, boolean isFulFillable) throws ApiException {

        orderPojo.setOrderStatus(isFulFillable ? "FULFILLABLE" : "UNFULFILLABLE");

        updateOrder(orderPojo);

        return statuses;
    }

    public void updateOrder(OrderPojo orderPojo) throws ApiException{

        OrderPojo record = orderDao.findByOrderId(orderPojo.getOrderId());

        if (record == null) throw new ApiException("Order with the given id does not exist");

        orderPojo.setId(record.getId());
        orderDao.save(orderPojo);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {
        OrderPojo record = orderDao.findByOrderId(orderId);

        if (record == null) throw new ApiException("Order with the given id does not exist");

        record.setOrderStatus("CANCELLED");
        orderDao.save(record);

        return new MessageData("Order cancelled successfully!");
    }

    public void checkOrderEditable(String orderId) throws ApiException {
        OrderPojo orderPojo = orderDao.findByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("Order with the given id doesnt exist");

        String status = orderPojo.getOrderStatus();

        if (status.equals("CANCELLED")) throw new ApiException("CANCELLED ORDERS CANNOT BE EDITED");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE EDITED");
    }

    public void checkOrderCancellable(String orderId) throws ApiException {

        OrderPojo orderPojo = orderDao.findByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("Order with the given id doesnt exist");

        String status = orderPojo.getOrderStatus();

        if (status.equals("CANCELLED")) throw new ApiException("ORDER CANCELLED ALREADY");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE CANCELLED");

    }

    public byte[] getInvoice(String orderId) throws ApiException {

        try {
            return storageService.readInvoice(orderId);
        } catch (IOException e) {
            throw new ApiException("Failed to fetch invoice for order: " + orderId);
        }

    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        logger.info("Fetching orders page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderDao.findAll(pageRequest);
    }

    public List<OrderPojo> getTodaysOrders() {
        return orderDao.findTodayFulfillableOrders();
    }

    public Map<String, Integer> aggregateItems(List<OrderItem> orderItems) {

        Map<String, Integer> aggregatedItems = new HashMap<>();

        for (OrderItem item : orderItems) {
            String barcode = item.getBarcode();
            Integer quantity = item.getOrderedQuantity();

            aggregatedItems.merge(barcode, quantity, Integer::sum);
        }

        return aggregatedItems;

    }

    public Map<String, Integer> aggregateItems(String orderId) {

        OrderPojo orderPojo = getOrderByOrderId(orderId);

        return aggregateItems(orderPojo.getOrderItems());
    }

    public OrderPojo getOrderByOrderId(String orderId) {
        return orderDao.findByOrderId(orderId);
    }

    public void updatePlacedStatus(String orderId) throws ApiException {
        OrderPojo orderPojo = getOrderByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        orderPojo.setOrderStatus("PLACED");
        orderDao.save(orderPojo);
    }
}