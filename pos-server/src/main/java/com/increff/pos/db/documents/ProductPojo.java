package com.increff.pos.db.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "products")
@CompoundIndex(name = "barcode_idx", def = "{'barcode': 1}", unique = true)
public class ProductPojo extends AbstractPojo{
    private String barcode;
    private String clientName;
    private String name;
    private Double mrp;
    private String imageUrl;
}