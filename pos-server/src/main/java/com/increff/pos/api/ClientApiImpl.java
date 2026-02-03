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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClientApiImpl implements ClientApi {

    @Autowired
    private ClientDao clientDao;

    @Transactional(rollbackFor = Exception.class)
    public ClientPojo addClient(ClientPojo clientPojo) {
        return clientDao.save(clientPojo);
    }

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

    public Map<String, ClientPojo> fetchExistingClientNames(List<String> clientNames) {

        List<ClientPojo> clientPojos = clientDao.findExistingClientNames(clientNames);

        return clientPojos.stream()
                        .collect(Collectors.toMap(
                                ClientPojo::getName,
                                Function.identity()
                        ));

    }

    public Page<ClientPojo> search(String type, String query, int page, int size) throws ApiException {

        // TODO: type should be an enum
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
