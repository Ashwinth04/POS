package com.increff.pos.dao;

import com.increff.pos.db.documents.SalesPojo;
import com.increff.pos.model.data.ProductRevenueRow;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

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

    public List<ProductRevenueRow> getSalesReport(String clientName,
                                                  ZonedDateTime startDate,
                                                  ZonedDateTime endDate) {

        MatchOperation dateMatch = match(
                Criteria.where("orderStatus").is("PLACED")
                        .and("orderTime").gte(startDate).lt(endDate)
        );

        UnwindOperation unwindItems = unwind("orderItems");

        AggregationOperation addProductIdObj = context ->
                new org.bson.Document("$addFields",
                        new org.bson.Document("orderItems.productIdObj",
                                new org.bson.Document("$toObjectId", "$orderItems.productId")));

        LookupOperation lookupProduct = lookup(
                "products",
                "orderItems.productIdObj",
                "_id",
                "product"
        );

        UnwindOperation unwindProduct = unwind("product");

        MatchOperation clientMatch = match(
                Criteria.where("product.clientName").is(clientName)
        );

        GroupOperation group = group("product.barcode")
                .sum("orderItems.orderedQuantity").as("quantity")
                .sum(
                        ArithmeticOperators.Multiply.valueOf("orderItems.orderedQuantity")
                                .multiplyBy("orderItems.sellingPrice")
                ).as("revenue");

        ProjectionOperation project = project()
                .andExclude("_id")
                .and("_id").as("product")
                .and("quantity").as("quantity")
                .and("revenue").as("revenue");

        Aggregation aggregation = newAggregation(
                dateMatch,
                unwindItems,
                addProductIdObj,
                lookupProduct,
                unwindProduct,
                clientMatch,
                group,
                project
        );

        AggregationResults<ProductRevenueRow> results =
                mongoOperations.aggregate(aggregation, "orders", ProductRevenueRow.class);

        return results.getMappedResults();
    }

    public SalesPojo getDailySalesData(ZonedDateTime start, ZonedDateTime end) {
        MatchOperation filterOrders = match(
                Criteria.where("orderStatus").is("PLACED")
                        .and("orderTime").gte(start).lt(end)
        );

        // Summary Steps
        ProjectionOperation summaryProject = project()
                .and(AccumulatorOperators.Sum.sumOf(
                        VariableOperators.Map.itemsOf("orderItems")
                                .as("item")
                                .andApply(ArithmeticOperators.Multiply.valueOf("$$item.orderedQuantity")
                                        .multiplyBy("$$item.sellingPrice"))
                )).as("orderRevenue")
                .and(AccumulatorOperators.Sum.sumOf("orderItems.orderedQuantity")).as("totalItemsInOrder");

        GroupOperation summaryGroup = group()
                .count().as("totalOrders")
                .sum("totalItemsInOrder").as("totalProducts")
                .sum("orderRevenue").as("totalRevenue");

        // Client Steps
        UnwindOperation unwindItems = unwind("orderItems");

        AggregationOperation addProductIdObj = context ->
                new org.bson.Document("$addFields",
                        new org.bson.Document("orderItems.productIdObj",
                                new org.bson.Document("$toObjectId", "$orderItems.productId")));

        LookupOperation lookupProduct = lookup("products", "orderItems.productIdObj", "_id", "product");
        UnwindOperation unwindProduct = unwind("product");

        GroupOperation clientGroup = group("product.clientName")
                .sum("orderItems.orderedQuantity").as("totalProducts")
                .sum(ArithmeticOperators.Multiply.valueOf("orderItems.orderedQuantity")
                        .multiplyBy("orderItems.sellingPrice")).as("totalRevenue");

        ProjectionOperation clientProject = project()
                .and("_id").as("clientName")
                .and("totalProducts").as("totalProducts")
                .and("totalRevenue").as("totalRevenue")
                .andExclude("_id");

        FacetOperation facetOperation = facet()
                .and(summaryProject, summaryGroup, project().andExclude("_id")).as("summary")
                .and(unwindItems, addProductIdObj, lookupProduct, unwindProduct, clientGroup, clientProject).as("clients");

        ProjectionOperation finalProject = project()
                .and("summary.totalOrders").arrayElementAt(0).as("totalOrders")
                .and("summary.totalProducts").arrayElementAt(0).as("totalProducts")
                .and("summary.totalRevenue").arrayElementAt(0).as("totalRevenue")
                .and("clients").as("clients");

        Aggregation aggregation = newAggregation(
                filterOrders,
                facetOperation,
                finalProject
        );

        AggregationResults<SalesPojo> results = mongoOperations.aggregate(aggregation, "orders", SalesPojo.class);
        return results.getUniqueMappedResult();
    }
}