package com.increff.pos.api;

import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

public interface ClientApi {
    ClientPojo addClient(ClientPojo clientPojo) throws ApiException;
    Page<ClientPojo> getAllClients(int page, int size) throws ApiException;
    ClientPojo updateClient(ClientPojo clientPojo) throws ApiException;
}
