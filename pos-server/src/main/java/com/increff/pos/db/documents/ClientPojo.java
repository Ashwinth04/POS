package com.increff.pos.db.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "clients")
@CompoundIndexes({
        @CompoundIndex(name = "name_idx", def = "{ 'name': 1 }", unique = true),
        @CompoundIndex(name = "email_idx", def = "{ 'email': 1 }"),
        @CompoundIndex(name = "phone_idx", def = "{ 'phoneNumber': 1 }")
})
public class ClientPojo extends AbstractPojo {
    private String name;
    private String email;
    private String location;
    private String phoneNumber;
}
