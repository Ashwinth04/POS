package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class OrderApiImpl implements OrderApi {

    @Autowired
    private OrderDao orderDao;

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo createOrder(OrderPojo orderPojo, boolean isFulFillable) {

        orderPojo.setOrderStatus(isFulFillable ? "FULFILLABLE" : "UNFULFILLABLE");

        return orderDao.save(orderPojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo editOrder(OrderPojo orderPojo, boolean isFulFillable) throws ApiException {

        orderPojo.setOrderStatus(isFulFillable ? "FULFILLABLE" : "UNFULFILLABLE");

        OrderPojo record = getCheckByOrderId(orderPojo.getOrderId());

        orderPojo.setId(record.getId());
        return orderDao.save(orderPojo);
    }

    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo record = getCheckByOrderId(orderId);

        record.setOrderStatus("CANCELLED");
        orderDao.save(record);

        return new MessageData("Order cancelled successfully!");
    }

    public Page<OrderPojo> getAllOrders(int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderTime"));
        return orderDao.findAll(pageRequest);
    }

    public OrderPojo getCheckByOrderId(String orderId) throws ApiException {

        OrderPojo pojo = orderDao.findByOrderId(orderId);
        if (pojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        return pojo;
    }

    public void updatePlacedStatus(String orderId) throws ApiException {
        OrderPojo orderPojo = getCheckByOrderId(orderId);

        if (orderPojo == null) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        orderPojo.setOrderStatus("PLACED");
        orderDao.save(orderPojo);
    }

    public Page<OrderPojo> filterOrders(ZonedDateTime start, ZonedDateTime end, int page, int size) {

        Page<OrderPojo> pojoPage = orderDao.findOrdersBetween(start, end, page, size);

        List<OrderPojo> dataList = pojoPage.getContent().stream()
                .toList();

        return new PageImpl<>(dataList, pojoPage.getPageable(), pojoPage.getTotalElements());
    }

    public String getOrderStatus(String orderId) throws ApiException {
        return getCheckByOrderId(orderId).getOrderStatus();
    }
}