package com.increff.helper;

import com.increff.db.OrderPojo;
import com.increff.pos.model.data.OrderData;

public class InvoiceHelper {

    public static OrderData convertToOrderDto(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setOrderTime(orderPojo.getOrderTime());
        orderData.setOrderItems(orderPojo.getOrderItems());
        orderData.setOrderStatus(orderPojo.getOrderStatus());
        orderData.setId(orderPojo.getOrderId());

        return orderData;
    }
}