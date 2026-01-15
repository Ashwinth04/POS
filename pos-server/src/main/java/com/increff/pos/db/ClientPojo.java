package com.increff.pos.db;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "clients")
public class ClientPojo extends AbstractPojo {
    private String name;
//    @Field("email")
    private String email;
//    @Field("location")
    private String location;
//    @Field("phoneNumber")
    private String phoneNumber;
}
