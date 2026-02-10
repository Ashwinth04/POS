package com.increff.pos.db.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
@Document(collection = "users")
public class UserPojo extends AbstractPojo {
    @Indexed(unique = true)
    private String email;
    private String password;
    private String role;
}