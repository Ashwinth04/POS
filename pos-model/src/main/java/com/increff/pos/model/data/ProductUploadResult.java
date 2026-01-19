package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUploadResult {
    private String barcode;
    private String clientId;
    private String name;
    private Double mrp;
    private String imageUrl;

    private String productId;
    private String status;   // SUCCESS / FAILED
    private String message;
}
