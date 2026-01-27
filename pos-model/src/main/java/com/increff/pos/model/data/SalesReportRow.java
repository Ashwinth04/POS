package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SalesReportRow {
    private String product;   // barcode
    private int quantity;
    private double revenue;
}
