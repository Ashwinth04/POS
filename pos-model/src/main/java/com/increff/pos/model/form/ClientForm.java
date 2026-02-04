package com.increff.pos.model.form;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 3, max = 21, message = "Number of characters for Name should be between 3 to 21")
    private String name;
    @NotBlank(message = "Email cannot be empty")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Invalid email format"
    )
    private String email;

    @NotBlank(message = "Location cannot be empty")
    @Size(
            min = 5,
            max = 30,
            message = "Phone number must be exactly 10 digits"
    )
    private String location;
    @NotBlank(message = "Phone number is required")

    @Pattern(
            regexp = "^[0-9]+$",
            message = "Phone number must contain only digits"
    )
    @Size(
            min = 10,
            max = 10,
            message = "Phone number must be exactly 10 digits"
    )
    private String phoneNumber;
}
