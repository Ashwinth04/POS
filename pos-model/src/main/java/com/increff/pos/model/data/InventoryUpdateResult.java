package com.increff.pos.model.data;

import lombok.Data;

@Data
public class InventoryUpdateResult {
    private int quantity;
    private String barcode;
    private String status;
    private String message;
}
