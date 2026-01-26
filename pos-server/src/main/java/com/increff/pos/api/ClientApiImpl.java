package com.increff.pos.api;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.ClientPojo;
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

@Service
public class ClientApiImpl implements ClientApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientApiImpl.class);

    private final ClientDao clientDao;

    public ClientApiImpl(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public ClientPojo addClient(ClientPojo clientPojo) throws ApiException {
        logger.info("Creating client with name: {}", clientPojo.getName());

        checkNameExists("",clientPojo.getName());
        checkEmailExists("",clientPojo.getEmail());
        checkPhoneNumberExists("",clientPojo.getPhoneNumber());

        // Save the new client
        ClientPojo saved = clientDao.save(clientPojo);

        logger.info("Created client with id: {}", saved.getId());
        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Page<ClientPojo> getAllClients(int page, int size) {
        logger.info("Fetching clients page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return clientDao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo updateClient(String oldName, ClientPojo clientPojo) throws ApiException {
        ClientPojo existingRecord = clientDao.findByName(oldName);

        if (existingRecord == null) {throw new ApiException("Client doesn't exist");}

        checkNameExists(existingRecord.getId(),clientPojo.getName());
        checkEmailExists(existingRecord.getId(),clientPojo.getEmail());
        checkPhoneNumberExists(existingRecord.getId(),clientPojo.getPhoneNumber());

        clientPojo.setId(existingRecord.getId());

        return clientDao.save(clientPojo);
    }

    public List<ClientPojo> searchClient(String name) throws ApiException {
        return clientDao.search(name);
    }

    public List<ClientPojo> searchClientByEmail(String email) throws ApiException {
        return clientDao.searchByEmail(email);
    }

    private void checkEmailExists(String id, String email) throws ApiException {
        ClientPojo existing = clientDao.findByEmail(email);

        if (existing != null && !existing.getId().equals(id)) {
            throw new ApiException("Email already exists");
        }
    }

    private void checkPhoneNumberExists(String id, String phoneNumber) throws ApiException {
        ClientPojo existing = clientDao.findByPhoneNumber(phoneNumber);

        if (existing != null && !existing.getId().equals(id)) {
            throw new ApiException("Phone Number already exists");
        }
    }

    private void checkNameExists(String id, String name) throws ApiException {
        ClientPojo existing = clientDao.findByName(name);

        if (existing != null && !existing.getId().equals(id)) {
            throw new ApiException("Client already exists");
        }
    }


}
