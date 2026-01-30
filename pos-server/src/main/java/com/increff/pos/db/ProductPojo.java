package com.increff.pos.db;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "products")
public class ProductPojo extends AbstractPojo{
    @Indexed(unique = true)
    private String barcode;
    private String clientName;
    private String name;
    private Double mrp;
    private String imageUrl;
}