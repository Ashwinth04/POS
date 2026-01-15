package com.increff.pos.model.form;

import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderItemForm;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class OrderForm {
    private List<OrderItemForm> orderItems;
}
