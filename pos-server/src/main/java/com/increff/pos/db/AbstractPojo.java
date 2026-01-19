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
    private String id; //ZonedDateTime
    @CreatedDate // These 2 annotations enable mongodb to automatically set these fields when inserting or updating a record
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
} 