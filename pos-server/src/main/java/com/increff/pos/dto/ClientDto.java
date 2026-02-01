package com.increff.pos.dto;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientDto {

    @Autowired
    private ClientApiImpl clientApi;

    public ClientData createClient(ClientForm clientForm) throws ApiException {
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo normalizedClientPojo = ClientHelper.normalizeClient(clientPojo);
        clientApi.checkNameExists(clientPojo.getName());
        ClientPojo savedClientPojo = clientApi.addClient(normalizedClientPojo);
        return ClientHelper.convertToDto(savedClientPojo);
    }

    public Page<ClientData> getAllClients(PageForm form) throws ApiException {
        Page<ClientPojo> clientPage = clientApi.getAllClients(form.getPage(), form.getSize());
        return clientPage.map(ClientHelper::convertToDto);
    }

    public ClientData updateClientDetails(ClientForm clientForm) throws ApiException {
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo normalizedClientPojo = ClientHelper.normalizeClient(clientPojo);

        ClientPojo updatedClientPojo = clientApi.updateClient(normalizedClientPojo);
        return ClientHelper.convertToDto(updatedClientPojo);
    }

    public Page<ClientData> search(String type, String query, PageForm pageForm) throws ApiException {
        ValidationUtil.validateSearchParams(type, query);
        Page<ClientPojo> clientPage = clientApi.search(type, query, pageForm.getPage(), pageForm.getSize());
        return clientPage.map(ClientHelper::convertToDto);
    }
}