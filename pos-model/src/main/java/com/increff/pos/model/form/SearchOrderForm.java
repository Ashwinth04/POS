package com.increff.pos.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchOrderForm {
    @NotBlank(message = "OrderId cannot be empty")
    private String orderId;

    @Min(value = 0, message = "Page number cannot be negative")
    int page;

    @Min(value = 1, message = "Page size must be positive")
    @Max(value = 100, message = "Page size cannot be greater than 100")
    int size;
}
