package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductData {
    private String id;
    private String barcode;
    private String clientId;
    private String name;
    private Double mrp;
    private String imageUrl;
}
