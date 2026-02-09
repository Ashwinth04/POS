package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
//todo : add segregated folders for documents and sub documents
public class OrderItemPojo extends AbstractPojo {
    private String orderItemId;
    private String productId;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
