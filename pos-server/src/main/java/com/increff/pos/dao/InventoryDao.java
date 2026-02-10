package com.increff.pos.dao;

import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
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

    public void updateInventory(InventoryPojo inventoryPojo) throws ApiException {

        String productId = inventoryPojo.getProductId();
        int quantity = inventoryPojo.getQuantity();

        Query query = Query.query(Criteria.where("productId").is(productId));
        Update update = new Update().set("quantity", quantity);
        UpdateResult result = mongoOperations.updateFirst(query, update, InventoryPojo.class);

        if (result.getMatchedCount() == 0) {
            throw new ApiException("No matching product found for the given productId");
        }
    }

    public BulkWriteResult bulkUpdate(List<InventoryPojo> validPojos) {

        BulkOperations bulkOps = mongoOperations.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                InventoryPojo.class
        );

        for (InventoryPojo pojo : validPojos) {
            Query query = Query.query(Criteria.where("productId").is(pojo.getProductId()));
            Update update = new Update().inc("quantity", pojo.getQuantity());

            bulkOps.updateOne(query, update);
        }
        return bulkOps.execute();
    }

    public List<InventoryPojo> findByProductIds(List<String> productIds) {

        Query query = new Query(
                Criteria.where("productId").in(productIds)
        );
        return mongoOperations.find(query, InventoryPojo.class);
    }
}