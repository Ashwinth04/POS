package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String orderItemId;
    private String productId;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
