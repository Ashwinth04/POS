package com.increff.pos.model.form;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 3, max = 21, message = "Number of characters should be between 3 to 21")
    private String name;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Location cannot be empty")
    private String location;
    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must be 10 digits"
    )
    private String phoneNumber;
}
