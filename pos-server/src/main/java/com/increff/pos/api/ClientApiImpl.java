package com.increff.pos.api;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientApiImpl implements ClientApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientDao.class);

    @Autowired
    private ClientDao dao;

    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo add(ClientPojo clientPojo) throws ApiException {
        logger.info("Creating client with name: {}", clientPojo.getName());

        checkNameExists("",clientPojo.getName());
        checkEmailExists("",clientPojo.getEmail());
        checkPhoneNumberExists("",clientPojo.getPhoneNumber());

        // Save the new client
        ClientPojo saved = dao.save(clientPojo);
        logger.info("Created client with id: {}", saved.getId());
        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Page<ClientPojo> getAll(int page, int size) {
        logger.info("Fetching clients page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return dao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ClientPojo update(String oldName, ClientPojo clientPojo) throws ApiException {
        ClientPojo existingRecord = dao.findByName(oldName);

        if (existingRecord == null) {throw new ApiException("Client doesn't exist");}

        checkNameExists(existingRecord.getId(),clientPojo.getName());
        checkEmailExists(existingRecord.getId(),clientPojo.getEmail());
        checkPhoneNumberExists(existingRecord.getId(),clientPojo.getPhoneNumber());

        clientPojo.setId(existingRecord.getId());

        return dao.save(clientPojo);
    }

    public List<ClientPojo> search(String name) throws ApiException {
        return dao.search(name);
    }

    private void checkEmailExists(String id, String email) throws ApiException {
        ClientPojo existing = dao.findByEmail(email);

        if (existing != null && !existing.getId().equals(id)) {
            throw new ApiException("Email already exists");
        }
    }


    private void checkPhoneNumberExists(String id, String phoneNumber) throws ApiException {
        ClientPojo existing = dao.findByPhoneNumber(phoneNumber);

        if (existing != null && !existing.getId().equals(id)) {
            throw new ApiException("Phone Number already exists");
        }
    }


    private void checkNameExists(String id, String name) throws ApiException {
        ClientPojo existing = dao.findByName(name);

        if (existing != null && !existing.getId().equals(id)) {
            throw new ApiException("Client already exists");
        }
    }


}
