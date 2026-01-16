package com.increff.pos.dto;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.helper.UserHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import jakarta.validation.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientDto {

    @Autowired
    private ClientApiImpl clientApi;

    public ClientData create(ClientForm clientForm) throws ApiException {
        ValidationUtil.validateClientForm(clientForm);
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo savedClientPojo = clientApi.add(clientPojo);
        return ClientHelper.convertToDto(savedClientPojo);
    }

    public Page<ClientData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ClientPojo> clientPage = clientApi.getAll(form.getPage(), form.getSize());
        return clientPage.map(ClientHelper::convertToDto);
    }

    public ClientData update(String oldName, ClientForm clientForm) throws ApiException {
        ValidationUtil.validateClientForm(clientForm);
        ValidationUtil.validateName(oldName);
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        ClientPojo updatedClientPojo = clientApi.update(oldName, clientPojo);
        return ClientHelper.convertToDto(updatedClientPojo);
    }

    public List<ClientData> search(String name) throws ApiException {
        ValidationUtil.validateName(name);
        List<ClientPojo> results = clientApi.search(name);
        List<ClientData> response = new ArrayList<>();

        for (ClientPojo pojo : results) {
            response.add(ClientHelper.convertToDto(pojo));
        }

        return response;
    }
}