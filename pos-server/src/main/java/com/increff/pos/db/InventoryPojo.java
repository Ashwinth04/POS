package com.increff.pos.db;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "inventories")
public class InventoryPojo extends AbstractPojo {
    @Indexed(unique = true)
    private String barcode;
    private int quantity;
}
