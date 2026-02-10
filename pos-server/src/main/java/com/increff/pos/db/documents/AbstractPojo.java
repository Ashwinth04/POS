package com.increff.pos.db.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.ZonedDateTime;

@Getter
@Setter
@CompoundIndex(name = "createdAt_idx", def = "{'createdAt':1}")
public abstract class AbstractPojo {
    @Id
    private String id;
    @CreatedDate
    private ZonedDateTime createdAt;
    @LastModifiedDate
    private ZonedDateTime updatedAt;
} 