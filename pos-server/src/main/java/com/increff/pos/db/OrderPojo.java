package com.increff.pos.db;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.increff.pos.model.data.OrderItem;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "orders")
public class OrderPojo extends AbstractPojo {
    @Indexed(unique = true)
    private String orderId;
    private Instant orderTime;
    private String orderStatus;
    private List<OrderItem> orderItems;
}
