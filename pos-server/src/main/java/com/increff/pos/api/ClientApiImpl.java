package com.increff.pos.api;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import jakarta.validation.constraints.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final ClientDao clientDao;

    public ClientApiImpl(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    // api -> get()
    // -> getCheck()
    @Transactional(rollbackFor = Exception.class)
    public ClientPojo addClient(ClientPojo clientPojo) throws ApiException {

        checkNameExists(clientPojo.getName());

        ClientPojo saved = clientDao.save(clientPojo);

        return saved;
    }

//    public ClientPojo getCheckByName(String name) {
//        ClientPojo clientPojo = clientDao.
//    }

    @Transactional(rollbackFor = ApiException.class)
    public Page<ClientPojo> getAllClients(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return clientDao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo updateClient(ClientPojo clientPojo) throws ApiException {

        String clientName = clientPojo.getName();
        ClientPojo existingRecord = clientDao.findByName(clientName);

        if (existingRecord == null) {throw new ApiException("Client with the given name doesn't exist");}

        clientPojo.setId(existingRecord.getId());

        return clientDao.save(clientPojo);
    }

    // getByClientName
    public List<ClientPojo> searchClient(String name) {
        return clientDao.search(name);
    }

    public List<ClientPojo> searchClientByEmail(String email) {
        return clientDao.searchByEmail(email);
    }

    private void checkNameExists(String name) throws ApiException {
        ClientPojo existing = clientDao.findByName(name);

        if (existing != null) {
            throw new ApiException("Client already exists");
        }
    }

    public void checkClientExists(String clientName) throws ApiException {
        ClientPojo client = clientDao.findByName(clientName);

        // Objects.isNull
        if (Objects.isNull(client)) { throw new ApiException("Client with the given name does not exist"); }
    }

    public List<String> fetchExistingClientNames(List<ProductPojo> pojos) {
        List<String> requestedClientNames = pojos.stream()
                .map(ProductPojo::getClientName)
                .toList();

        return clientDao.findExistingClientNames(requestedClientNames);
    }

}
