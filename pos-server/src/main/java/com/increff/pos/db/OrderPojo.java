package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.increff.pos.model.data.OrderItem;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
public class OrderPojo extends AbstractPojo {
    private Instant orderTime;
    private List<OrderItem> orderItems;
}
