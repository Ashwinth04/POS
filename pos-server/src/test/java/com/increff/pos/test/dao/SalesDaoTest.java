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
    // getSalesReport()
    // ------------------------------------------------

    @Test
    void testGetSalesReport_success() {

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
                        "orderTime", new Date(),
                        "orderItems", List.of(
                                Map.of(
                                        "barcode", "B1",
                                        "orderedQuantity", 2,
                                        "sellingPrice", 50.0
                                )
                        )
                )),
                "orders"
        );

        ZonedDateTime now = ZonedDateTime.now();

        List<ProductRow> rows =
                salesDao.getSalesReport(
                        "ClientA",
                        now.minusDays(1),
                        now.plusDays(1)
                );

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getProduct()).isEqualTo("B1");
        assertThat(rows.get(0).getQuantity()).isEqualTo(2);
        assertThat(rows.get(0).getRevenue()).isEqualTo(100.0);
    }

    @Test
    void testGetSalesReport_noData() {

        ZonedDateTime now = ZonedDateTime.now();

        List<ProductRow> rows =
                salesDao.getSalesReport(
                        "ClientA",
                        now.minusDays(1),
                        now.plusDays(1)
                );

        assertThat(rows).isEmpty();
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
        assertThat(result.getTotalOrders()).isEqualTo(1);
        assertThat(result.getTotalProducts()).isEqualTo(3);
        assertThat(result.getTotalRevenue()).isEqualTo(60.0);
        assertThat(result.getClients()).hasSize(1);
    }
}
