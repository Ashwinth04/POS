package com.increff.pos.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import com.mongodb.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class AppRestControllerAdvice {

    @ExceptionHandler({ApiException.class, IOException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handle(ApiException e) {
        return new MessageData(e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageData handle(Throwable e) {
        return new MessageData("An internal error occurred");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageData handle(DuplicateKeyException e) {
        return new MessageData("A record with this key already exists");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = "Invalid input format";

        if (ex.getCause() instanceof InvalidFormatException ife) {
            String field = ife.getPath().get(0).getFieldName();
            message = field + " has invalid value";
        }

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

}