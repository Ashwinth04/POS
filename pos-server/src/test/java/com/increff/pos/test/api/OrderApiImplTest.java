package com.increff.pos.test.api;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderApiImplTest {

    @InjectMocks
    private OrderApiImpl orderApi;

    @Mock
    private OrderDao orderDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- placeOrder ----------
    @Test
    void shouldPlaceOrderAsFulfillable() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");

        OrderPojo saved = new OrderPojo();
        saved.setOrderId("o1");

        when(orderDao.save(pojo)).thenReturn(saved);

        OrderPojo result = orderApi.placeOrder(pojo, true);

        assertEquals("FULFILLABLE", pojo.getOrderStatus());
        assertEquals(saved, result);
        verify(orderDao).save(pojo);
    }

    // ---------- editOrder ----------
    @Test
    void shouldEditOrder() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");

        OrderPojo existing = new OrderPojo();
        existing.setOrderId("o1");
        existing.setId("232rfe");

        when(orderDao.findByOrderId("o1")).thenReturn(existing);
        when(orderDao.save(any(OrderPojo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderPojo result = orderApi.editOrder(pojo, false);

        assertEquals("UNFULFILLABLE", pojo.getOrderStatus());
        assertEquals(existing.getId(), pojo.getId());
        verify(orderDao).save(pojo);
    }

    @Test
    void shouldThrowExceptionWhenEditingNonExistingOrder() {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o2");

        when(orderDao.findByOrderId("o2")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> orderApi.editOrder(pojo, true));

        assertEquals("Order with the given id does not exist", exception.getMessage());
    }

    // ---------- cancelOrder ----------
    @Test
    void shouldCancelOrder() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);
        when(orderDao.save(any(OrderPojo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageData message = orderApi.cancelOrder("o1");

        assertEquals("CANCELLED", pojo.getOrderStatus());
        assertEquals("Order cancelled successfully!", message.getMessage());
        verify(orderDao).save(pojo);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistingOrder() {
        when(orderDao.findByOrderId("o2")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> orderApi.cancelOrder("o2"));

        assertEquals("Order with the given id does not exist", exception.getMessage());
    }

    // ---------- getOrderByOrderId ----------
    @Test
    void shouldGetOrderByOrderId() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);

        OrderPojo result = orderApi.getOrderByOrderId("o1");

        assertEquals(pojo, result);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderDao.findByOrderId("o2")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> orderApi.getOrderByOrderId("o2"));

        assertEquals("ORDER WITH THE GIVEN ID DOESN'T EXIST", exception.getMessage());
    }

    // ---------- updatePlacedStatus ----------
    @Test
    void shouldUpdatePlacedStatus() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");
        pojo.setOrderStatus("FULFILLABLE");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);
        when(orderDao.save(any(OrderPojo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderApi.updatePlacedStatus("o1");

        assertEquals("PLACED", pojo.getOrderStatus());
        verify(orderDao).save(pojo);
    }

    // ---------- getAllOrders ----------
    @Test
    void shouldGetAllOrders() {
        OrderPojo pojo1 = new OrderPojo();
        OrderPojo pojo2 = new OrderPojo();

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "orderTime"));
        Page<OrderPojo> page = new PageImpl<>(List.of(pojo1, pojo2), pageable, 2);

        when(orderDao.findAll(pageable)).thenReturn(page);

        Page<OrderPojo> result = orderApi.getAllOrders(0, 2);

        assertEquals(2, result.getTotalElements());
        verify(orderDao).findAll(pageable);
    }

    // ---------- filterOrders ----------
    @Test
    void shouldFilterOrders() {
        OrderPojo pojo = new OrderPojo();
        Pageable pageable = PageRequest.of(0, 1);
        Page<OrderPojo> page = new PageImpl<>(List.of(pojo), pageable, 1);

        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        when(orderDao.findOrdersBetween(start, end, 0, 1)).thenReturn(page);

        Page<OrderPojo> result = orderApi.filterOrders(start, end, 0, 1);

        assertEquals(1, result.getTotalElements());
        verify(orderDao).findOrdersBetween(start, end, 0, 1);
    }

    // ---------- getOrderStatus ----------
    @Test
    void shouldGetOrderStatus() throws ApiException {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("o1");
        pojo.setOrderStatus("FULFILLABLE");

        when(orderDao.findByOrderId("o1")).thenReturn(pojo);

        String status = orderApi.getOrderStatus("o1");

        assertEquals("FULFILLABLE", status);
    }
}
