package com.increff.pos.test.api;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiImplTest {

    @Mock
    private OrderDao orderDao;

    @InjectMocks
    private OrderApiImpl orderApi;

    // ---------- placeOrder ----------

    @Test
    void testPlaceOrder_fulfillable() {
        OrderPojo pojo = new OrderPojo();

        when(orderDao.save(pojo)).thenReturn(pojo);

        OrderPojo result =
                orderApi.placeOrder(pojo, true);

        assertThat(result.getOrderStatus()).isEqualTo("FULFILLABLE");
        verify(orderDao).save(pojo);
    }

    @Test
    void testPlaceOrder_unfulfillable() {
        OrderPojo pojo = new OrderPojo();

        when(orderDao.save(pojo)).thenReturn(pojo);

        OrderPojo result =
                orderApi.placeOrder(pojo, false);

        assertThat(result.getOrderStatus()).isEqualTo("UNFULFILLABLE");
    }

    // ---------- editOrder ----------

    @Test
    void testEditOrder_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("O1");

        OrderPojo existing = new OrderPojo();
        existing.setId("DB_ID");

        when(orderDao.findByOrderId("O1")).thenReturn(existing);
        when(orderDao.save(any(OrderPojo.class))).thenReturn(pojo);

        OrderPojo result =
                orderApi.editOrder(pojo, true);

        assertThat(result.getOrderStatus()).isEqualTo("FULFILLABLE");
        verify(orderDao).save(pojo);
    }

    // ---------- updateOrder ----------

    @Test
    void testUpdateOrder_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("O1");

        OrderPojo existing = new OrderPojo();
        existing.setId("DB_ID");

        when(orderDao.findByOrderId("O1")).thenReturn(existing);
        when(orderDao.save(pojo)).thenReturn(pojo);

        OrderPojo result =
                orderApi.updateOrder(pojo);

        assertThat(result).isEqualTo(pojo);
        assertThat(pojo.getId()).isEqualTo("DB_ID");
    }

    @Test
    void testUpdateOrder_notFound() {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("O1");

        when(orderDao.findByOrderId("O1")).thenReturn(null);

        assertThatThrownBy(() -> orderApi.updateOrder(pojo))
                .isInstanceOf(ApiException.class)
                .hasMessage("Order with the given id does not exist");
    }

    // ---------- cancelOrder ----------

    @Test
    void testCancelOrder_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("PLACED");

        when(orderDao.findByOrderId("O1")).thenReturn(pojo);

        MessageData result =
                orderApi.cancelOrder("O1");

        assertThat(pojo.getOrderStatus()).isEqualTo("CANCELLED");
        assertThat(result.getMessage())
                .isEqualTo("Order cancelled successfully!");
        verify(orderDao).save(pojo);
    }

    @Test
    void testCancelOrder_notFound() {
        when(orderDao.findByOrderId("O1")).thenReturn(null);

        assertThatThrownBy(() -> orderApi.cancelOrder("O1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Order with the given id does not exist");
    }

    // ---------- getAllOrders ----------

    @Test
    void testGetAllOrders_success() {
        Page<OrderPojo> page =
                new PageImpl<>(List.of(new OrderPojo()));

        when(orderDao.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<OrderPojo> result =
                orderApi.getAllOrders(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    // ---------- getOrderByOrderId ----------

    @Test
    void testGetOrderByOrderId_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();

        when(orderDao.findByOrderId("O1"))
                .thenReturn(pojo);

        OrderPojo result =
                orderApi.getOrderByOrderId("O1");

        assertThat(result).isEqualTo(pojo);
    }

    @Test
    void testGetOrderByOrderId_notFound() {
        when(orderDao.findByOrderId("O1"))
                .thenReturn(null);

        assertThatThrownBy(() -> orderApi.getOrderByOrderId("O1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("ORDER WITH THE GIVEN ID DOESN'T EXIST");
    }

    // ---------- updatePlacedStatus ----------

    @Test
    void testUpdatePlacedStatus_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("FULFILLABLE");

        when(orderDao.findByOrderId("O1"))
                .thenReturn(pojo);

        orderApi.updatePlacedStatus("O1");

        assertThat(pojo.getOrderStatus()).isEqualTo("PLACED");
        verify(orderDao).save(pojo);
    }

    // ---------- filterOrders ----------

    @Test
    void testFilterOrders_success() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        Page<OrderPojo> daoPage =
                new PageImpl<>(List.of(new OrderPojo()),
                        PageRequest.of(0, 10),
                        1);

        when(orderDao.findOrdersBetween(start, end, 0, 10))
                .thenReturn(daoPage);

        Page<OrderPojo> result =
                orderApi.filterOrders(start, end, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---------- getOrderStatus ----------

    @Test
    void testGetOrderStatus_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderStatus("PLACED");

        when(orderDao.findByOrderId("O1"))
                .thenReturn(pojo);

        String status =
                orderApi.getOrderStatus("O1");

        assertThat(status).isEqualTo("PLACED");
    }
}
