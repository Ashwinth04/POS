package com.increff.pos.model.form;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {
    private String name;
    private String email;
    private String location;
    private String phoneNumber;
}
