package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryData {
    private String barcode;
    private int quantity;
}
