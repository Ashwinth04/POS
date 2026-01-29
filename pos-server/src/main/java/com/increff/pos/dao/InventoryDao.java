package com.increff.pos.dao;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {
    public InventoryDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(InventoryPojo.class),
                mongoOperations
        );
    }

    BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, InventoryPojo.class);

    public void updateInventory(InventoryPojo inventoryPojo) throws ApiException {
        String barcode = inventoryPojo.getBarcode();
        int quantity = inventoryPojo.getQuantity();

        Query query = Query.query(Criteria.where("barcode").is(barcode));
        Update update = new Update().inc("quantity", quantity);

        UpdateResult result = mongoOperations.updateFirst(query, update, InventoryPojo.class);

        if (result.getMatchedCount() == 0) {
            throw new ApiException("No matching product found for the given barcode");
        }
    }

    public int getQuantity(String barcode) throws ApiException {
        Query query = Query.query(Criteria.where("barcode").is(barcode));

        InventoryPojo pojo = mongoOperations.findOne(query, InventoryPojo.class);

        if (pojo == null) {
            throw new ApiException("Inventory not found for barcode: " + barcode);
        }

        return pojo.getQuantity();
    }

    public BulkWriteResult bulkUpdate(List<InventoryPojo> validPojos) {
        BulkOperations bulkOps = mongoOperations.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                InventoryPojo.class
        );

        for (InventoryPojo pojo : validPojos) {
            Query query = Query.query(Criteria.where("barcode").is(pojo.getBarcode()));
            Update update = new Update().inc("quantity", pojo.getQuantity());

            bulkOps.updateOne(query, update);
        }

        return bulkOps.execute();
    }

    public List<String> findExistingBarcodes(List<String> barcodes) {

        if (barcodes == null || barcodes.isEmpty()) {
            return List.of();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("barcode").in(barcodes));

        query.fields().include("barcode");

        List<InventoryPojo> records = mongoOperations.find(query, InventoryPojo.class);

        return records.stream()
                .map(InventoryPojo::getBarcode)
                .toList();
    }

    public List<InventoryPojo> findByBarcodes(List<String> barcodes) {
        Query query = new Query(
                Criteria.where("barcode").in(barcodes)
        );

        return mongoOperations.find(query, InventoryPojo.class);
    }

}