package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "inventories")
public class InventoryPojo extends AbstractPojo {
    @Field("productId")
    private String productId;
    @Field("quantity")
    private int quantity;
}
