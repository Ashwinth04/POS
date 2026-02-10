package com.increff.pos.model.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderData {
    private String orderId;
    private ZonedDateTime createdAt;
    private String orderStatus;
    private List<OrderItem> orderItems;
}
