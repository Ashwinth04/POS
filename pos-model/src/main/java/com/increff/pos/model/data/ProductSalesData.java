package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductSalesData {
    String barcode;
    int quantity;
    double revenue;
}
