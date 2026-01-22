package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

public interface ClientApi {
    ClientPojo addClient(ClientPojo clientPojo) throws ApiException;
    Page<ClientPojo> getAllClients(int page, int size) throws ApiException;
    ClientPojo updateClient(String oldName, ClientPojo clientPojo) throws ApiException;
}
