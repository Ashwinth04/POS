package com.increff.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileForm {
    @NotBlank(message = "File cannot be empty")
    private String base64file;
}
