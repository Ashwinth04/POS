package com.increff.pos.db.documents;

import com.increff.pos.db.subdocuments.OrderItemPojo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "orders")
@CompoundIndexes({
        @CompoundIndex(name = "orderId_idx",
                def = "{'orderId': 1}",
                unique = true),

        @CompoundIndex(name = "orderStatus_createdAt_idx",
                def = "{'orderStatus': 1, 'createdAt': 1}")
})
public class OrderPojo extends AbstractPojo {
    private String orderId;
    private String orderStatus;
    private List<OrderItemPojo> orderItems;
}
