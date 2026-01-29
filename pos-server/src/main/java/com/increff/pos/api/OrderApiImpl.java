package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.storage.StorageService;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderStatus;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class OrderApiImpl implements OrderApi {
    private static final Logger logger = LoggerFactory.getLogger(OrderApiImpl.class);

    private final OrderDao orderDao;

    public OrderApiImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
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

    public String checkAndGetStatus(String orderId) throws ApiException {
        OrderPojo orderPojo = orderDao.findByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("Order with the given id doesnt exist");

        String status = orderPojo.getOrderStatus();

        if (status.equals("CANCELLED")) throw new ApiException("CANCELLED ORDERS CANNOT BE EDITED");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE EDITED");

        return status;
    }

    public void checkOrderCancellable(String orderId) throws ApiException {

        OrderPojo orderPojo = orderDao.findByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("Order with the given id doesnt exist");

        String status = orderPojo.getOrderStatus();

        if (status.equals("CANCELLED")) throw new ApiException("ORDER CANCELLED ALREADY");

        if (status.equals("PLACED")) throw new ApiException("PLACED ORDERS CANNOT BE CANCELLED");

    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        logger.info("Fetching orders page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderDao.findAll(pageRequest);
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

    public Map<String, Integer> aggregateItems(String orderId) throws ApiException {

        OrderPojo orderPojo = getOrderByOrderId(orderId);

        return aggregateItems(orderPojo.getOrderItems());
    }

    public OrderPojo getOrderByOrderId(String orderId) throws ApiException {

        OrderPojo pojo = orderDao.findByOrderId(orderId);
        if (pojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        return pojo;
    }

    public void updatePlacedStatus(String orderId) throws ApiException {
        OrderPojo orderPojo = getOrderByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        orderPojo.setOrderStatus("PLACED");
        orderDao.save(orderPojo);
    }

    public Page<OrderPojo> filterOrders(ZonedDateTime start, ZonedDateTime end, int page, int size) {
        // Fetch paginated results from DAO
        Page<OrderPojo> pojoPage = orderDao.findOrdersBetween(start, end, page, size);

        List<OrderPojo> dataList = pojoPage.getContent().stream()
                .toList();

        return new PageImpl<>(dataList, pojoPage.getPageable(), pojoPage.getTotalElements());
    }

    public String getOrderStatus(String orderId) throws ApiException {
        return getOrderByOrderId(orderId).getOrderStatus();
    }
}