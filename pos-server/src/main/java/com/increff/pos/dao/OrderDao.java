package com.increff.pos.dao;

import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.StyleSheet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class OrderDao extends AbstractDao<OrderPojo> {
    public OrderDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(OrderPojo.class),
                mongoOperations
        );
    }

    public OrderPojo findByOrderId(String orderId) {
        Query query = Query.query(Criteria.where("orderId").is(orderId));
        return mongoOperations.findOne(query, OrderPojo.class);
    }

    public Page<OrderPojo> findOrdersBetween(ZonedDateTime start, ZonedDateTime end, PageRequest pageable) {

        Query query = new Query();

        query.addCriteria(
                Criteria.where("orderTime")
                        .gte(start.toInstant())
                        .lte(end.toInstant())
        );

        long total = mongoOperations.count(query, OrderPojo.class);
        query.with(pageable);

        List<OrderPojo> orders = mongoOperations.find(query, OrderPojo.class);

        return new PageImpl<>(
                orders,
                pageable,
                total
        );
    }

    public Page<OrderPojo> search(String orderId, PageRequest pageable) throws ApiException {

        String pattern = ".*" + Pattern.quote(orderId) + ".*";

        Query query = new Query(Criteria.where("orderId").regex(pattern, "i"));

        long total = mongoOperations.count(query, OrderPojo.class);
        query.with(pageable);

        List<OrderPojo> results = mongoOperations.find(query, OrderPojo.class);

        return new PageImpl<>(results, pageable, total);
    }

}