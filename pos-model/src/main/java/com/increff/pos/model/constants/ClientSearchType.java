package com.increff.pos.model.constants;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ClientSearchType {
    NAME,
    EMAIL,
    PHONE;

    @JsonCreator
    public static ClientSearchType from(String value) {
        return ClientSearchType.valueOf(value.toUpperCase());
    }
}

