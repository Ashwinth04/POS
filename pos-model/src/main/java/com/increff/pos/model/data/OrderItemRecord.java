package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRecord {
    private String orderItemId;
    private String productId;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
