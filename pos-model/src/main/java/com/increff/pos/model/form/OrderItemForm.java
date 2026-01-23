package com.increff.pos.model.form;

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
