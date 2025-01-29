package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.ZonedDateTime;

@Data
public abstract class AbstractPojo {
    @Id
    private String id;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
} 