package com.increff.pos.model.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductForm {
    private String barcode;
    private String clientName; //USe client name
    private String name;
    @NotNull(message = "MRP is required")
    @Positive(message = "MRP must be a positive number")
    private Double mrp;
    private String imageUrl;
}
