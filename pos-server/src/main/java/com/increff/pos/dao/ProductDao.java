package com.increff.pos.dao;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ProductDao extends AbstractDao<ProductPojo> {
    public ProductDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(ProductPojo.class),
                mongoOperations
        );
    }

    public ProductPojo findByBarcode(String barcode) {
        Query query = Query.query(Criteria.where("barcode").is(barcode));
        return mongoOperations.findOne(query, ProductPojo.class);
    }

    public List<ProductPojo> findByBarcodes(List<String> barcodes) {

        Query query = new Query(
                Criteria.where("barcode").in(barcodes)
        );

        return mongoOperations.find(query, ProductPojo.class);
    }

}