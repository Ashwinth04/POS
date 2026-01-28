package com.increff.pos.dao;

import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.ProductRow;
import org.bson.BsonNull;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository
public class SalesDao extends AbstractDao<SalesPojo> {
    public SalesDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(SalesPojo.class),
                mongoOperations
        );
    }

    public SalesPojo findByDate(ZonedDateTime date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("date").is(date));
        return mongoOperations.findOne(query, SalesPojo.class);
    }

    public List<ProductRow> getSalesReport(String clientName,
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

        AggregationResults<ProductRow> results =
                mongoOperations.aggregate(aggregation, "orders", ProductRow.class);

        return results.getMappedResults();
    }

    public SalesPojo getDailySalesData(ZonedDateTime start, ZonedDateTime end) {

        Date startDate = Date.from(start.toInstant());
        Date endDate = Date.from(end.toInstant());

        List<Document> pipeline = Arrays.asList(new Document("$match",
                        new Document("orderStatus", "FULFILLABLE")
                                .append("orderTime",
                                        new Document("$gte", startDate)
                                                .append("$lt", endDate))),
                new Document("$facet",
                        new Document("summary", Arrays.asList(new Document("$project",
                                        new Document("orderItems", 1L)
                                                .append("orderRevenue",
                                                        new Document("$sum",
                                                                new Document("$map",
                                                                        new Document("input", "$orderItems")
                                                                                .append("as", "item")
                                                                                .append("in",
                                                                                        new Document("$multiply", Arrays.asList("$$item.orderedQuantity", "$$item.sellingPrice"))))))
                                                .append("totalItemsInOrder",
                                                        new Document("$sum", "$orderItems.orderedQuantity"))),
                                new Document("$group",
                                        new Document("_id",
                                                new BsonNull())
                                                .append("totalOrders",
                                                        new Document("$sum", 1L))
                                                .append("totalProducts",
                                                        new Document("$sum", "$totalItemsInOrder"))
                                                .append("totalRevenue",
                                                        new Document("$sum", "$orderRevenue"))),
                                new Document("$project",
                                        new Document("_id", 0L))))
                                .append("clients", Arrays.asList(new Document("$unwind", "$orderItems"),
                                        new Document("$lookup",
                                                new Document("from", "products")
                                                        .append("localField", "orderItems.barcode")
                                                        .append("foreignField", "barcode")
                                                        .append("as", "product")),
                                        new Document("$unwind", "$product"),
                                        new Document("$group",
                                                new Document("_id", "$product.clientName")
                                                        .append("totalProducts",
                                                                new Document("$sum", "$orderItems.orderedQuantity"))
                                                        .append("totalRevenue",
                                                                new Document("$sum",
                                                                        new Document("$multiply", Arrays.asList("$orderItems.orderedQuantity", "$orderItems.sellingPrice"))))),
                                        new Document("$project",
                                                new Document("_id", 0L)
                                                        .append("clientName", "$_id")
                                                        .append("totalProducts", 1L)
                                                        .append("totalRevenue", 1L))))),
                new Document("$project",
                        new Document("totalOrders",
                                new Document("$arrayElemAt", Arrays.asList("$summary.totalOrders", 0L)))
                                .append("totalProducts",
                                        new Document("$arrayElemAt", Arrays.asList("$summary.totalProducts", 0L)))
                                .append("totalRevenue",
                                        new Document("$arrayElemAt", Arrays.asList("$summary.totalRevenue", 0L)))
                                .append("clients", 1L)));


        List<AggregationOperation> operations = pipeline.stream()
                .map(doc -> (AggregationOperation) context -> doc)
                .toList();

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<SalesPojo> result =
                mongoOperations.aggregate(aggregation, "orders", SalesPojo.class);

        return result.getUniqueMappedResult();
    }

}