package com.increff.pos.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ClientSearchForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ClientDto {

    @Autowired
    private ClientApiImpl clientApi;

    public ClientData createClient(ClientForm clientForm) throws ApiException {
        NormalizationUtil.normalizeClientForm(clientForm);
        FormValidator.validate(clientForm);
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        clientApi.checkNameExists(clientPojo.getName());
        ClientPojo savedClientPojo = clientApi.addClient(clientPojo);
        return ClientHelper.convertToData(savedClientPojo);
    }

    public Page<ClientData> getAllClients(PageForm form) throws ApiException {
        FormValidator.validate(form);
        Page<ClientPojo> clientPage = clientApi.getAllClients(form.getPage(), form.getSize());
        return clientPage.map(ClientHelper::convertToData);
    }

    public ClientData updateClientDetails(ClientForm clientForm) throws ApiException {
        NormalizationUtil.normalizeClientForm(clientForm);
        FormValidator.validate(clientForm);
        ClientPojo clientPojo = ClientHelper.convertToEntity(clientForm);
        return ClientHelper.convertToData(clientApi.updateClient(clientPojo));
    }

    public Page<ClientData> search(ClientSearchForm searchForm) throws ApiException {
        NormalizationUtil.normalizeSearchClientForm(searchForm);
        FormValidator.validate(searchForm);
        Page<ClientPojo> clientPage = clientApi.search(searchForm.getType(), searchForm.getQuery(),
                searchForm.getPage(), searchForm.getSize());
        return clientPage.map(ClientHelper::convertToData);
    }
}