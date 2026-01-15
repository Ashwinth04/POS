package com.increff.pos.helper;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ClientHelper {
    public static ClientPojo convertToEntity(ClientForm clientForm) {
        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setName(clientForm.getName());
        clientPojo.setEmail(clientForm.getEmail());
        clientPojo.setLocation(clientForm.getLocation());
        clientPojo.setPhoneNumber(clientForm.getPhoneNumber());
        return clientPojo;
    }

    public static ClientData convertToDto(ClientPojo clientPojo) {
        ClientData clientData = new ClientData();
        clientData.setId(clientPojo.getId());
        clientData.setName(clientPojo.getName());
        clientData.setEmail(clientPojo.getEmail());
        clientData.setLocation(clientPojo.getLocation());
        clientData.setPhoneNumber(clientPojo.getPhoneNumber());
        return clientData;
    }
}