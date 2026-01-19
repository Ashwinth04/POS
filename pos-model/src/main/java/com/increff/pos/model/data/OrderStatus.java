package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatus {
    private String orderItemId;
    private String status;
    private String message;
}
