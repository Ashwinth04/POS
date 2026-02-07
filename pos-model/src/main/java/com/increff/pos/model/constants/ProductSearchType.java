package com.increff.pos.model.constants;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductSearchType {
    BARCODE,
    NAME;

    @JsonCreator
    public static ProductSearchType from(String value) {
        return ProductSearchType.valueOf(value.toUpperCase());
    }
}
