package com.increff.pos.dao;

import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

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
        Query query = new Query();
        query.addCriteria(Criteria.where("name").in(clientNames));
        return mongoOperations.find(query, ClientPojo.class);
    }

    public Page<ClientPojo> searchByName(String name, Pageable pageable) {
        String pattern = ".*" + Pattern.quote(name) + ".*";
        Query query = new Query(
                Criteria.where("name").regex(pattern, "i")
        );

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);
        List<ClientPojo> results = mongoOperations.find(query, ClientPojo.class);
        return new PageImpl<>(results, pageable, total);
    }

    public Page<ClientPojo> searchByEmail(String email, Pageable pageable) {
        String pattern = ".*" + Pattern.quote(email) + ".*";
        Query query = new Query(
                Criteria.where("email").regex(pattern, "i")
        );

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);
        List<ClientPojo> results = mongoOperations.find(query, ClientPojo.class);
        return new PageImpl<>(results, pageable, total);
    }

    public Page<ClientPojo> searchByPhoneNumber(String phoneNumber, Pageable pageable) {
        String pattern = ".*" + Pattern.quote(phoneNumber) + ".*";
        Query query = new Query(
                Criteria.where("phoneNumber").regex(pattern, "i")
        );

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);
        List<ClientPojo> results = mongoOperations.find(query, ClientPojo.class);
        return new PageImpl<>(results, pageable, total);
    }
}