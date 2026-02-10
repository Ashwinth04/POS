package com.increff.pos.db.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
@Document(collection = "users")
@CompoundIndex(name = "email_idx", def = "{'email':1}", unique = true)
public class UserPojo extends AbstractPojo {
    private String email;
    private String password;
    private String role;
}