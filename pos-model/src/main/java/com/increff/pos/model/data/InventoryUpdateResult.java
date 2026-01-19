package com.increff.pos.model.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryUpdateResult {
    private int quantity;
    private String barcode;
    private String status;
    private String message;
}
