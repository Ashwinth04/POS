package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

public interface ClientApi {
    ClientPojo add(ClientPojo clientPojo) throws ApiException;
    Page<ClientPojo> getAll(int page, int size) throws ApiException;
    ClientPojo update(String oldName, ClientPojo clientPojo) throws ApiException;
}
