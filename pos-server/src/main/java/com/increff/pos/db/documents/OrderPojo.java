package com.increff.pos.db.documents;

import com.increff.pos.db.subdocuments.OrderItemPojo;
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
    private Instant orderTime;
    private String orderStatus;
    private List<OrderItemPojo> orderItems;
}
