package com.increff.pos.dao;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.PageImpl;
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
import java.util.regex.Pattern;
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

    public List<ClientPojo> findExistingClientNames(List<String> clientNames) {

        if (clientNames == null || clientNames.isEmpty()) {
            return List.of();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("name").in(clientNames));

        query.fields().include("name");

        return mongoOperations.find(query, ClientPojo.class);
    }

    public Page<ClientPojo> searchByName(String name, Pageable pageable) throws ApiException {

        if (name == null || name.isEmpty()) {
            throw new ApiException("Search text cannot be empty");
        }


        String pattern = ".*" + Pattern.quote(name) + ".*";
        Query query = new Query(
                Criteria.where("name").regex(pattern, "i")
        );

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);

        List<ClientPojo> results =
                mongoOperations.find(query, ClientPojo.class);

        return new PageImpl<>(results, pageable, total);
    }

    public Page<ClientPojo> searchByEmail(String email, Pageable pageable) throws ApiException {

        if (email == null || email.isEmpty()) {
            throw new ApiException("Search text cannot be empty");
        }

        String pattern = ".*" + Pattern.quote(email) + ".*";
        Query query = new Query(
                Criteria.where("email").regex(pattern, "i")
        );

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);

        List<ClientPojo> results =
                mongoOperations.find(query, ClientPojo.class);

        return new PageImpl<>(results, pageable, total);
    }

    public Page<ClientPojo> searchByPhoneNumber(String phoneNumber, Pageable pageable) throws ApiException {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new ApiException("Search text cannot be empty");
        }

        String pattern = ".*" + Pattern.quote(phoneNumber) + ".*";
        Query query = new Query(
                Criteria.where("phoneNumber").regex(pattern, "i")
        );

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);

        List<ClientPojo> results =
                mongoOperations.find(query, ClientPojo.class);

        return new PageImpl<>(results, pageable, total);
    }

}