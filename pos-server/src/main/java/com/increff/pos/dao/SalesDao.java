package com.increff.pos.dao;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.SalesPojo;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class SalesDao extends AbstractDao<SalesPojo> {
    public SalesDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(SalesPojo.class),
                mongoOperations
        );
    }

    public SalesPojo findByDate(LocalDate date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("date").is(date));
        return mongoOperations.findOne(query, SalesPojo.class);
    }
}