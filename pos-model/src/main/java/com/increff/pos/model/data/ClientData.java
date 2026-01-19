package com.increff.pos.model.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientData {
    private String id;
    private String name;
    private String email;
    private String location;
    private String phoneNumber;
}
