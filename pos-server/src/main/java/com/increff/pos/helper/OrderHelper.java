package com.increff.pos.helper;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderHelper {

    public static OrderPojo convertToEntity(OrderForm orderForm) {

        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setOrderTime(Instant.now());

        List<OrderItem> items = new ArrayList<>();

        for (OrderItemForm item: orderForm.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBarcode(item.getBarcode());
            orderItem.setOrderedQuantity(item.getOrderedQuantity());
            orderItem.setSellingPrice(item.getSellingPrice());

            items.add(orderItem);
        }

        orderPojo.setOrderItems(items);
        String id = generate();
        orderPojo.setOrderId(id);
        return orderPojo;
    }

    public static OrderData convertToData(OrderPojo orderPojo) {

        OrderData orderData = new OrderData();

        orderData.setOrderId(orderPojo.getOrderId());
        orderData.setOrderTime(orderPojo.getOrderTime());
        orderData.setOrderStatus(orderPojo.getOrderStatus());
        orderData.setOrderItems(orderPojo.getOrderItems());

        return orderData;
    }

    public static OrderData convertToOrderDto(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setOrderTime(orderPojo.getOrderTime());
        orderData.setOrderItems(orderPojo.getOrderItems());
        orderData.setOrderStatus(orderPojo.getOrderStatus());

        return orderData;
    }

    public static String generate() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        String random = UUID.randomUUID()
                .toString()
                .substring(0, 4)
                .toUpperCase();

        return "ORD-" + timestamp + "-" + random;
    }
}
