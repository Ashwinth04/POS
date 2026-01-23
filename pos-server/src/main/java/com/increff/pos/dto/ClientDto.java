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
        ValidationUtil.validateClientForm(clientForm);
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo savedClientPojo = clientApi.addClient(clientPojo);
        return ClientHelper.convertToDto(savedClientPojo);
    }

    public Page<ClientData> getAllClients(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ClientPojo> clientPage = clientApi.getAllClients(form.getPage(), form.getSize());
        return clientPage.map(ClientHelper::convertToDto);
    }

    public ClientData updateClientDetails(String oldName, ClientForm clientForm) throws ApiException {
        ValidationUtil.validateClientForm(clientForm);
        ValidationUtil.validateName(oldName);
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo updatedClientPojo = clientApi.updateClient(oldName, clientPojo);
        return ClientHelper.convertToDto(updatedClientPojo);
    }

    public List<ClientData> searchClient(String name) throws ApiException {
        ValidationUtil.validateName(name);
        List<ClientPojo> results = clientApi.searchClient(name);
        List<ClientData> response = new ArrayList<>();

        for (ClientPojo pojo : results) {
            response.add(ClientHelper.convertToDto(pojo));
        }

        return response;
    }
}