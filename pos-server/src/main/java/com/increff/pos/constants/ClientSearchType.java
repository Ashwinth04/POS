package com.increff.pos.constants;

import com.increff.pos.exception.ApiException;

public enum ClientSearchType {
    NAME,
    EMAIL,
    PHONE;

    public static ClientSearchType from(String value) throws ApiException {
        try {
            return ClientSearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApiException("Invalid search type: " + value);
        }
    }
}
