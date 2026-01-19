package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemForm {
    private String barcode;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
