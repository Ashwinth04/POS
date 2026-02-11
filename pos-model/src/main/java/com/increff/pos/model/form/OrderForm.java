package com.increff.pos.model.form;

import com.increff.pos.model.data.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderForm {
    @NotNull(message = "orderItems cannot be null")
    @NotEmpty(message = "orderItems cannot be empty")
    private List<OrderItemForm> orderItems;
}