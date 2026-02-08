package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
public class OrderItemPojo extends AbstractPojo {
    private String orderItemId;
    private String productId;
    private Integer orderedQuantity;
    private Double sellingPrice;
}
