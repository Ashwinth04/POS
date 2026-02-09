package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "orders")
public class OrderPojo extends AbstractPojo {
    @Indexed(unique = true)
    private String orderId;
    // TODO: Remove this field
    private Instant orderTime;
    private String orderStatus;
    private List<OrderItemPojo> orderItems;
}
