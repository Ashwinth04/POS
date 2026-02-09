package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

public class FormValidator {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static <T> void validate(T form) throws ApiException {
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
