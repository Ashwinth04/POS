package com.increff.pos.dao;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
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
        String barcode = inventoryPojo.getBarcode();
        int quantity = inventoryPojo.getQuantity();

        Query query = Query.query(Criteria.where("barcode").is(barcode));
        Update update = new Update().inc("quantity", quantity);

        UpdateResult result = mongoOperations.updateFirst(query, update, InventoryPojo.class);

        if (result.getMatchedCount() == 0) {
            throw new ApiException("No matching product found for the given barcode");
        }
    }

    public int getQuantity(String productId) throws ApiException {
        Query query = Query.query(Criteria.where("productId").is(productId));

        return mongoOperations.findOne(query,InventoryPojo.class).getQuantity();
    }

    @Override
    public Page<InventoryPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}