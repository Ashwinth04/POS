package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "clients")
public class ClientPojo extends AbstractPojo {
    @Indexed(unique = true)
    private String name;
    private String email;
    private String location;
    private String phoneNumber;
}
