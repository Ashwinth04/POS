package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String orderItemId;
    private String orderItemStatus;
    private String barcode;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
