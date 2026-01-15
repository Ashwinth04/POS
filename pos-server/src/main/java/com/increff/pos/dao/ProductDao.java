package com.increff.pos.dao;

import com.increff.pos.db.ProductPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDao extends AbstractDao<ProductPojo> {
    public ProductDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(ProductPojo.class),
                mongoOperations
        );
    }

    public ProductPojo findByProductId(String productId) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        return mongoOperations.findOne(query, ProductPojo.class);
    }

    @Override
    public Page<ProductPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}