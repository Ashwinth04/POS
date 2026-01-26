package com.increff.pos.dao;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Repository
public class OrderDao extends AbstractDao<OrderPojo> {
    public OrderDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(OrderPojo.class),
                mongoOperations
        );
    }

    public List<OrderPojo> findFulfillableOrders() {
        Query query = new Query();
        query.addCriteria(Criteria.where("orderStatus").is("FULFILLABLE"));
        return mongoOperations.find(query, OrderPojo.class);
    }

    public List<OrderPojo> findTodayFulfillableOrders() {
        Instant startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        System.out.println("START OF THE DAY: " + startOfDay);

        Instant endOfDay = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        Query query = new Query();
        query.addCriteria(
                Criteria.where("orderStatus").is("FULFILLABLE")
                        .and("orderTime").gte(startOfDay).lt(endOfDay)
        );

        return mongoOperations.find(query, OrderPojo.class);
    }


}