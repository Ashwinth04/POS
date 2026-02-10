package com.increff.pos.model.form;

import com.increff.pos.model.constants.ClientSearchType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSearchForm {

    @NotNull(message = "Type cannot be empty")
    private ClientSearchType type;

    @NotBlank(message = "Query cannot be empty")
    @NotNull(message = "Query cannot be null")
    private String query;

    @Min(value = 0, message = "Page number cannot be negative")
    private int page;

    @Min(value = 1, message = "Page size must be positive")
    @Max(value = 100, message = "Page size cannot be greater than 100")
    private int size;
}
