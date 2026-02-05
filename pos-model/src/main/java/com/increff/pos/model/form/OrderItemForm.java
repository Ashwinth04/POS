package com.increff.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemForm {
    @NotBlank(message = "barcode cannot be null or empty")
    private String barcode;

    @NotNull(message = "orderedQuantity cannot be null")
    @Positive(message = "orderedQuantity must be > 0")
    private Integer orderedQuantity;

    @NotNull(message = "sellingPrice cannot be null")
    @Positive(message = "sellingPrice must be > 0")
    private Double sellingPrice;
}
