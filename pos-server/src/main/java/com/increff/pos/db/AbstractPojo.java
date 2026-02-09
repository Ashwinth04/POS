package com.increff.pos.db;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class AbstractPojo {
    @Id
    private String id;
    @CreatedDate
    private ZonedDateTime createdAt; // TODO: Check if you can index inside only one implementation
    @LastModifiedDate
    private ZonedDateTime updatedAt;
} 