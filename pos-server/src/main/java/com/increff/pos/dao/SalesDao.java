package com.increff.pos.dao;

import ch.qos.logback.core.net.server.Client;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.SalesReportRow;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
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

    public List<SalesReportRow> getSalesReport(String clientName,
                                               ZonedDateTime startDate,
                                               ZonedDateTime endDate) {

        MatchOperation dateMatch = Aggregation.match(
                Criteria.where("orderTime")
                        .gte(Date.from(startDate.toInstant()))
                        .lte(Date.from(endDate.toInstant()))
        );

        UnwindOperation unwindItems = Aggregation.unwind("orderItems");

        LookupOperation lookupProduct = Aggregation.lookup(
                "products",
                "orderItems.barcode",
                "barcode",
                "product"
        );

        UnwindOperation unwindProduct = Aggregation.unwind("product");

        MatchOperation clientMatch = Aggregation.match(
                Criteria.where("product.clientName").is(clientName)
        );

        GroupOperation group = Aggregation.group("product.barcode")
                .sum("orderItems.orderedQuantity").as("quantity")
                .sum(
                        ArithmeticOperators.Multiply
                                .valueOf("orderItems.orderedQuantity")
                                .multiplyBy("orderItems.sellingPrice")
                ).as("revenue");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("product")
                .and("quantity").as("quantity")
                .and("revenue").as("revenue")
                .andExclude("_id");

        Aggregation aggregation = Aggregation.newAggregation(
                dateMatch,
                unwindItems,
                lookupProduct,
                unwindProduct,
                clientMatch,
                group,
                project
        );

        AggregationResults<SalesReportRow> results =
                mongoOperations.aggregate(aggregation, "orders", SalesReportRow.class);

        return results.getMappedResults();
    }
}