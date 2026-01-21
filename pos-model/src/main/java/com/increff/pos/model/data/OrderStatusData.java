package com.increff.pos.model.data;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusData {
    private String orderId;
    private List<OrderStatus> orderItems;
}
