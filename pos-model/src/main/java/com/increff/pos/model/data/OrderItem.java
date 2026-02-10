package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String barcode;
    private String productName;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
