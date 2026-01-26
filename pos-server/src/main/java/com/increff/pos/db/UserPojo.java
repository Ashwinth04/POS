package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

@Getter
@Setter
@Document(collection = "users")
public class UserPojo extends AbstractPojo {
    private String id;
    private String username;
    private String password;
    private String status;
    private String role;
}