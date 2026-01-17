package com.increff.pos.helper;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderItemForm;
import com.increff.pos.model.form.OrderForm;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    public static OrderData convertToDto(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        orderData.setOrderTime(orderPojo.getOrderTime());
        orderData.setOrderItems(orderPojo.getOrderItems());
        return orderData;
    }
}
