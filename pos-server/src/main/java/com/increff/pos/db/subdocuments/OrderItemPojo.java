package com.increff.pos.db.subdocuments;

import com.increff.pos.db.documents.AbstractPojo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemPojo extends AbstractPojo {
    private String orderItemId;
    private String productId;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
