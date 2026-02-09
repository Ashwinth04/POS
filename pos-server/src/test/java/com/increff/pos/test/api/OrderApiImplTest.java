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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiImplTest {

    @Mock
    private OrderDao orderDao;

    @InjectMocks
    private OrderApiImpl orderApi;

    // ---------- createOrder ----------

    @Test
    void createOrder_fulfillable() {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");

        when(orderDao.save(any(OrderPojo.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderPojo result = orderApi.createOrder(pojo);

        assertEquals("FULFILLABLE", result.getOrderStatus());
        verify(orderDao).save(pojo);
    }

    @Test
    void createOrder_unfulfillable() {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o2");

        when(orderDao.save(any(OrderPojo.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderPojo result = orderApi.createOrder(pojo);

        assertEquals("UNFULFILLABLE", result.getOrderStatus());
    }

    // ---------- editOrder ----------

    @Test
    void editOrder_success() throws ApiException {
        OrderPojo existing = new OrderPojo();
        existing.setId("10");
        existing.setOrderId("o1");

        OrderPojo updated = new OrderPojo();
        updated.setOrderId("o1");

        when(orderDao.findByOrderId("o1")).thenReturn(existing);
        when(orderDao.save(any(OrderPojo.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderPojo result = orderApi.editOrder(updated);

        assertEquals("FULFILLABLE", result.getOrderStatus());
        assertEquals("10", result.getId());
    }

    @Test
    void editOrder_notFound() {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("missing");

        when(orderDao.findByOrderId("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderApi.editOrder(pojo));

        assertEquals("ORDER WITH THE GIVEN ID DOESN'T EXIST", ex.getMessage());
    }

    // ---------- cancelOrder ----------

    @Test
    void cancelOrder_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");
        pojo.setOrderStatus("PLACED");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);

        MessageData result = orderApi.cancelOrder("o1");

        assertEquals("Order cancelled successfully!", result.getMessage());
        assertEquals("CANCELLED", pojo.getOrderStatus());
        verify(orderDao).save(pojo);
    }

    // ---------- getAllOrders ----------

    @Test
    void getAllOrders_success() {
        Page<OrderPojo> page = new PageImpl<>(List.of(new OrderPojo()));
        when(orderDao.findAll(any(PageRequest.class))).thenReturn(page);

        Page<OrderPojo> result = orderApi.getAllOrders(0, 5);

        assertEquals(1, result.getContent().size());
    }

    // ---------- getCheckByOrderId ----------

    @Test
    void getCheckByOrderId_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);

        OrderPojo result = orderApi.getCheckByOrderId("o1");

        assertEquals("o1", result.getOrderId());
    }

    @Test
    void getCheckByOrderId_notFound() {
        when(orderDao.findByOrderId("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderApi.getCheckByOrderId("missing"));

        assertEquals("ORDER WITH THE GIVEN ID DOESN'T EXIST", ex.getMessage());
    }

    // ---------- updatePlacedStatus ----------

    @Test
    void updatePlacedStatus_success() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");
        pojo.setOrderStatus("FULFILLABLE");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);

        orderApi.updatePlacedStatus("o1");

        assertEquals("PLACED", pojo.getOrderStatus());
        verify(orderDao).save(pojo);
    }

    @Test
    void updatePlacedStatus_nullOrder() {
        when(orderDao.findByOrderId("missing")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderApi.updatePlacedStatus("missing"));

        assertEquals("ORDER WITH THE GIVEN ID DOESN'T EXIST", ex.getMessage());
    }

    // ---------- filterOrders ----------

    @Test
    void filterOrders_success() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        OrderPojo pojo = new OrderPojo();
        Page<OrderPojo> page =
                new PageImpl<>(List.of(pojo), PageRequest.of(0, 10), 1);

        when(orderDao.findOrdersBetween(start, end, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderId"))))
                .thenReturn(page);

        Page<OrderPojo> result =
                orderApi.filterOrders(start, end, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

}
