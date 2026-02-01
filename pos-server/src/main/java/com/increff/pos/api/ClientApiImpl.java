package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ClientApiImpl implements ClientApi {

    @Autowired
    private ClientDao clientDao;

    @Transactional(rollbackFor = Exception.class)
    public ClientPojo addClient(ClientPojo clientPojo) throws ApiException {

        return clientDao.save(clientPojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public Page<ClientPojo> getAllClients(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return clientDao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo updateClient(ClientPojo clientPojo) throws ApiException {

        String clientName = clientPojo.getName();
        ClientPojo existingRecord = getCheckByClientName(clientName);
        clientPojo.setId(existingRecord.getId());

        return clientDao.save(clientPojo);
    }

    public void checkNameExists(String name) throws ApiException {
        ClientPojo existing = clientDao.findByName(name);

        if (Objects.nonNull(existing)) {
            throw new ApiException("Client already exists");
        }
    }

    public List<String> fetchExistingClientNames(List<String> clientNames) {

        List<ClientPojo> clientPojos = clientDao.findExistingClientNames(clientNames);

        return clientPojos.stream().map(ClientPojo::getName).toList();
    }

    public Page<ClientPojo> search(String type, String query, int page, int size)
            throws ApiException {

        if (type == null || query == null || query.isBlank()) {
            throw new ApiException("Search type and query must be provided");
        }

        Pageable pageable = PageRequest.of(page, size);

        return switch (type.toLowerCase()) {
            case "name" ->
                    clientDao.searchByName(query, pageable);

            case "email" ->
                    clientDao.searchByEmail(query, pageable);

            case "phone", "phonenumber" ->
                    clientDao.searchByPhoneNumber(query, pageable);

            default ->
                    throw new ApiException("Invalid search type: " + type);
        };
    }

    public ClientPojo getCheckByClientName(String clientName) throws ApiException {
        ClientPojo record = clientDao.findByName(clientName);

        if (Objects.isNull(record)) {throw new ApiException("Client with the given name doesn't exist");}

        return record;
    }

}
