package com.increff.pos.test.dao;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.SalesDao;
import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.ProductRow;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class SalesDaoTest {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private SalesDao salesDao;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection("orders");
        mongoOperations.dropCollection("products");
        mongoOperations.dropCollection(SalesPojo.class);
    }

    // ------------------------------------------------
    // findByDate()
    // ------------------------------------------------

    @Test
    void testFindByDate_notFound() {
        SalesPojo result = salesDao.findByDate(ZonedDateTime.now());
        assertThat(result).isNull();
    }

    // ------------------------------------------------
    // getDailySalesData()
    // ------------------------------------------------

    @Test
    void testGetDailySalesData_success() {

        // product
        mongoOperations.insert(
                new Document(Map.of(
                        "barcode", "B1",
                        "clientName", "ClientA"
                )),
                "products"
        );

        // order
        mongoOperations.insert(
                new Document(Map.of(
                        "orderStatus", "FULFILLABLE",
                        "orderTime", new Date(),
                        "orderItems", List.of(
                                Map.of(
                                        "barcode", "B1",
                                        "orderedQuantity", 3,
                                        "sellingPrice", 20.0
                                )
                        )
                )),
                "orders"
        );

        ZonedDateTime now = ZonedDateTime.now();

        SalesPojo result =
                salesDao.getDailySalesData(
                        now.minusDays(1),
                        now.plusDays(1)
                );

        assertThat(result).isNotNull();
    }
}
