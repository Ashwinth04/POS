package com.increff.pos.helper;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.OrderForm;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return orderPojo;
    }

    public static OrderStatusData convertToDto(Map<String, OrderStatus> orderStatuses) {
        OrderStatusData orderStatusData = new OrderStatusData();
        List<OrderStatus> statuses = new ArrayList<>();

        orderStatuses.forEach((orderItemId, status) -> {
            statuses.add(status);
        });

        orderStatusData.setOrderItems(statuses);
        return orderStatusData;
    }

    public static OrderData convertToOrderDto(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setOrderTime(orderPojo.getOrderTime());
        orderData.setOrderItems(orderPojo.getOrderItems());
        orderData.setOrderStatus(orderPojo.getOrderStatus());

        return orderData;
    }
}
