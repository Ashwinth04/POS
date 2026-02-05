package com.increff.pos.model.form;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductForm {

    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @NotBlank(message = "Client Name cannot be empty")
    @Size(min = 3, max = 21, message = "Number of characters for Client Name should be between 3 to 21")
    private String clientName;

    @NotBlank(message = "Product Name cannot be empty")
    @Size(min = 3, max = 21, message = "Number of characters for Product Name should be between 3 to 21")
    private String name;

    @Min(value = 1, message = "MRP cannot be less than or equal to zero")
    @Max(value = 1000000, message = "MRP cannot be greater than or equal to 1000000")
    private Double mrp;

    @Pattern(
            regexp = "^(https?://.+)?$",
            message = "Invalid URL format"
    )
    private String imageUrl;
}
