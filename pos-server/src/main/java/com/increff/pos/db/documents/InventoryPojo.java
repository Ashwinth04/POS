package com.increff.pos.db.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Document(collection = "inventories")
@CompoundIndex(name = "productId_idx", def = "{'productId': 1}", unique = true)
public class InventoryPojo extends AbstractPojo {
    private String productId;
    private int quantity;
}
