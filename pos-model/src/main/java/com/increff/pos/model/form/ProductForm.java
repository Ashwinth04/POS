package com.increff.pos.model.form;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductForm {
    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;
    @NotBlank(message = "Client Name cannot be empty")
    @Size(min = 3, max = 21, message = "Number of characters should be between 3 to 21")
    private String clientName;
    @NotBlank(message = "Product Name cannot be empty")
    @Size(min = 3, max = 21, message = "Number of characters should be between 3 to 21")
    private String name;
    @Min(value = 0, message = "MRP cannot be less than or equal to zero")
    private Double mrp;
    private String imageUrl;
}
