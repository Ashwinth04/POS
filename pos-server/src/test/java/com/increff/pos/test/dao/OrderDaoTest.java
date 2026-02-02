package com.increff.pos.test.dao;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.OrderPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class OrderDaoTest {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private OrderDao orderDao;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(OrderPojo.class);
        seedOrders();
    }

    // ------------------------------------------------
    // SEED DATA
    // ------------------------------------------------

    private void seedOrders() {

        ZonedDateTime now = ZonedDateTime.now();

        insertOrder("O1", now.minusHours(1));
        insertOrder("O2", now.minusHours(2));
        insertOrder("O3", now.minusHours(3));
        insertOrder("O4", now.minusHours(4));
        insertOrder("O5", now.minusHours(5));
    }

    private void insertOrder(String orderId, ZonedDateTime orderTime) {
        OrderPojo order = new OrderPojo();
        order.setOrderId(orderId);
        order.setOrderTime(orderTime.toInstant());
        order.setOrderStatus("FULFILLABLE");

        mongoOperations.insert(order);
    }

    // ------------------------------------------------
    // TESTS
    // ------------------------------------------------

    @Test
    void testFindByOrderId_found() {

        OrderPojo order = orderDao.findByOrderId("O3");

        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isEqualTo("O3");
    }

    @Test
    void testFindByOrderId_notFound() {

        OrderPojo order = orderDao.findByOrderId("INVALID");

        assertThat(order).isNull();
    }

    @Test
    void testFindOrdersBetween_singlePage() {

        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now().plusDays(1);

        Page<OrderPojo> page =
                orderDao.findOrdersBetween(start, end, 0, 10);

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(5);

        // sorted DESC by orderTime
        List<OrderPojo> orders = page.getContent();

        assertThat(orders.get(0).getOrderTime())
                .isAfter(orders.get(1).getOrderTime());
    }

    @Test
    void testFindOrdersBetween_withPagination() {

        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now().plusDays(1);

        Page<OrderPojo> page1 =
                orderDao.findOrdersBetween(start, end, 0, 2);

        Page<OrderPojo> page2 =
                orderDao.findOrdersBetween(start, end, 1, 2);

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);

        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page2.getTotalElements()).isEqualTo(5);

        // ensure no overlap
        assertThat(
                page1.getContent().get(0).getOrderId()
        ).isNotEqualTo(
                page2.getContent().get(0).getOrderId()
        );
    }

    @Test
    void testFindOrdersBetween_emptyResult() {

        ZonedDateTime start = ZonedDateTime.now().minusDays(10);
        ZonedDateTime end = ZonedDateTime.now().minusDays(5);

        Page<OrderPojo> page =
                orderDao.findOrdersBetween(start, end, 0, 5);

        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void testFindOrdersBetween_boundaryInclusive() {

        ZonedDateTime exactTime = ZonedDateTime.now().minusHours(1);

        insertOrder("BOUNDARY", exactTime);

        Page<OrderPojo> page =
                orderDao.findOrdersBetween(
                        exactTime,
                        exactTime,
                        0,
                        10
                );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getOrderId())
                .isEqualTo("BOUNDARY");
    }
}
