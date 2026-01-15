package com.increff.pos.dao;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ClientDao extends AbstractDao<ClientPojo> {
    public ClientDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(ClientPojo.class),
                mongoOperations
        );
    }

    public ClientPojo findByName(String name) {
        Query query = Query.query(Criteria.where("name").is(name));
        return mongoOperations.findOne(query, ClientPojo.class);
    }

    public void updateByName(String oldName, ClientPojo clientPojo) throws ApiException {
        String newName = clientPojo.getName();
        String newEmail = clientPojo.getEmail();
        String newLocation = clientPojo.getLocation();
        String newPhoneNumber = clientPojo.getPhoneNumber();
        Instant updateTime = clientPojo.getUpdatedAt();

        Query query = Query.query(Criteria.where("name").is(oldName));
        Update update = new Update()
                .set("name", newName)
                .set("email", newEmail)
                .set("location", newLocation)
                .set("phoneNumber", newPhoneNumber)
                .set("updatedAt",updateTime);

        UpdateResult result = mongoOperations.updateFirst(query, update, ClientPojo.class);

        if (result.getMatchedCount() == 0) {
            throw new ApiException("No matching client found");
        }
    }

    public Set<String> findExistingClientIds(Set<String> clientIds) {

        if (clientIds == null || clientIds.isEmpty()) {
            return Set.of();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(clientIds));

        // Only fetch the id field (optimization)
        query.fields().include("id");

        List<ClientPojo> clients = mongoOperations.find(query, ClientPojo.class);

        return clients.stream()
                .map(ClientPojo::getId)
                .collect(Collectors.toSet());
    }


    @Override
    public Page<ClientPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}