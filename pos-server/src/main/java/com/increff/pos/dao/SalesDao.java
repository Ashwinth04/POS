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

    public List<ProductRow> getSalesReport(String clientName,
                                           ZonedDateTime startDate,
                                           ZonedDateTime endDate) {

        MatchOperation dateMatch = match(
                Criteria.where("orderStatus").is("FULFILLABLE")
                        .and("orderTime").gte(startDate).lt(endDate)
        );

        UnwindOperation unwindItems = Aggregation.unwind("orderItems");

        LookupOperation lookupProduct = Aggregation.lookup(
                "products",
                "orderItems.barcode",
                "barcode",
                "product"
        );

        UnwindOperation unwindProduct = Aggregation.unwind("product");

        MatchOperation clientMatch = match(
                Criteria.where("product.clientName").is(clientName)
        );

        GroupOperation group = group("product.barcode")
                .sum("orderItems.orderedQuantity").as("quantity")
                .sum(
                        ArithmeticOperators.Multiply
                                .valueOf("orderItems.orderedQuantity")
                                .multiplyBy("orderItems.sellingPrice")
                ).as("revenue");

        ProjectionOperation project = project()
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

        // Match valid orders
        MatchOperation matchOrders = match(
                Criteria.where("orderStatus").is("FULFILLABLE")
                        .and("orderTime").gte(startDate).lt(endDate)
        );

        // Break orderItems into rows
        UnwindOperation unwindItems = unwind("orderItems");

        // Compute item-level revenue
        ProjectionOperation itemRevenueProjection = project()
                .andExpression("$_id").as("orderId")
                .and("orderItems.orderedQuantity").as("quantity")
                .and(
                        ArithmeticOperators.Multiply.valueOf("orderItems.orderedQuantity")
                                .multiplyBy("orderItems.sellingPrice")
                ).as("itemRevenue")
                .and("orderItems.barcode").as("barcode");


        // SUMMARY PIPELINE
        GroupOperation perOrderGroup = group("orderId")
                .sum("quantity").as("totalItemsInOrder")
                .sum("itemRevenue").as("orderRevenue");

        GroupOperation summaryGroup = group()
                .count().as("totalOrders")
                .sum("totalItemsInOrder").as("totalProducts")
                .sum("orderRevenue").as("totalRevenue");

        // CLIENT PIPELINE
        AggregationOperation lookupProduct = lookup(
                "products", "barcode", "barcode", "product"
        );

        UnwindOperation unwindProduct = unwind("product");

        GroupOperation clientGroup = group("product.clientName")
                .sum("quantity").as("totalProducts")
                .sum("itemRevenue").as("totalRevenue");

        ProjectionOperation clientProjection = project()
                .and("_id").as("clientName")
                .andInclude("totalProducts", "totalRevenue")
                .andExclude("_id");


        // FACET
        FacetOperation facet = facet(
                perOrderGroup,
                summaryGroup
        ).as("summary")
                .and(
                        lookupProduct,
                        unwindProduct,
                        clientGroup,
                        clientProjection
                ).as("clients");

        // FINAL SHAPE
        ProjectionOperation finalProjection = project()
                .and(ArrayOperators.ArrayElemAt.arrayOf("summary.totalOrders").elementAt(0))
                .as("totalOrders")
                .and(ArrayOperators.ArrayElemAt.arrayOf("summary.totalProducts").elementAt(0))
                .as("totalProducts")
                .and(ArrayOperators.ArrayElemAt.arrayOf("summary.totalRevenue").elementAt(0))
                .as("totalRevenue")
                .and("clients").as("clients");

        Aggregation aggregation = newAggregation(
                matchOrders,
                unwindItems,
                itemRevenueProjection,
                facet,
                finalProjection
        );

        return mongoOperations
                .aggregate(aggregation, "orders", SalesPojo.class)
                .getUniqueMappedResult();
    }



}