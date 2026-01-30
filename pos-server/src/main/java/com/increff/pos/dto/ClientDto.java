package com.increff.pos.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientDto {

    private final ClientApiImpl clientApi;

    public ClientDto(ClientApiImpl clientApi) {
        this.clientApi = clientApi;
    }

    public ClientData createClient(ClientForm clientForm) throws ApiException {
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo normalizedClientPojo = ClientHelper.normalizeClient(clientPojo);
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

    //TODO this api needs to paginated.
    public List<ClientData> searchClient(String name) throws ApiException {
        ValidationUtil.validateName(name);
        List<ClientPojo> results = clientApi.searchClient(name);
        List<ClientData> response = new ArrayList<>();

        for (ClientPojo pojo : results) {
            response.add(ClientHelper.convertToDto(pojo));
        }

        return response;
    }

    //TODO this api needs to paginated.
    public List<ClientData> searchClientByEmail(String email) throws ApiException {
        ValidationUtil.validateEmail(email);
        List<ClientPojo> results = clientApi.searchClientByEmail(email);
        List<ClientData> response = new ArrayList<>();

        for (ClientPojo pojo : results) {
            response.add(ClientHelper.convertToDto(pojo));
        }

        return response;
    }
}