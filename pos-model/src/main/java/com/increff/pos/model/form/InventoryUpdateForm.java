package com.increff.pos.model.form;

import lombok.Data;

@Data
public class InventoryUpdateForm {
    private int quantity;
    private String barcode;
}
