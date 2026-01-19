package com.increff.pos.model.form;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryUpdateForm {
    private int quantity;
    private String barcode;
}
