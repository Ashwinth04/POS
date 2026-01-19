package com.increff.pos.model.form;

import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderItemForm;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderForm {
    private List<OrderItemForm> orderItems;
}
