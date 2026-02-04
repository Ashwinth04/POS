package com.increff.pos.model.form;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryForm {
    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;
    @NotNull(message = "Quantity is required")
    @Digits(integer = 10, fraction = 0, message = "quantity must be a whole number")
    @Min(value = 0, message = "Page number cannot be negative")
    private Integer quantity;
}
