package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FormValidator {

    @Autowired
    private Validator validator;

    public <T> void validate(T form) throws ApiException {
        Set<ConstraintViolation<T>> violations = validator.validate(form);

        if (!violations.isEmpty()) {
            String errorMessage = violations
                    .iterator()
                    .next()
                    .getMessage();
            throw new ApiException(errorMessage);
        }
    }

}
