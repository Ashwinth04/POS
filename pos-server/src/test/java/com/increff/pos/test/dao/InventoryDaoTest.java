package com.increff.pos.test.dao;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.mongodb.bulk.BulkWriteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class InventoryDaoTest {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private InventoryDao inventoryDao;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(InventoryPojo.class);
        seedInventory();
    }

    // ------------------------------------------------
    // SEED DATA
    // ------------------------------------------------

    private void seedInventory() {
        insertInventory("P1", 10);
        insertInventory("P2", 20);
    }

    private void insertInventory(String productId, int quantity) {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(productId);
        pojo.setQuantity(quantity);
        mongoOperations.insert(pojo);
    }

    // ------------------------------------------------
    // updateBulkInventory()
    // ------------------------------------------------

    @Test
    void testUpdateInventory_success() throws ApiException {

        InventoryPojo update = new InventoryPojo();
        update.setProductId("P1");
        update.setQuantity(5);

        inventoryDao.updateInventory(update);

        InventoryPojo updated =
                mongoOperations.findOne(
                        Query.query(Criteria.where("productId").is("P1")),
                        InventoryPojo.class
                );

        assertThat(updated).isNotNull();
        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    void testUpdateInventory_noMatchingProduct() {

        InventoryPojo update = new InventoryPojo();
        update.setProductId("INVALID");
        update.setQuantity(5);

        assertThatThrownBy(() -> inventoryDao.updateInventory(update))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No matching product found");
    }

    // ------------------------------------------------
    // bulkUpdate()
    // ------------------------------------------------

    @Test
    void testBulkUpdate_nullInput() {
        BulkWriteResult result = inventoryDao.bulkUpdate(null);
        assertThat(result).isNull();
    }

    @Test
    void testBulkUpdate_emptyInput() {
        BulkWriteResult result = inventoryDao.bulkUpdate(List.of());
        assertThat(result).isNull();
    }

    @Test
    void testBulkUpdate_success() {

        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("P1");
        p1.setQuantity(5);

        InventoryPojo p2 = new InventoryPojo();
        p2.setProductId("P2");
        p2.setQuantity(10);

        BulkWriteResult result =
                inventoryDao.bulkUpdate(List.of(p1, p2));

        assertThat(result).isNotNull();
        assertThat(result.getModifiedCount()).isEqualTo(2);

        InventoryPojo updatedP1 =
                mongoOperations.findOne(
                        org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where("productId").is("P1")
                        ),
                        InventoryPojo.class
                );

        InventoryPojo updatedP2 =
                mongoOperations.findOne(
                        org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where("productId").is("P2")
                        ),
                        InventoryPojo.class
                );

        assertThat(updatedP1.getQuantity()).isEqualTo(15);
        assertThat(updatedP2.getQuantity()).isEqualTo(30);
    }

    // ------------------------------------------------
    // findByProductIds()
    // ------------------------------------------------

    @Test
    void testFindByProductIds() {

        List<InventoryPojo> result =
                inventoryDao.findByProductIds(List.of("P1", "P2"));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(InventoryPojo::getProductId)
                .containsExactlyInAnyOrder("P1", "P2");
    }
}
