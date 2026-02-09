package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class OrderApiImpl implements OrderApi {

    @Autowired
    private OrderDao orderDao;

    public OrderPojo createOrder(OrderPojo orderPojo) {
        return orderDao.save(orderPojo);
    }

    public OrderPojo editOrder(OrderPojo orderPojo) throws ApiException {

        OrderPojo record = getCheckByOrderId(orderPojo.getOrderId());
        orderPojo.setId(record.getId());
        return orderDao.save(orderPojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public MessageData cancelOrder(String orderId) throws ApiException {

        OrderPojo record = getCheckByOrderId(orderId);
        record.setOrderStatus("CANCELLED");
        orderDao.save(record);
        return new MessageData("Order cancelled successfully!");
    }

    public Page<OrderPojo> getAllOrders(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderId"));
        return orderDao.findAll(pageRequest);
    }

    public OrderPojo getCheckByOrderId(String orderId) throws ApiException {

        OrderPojo pojo = orderDao.findByOrderId(orderId);
        if (Objects.isNull(pojo)) throw new ApiException("ORDER WITH THE GIVEN ID DOESN'T EXIST");

        return pojo;
    }

    @Transactional(rollbackFor = ApiException.class)
    public void updatePlacedStatus(String orderId) throws ApiException {
        OrderPojo orderPojo = getCheckByOrderId(orderId);

        orderPojo.setOrderStatus("PLACED");
        orderDao.save(orderPojo);
    }

    public Page<OrderPojo> filterOrders(ZonedDateTime start, ZonedDateTime end, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderId"));
        Page<OrderPojo> pojoPage = orderDao.findOrdersBetween(start, end, pageable);

        List<OrderPojo> dataList = pojoPage.getContent().stream()
                .toList();

        return new PageImpl<>(dataList, pojoPage.getPageable(), pojoPage.getTotalElements());
    }

    public Page<OrderPojo> search(String orderId, int page, int size) throws ApiException {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderId"));
        return orderDao.search(orderId, pageable);
    }
}