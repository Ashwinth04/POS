package com.increff.pos.test.api;

import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.documents.OrderPojo;
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
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- saveOrder ----------
    @Test
    void testSaveOrder() {
        OrderPojo pojo = new OrderPojo();
        when(orderDao.save(pojo)).thenReturn(pojo);

        OrderPojo result = orderApi.saveOrder(pojo);

        assertEquals(pojo, result);
        verify(orderDao).save(pojo);
    }

    // ---------- editOrder ----------
    @Test
    void testEditOrderSuccess() throws Exception {
        OrderPojo existing = new OrderPojo();
        existing.setOrderId("1");

        OrderPojo update = new OrderPojo();
        update.setOrderId("1");
        update.setOrderItems(List.of());
        update.setOrderStatus("UPDATED");

        when(orderDao.findByOrderId("1")).thenReturn(existing);
        when(orderDao.save(existing)).thenReturn(existing);

        OrderPojo result = orderApi.editOrder(update);

        assertEquals("UPDATED", existing.getOrderStatus());
        assertEquals(existing, result);
    }

    @Test
    void testEditOrderNotFound() {
        when(orderDao.findByOrderId("1")).thenReturn(null);

        OrderPojo update = new OrderPojo();
        update.setOrderId("1");

        assertThrows(ApiException.class,
                () -> orderApi.editOrder(update));
    }

    // ---------- cancelOrder ----------
    @Test
    void testCancelOrderSuccess() throws Exception {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("1");

        when(orderDao.findByOrderId("1")).thenReturn(pojo);

        MessageData result = orderApi.cancelOrder("1");

        assertEquals("CANCELLED", pojo.getOrderStatus());
        assertEquals("Order cancelled successfully!", result.getMessage());
        verify(orderDao).save(pojo);
    }

    // ---------- getAllOrders ----------
    @Test
    void testGetAllOrders() {
        Page<OrderPojo> page = new PageImpl<>(List.of(new OrderPojo()));
        when(orderDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<OrderPojo> result = orderApi.getAllOrders(0,10);

        assertEquals(1, result.getContent().size());
        verify(orderDao).findAll(any(Pageable.class));
    }

    // ---------- getCheckByOrderId ----------
    @Test
    void testGetCheckByOrderIdSuccess() throws Exception {
        OrderPojo pojo = new OrderPojo();
        when(orderDao.findByOrderId("1")).thenReturn(pojo);

        OrderPojo result = orderApi.getCheckByOrderId("1");

        assertEquals(pojo, result);
    }

    @Test
    void testGetCheckByOrderIdThrows() {
        when(orderDao.findByOrderId("1")).thenReturn(null);

        assertThrows(ApiException.class,
                () -> orderApi.getCheckByOrderId("1"));
    }

    // ---------- updatePlacedStatus ----------
    @Test
    void testUpdatePlacedStatus() throws Exception {
        OrderPojo pojo = new OrderPojo();
        pojo.setOrderId("1");

        when(orderDao.findByOrderId("1")).thenReturn(pojo);

        orderApi.updatePlacedStatus("1");

        assertEquals("PLACED", pojo.getOrderStatus());
        verify(orderDao).save(pojo);
    }

    // ---------- filterOrdersByDate ----------
    @Test
    void testFilterOrdersByDate() {
        Page<OrderPojo> page =
                new PageImpl<>(List.of(new OrderPojo()));

        when(orderDao.findOrdersBetweenDates(any(), any(), any()))
                .thenReturn(page);

        Page<OrderPojo> result =
                orderApi.filterOrdersByDate(
                        ZonedDateTime.now().minusDays(1),
                        ZonedDateTime.now(),
                        0,
                        10
                );

        assertEquals(1, result.getContent().size());
    }

    // ---------- searchById ----------
    @Test
    void testSearchById() throws Exception {
        Page<OrderPojo> page =
                new PageImpl<>(List.of(new OrderPojo()));

        when(orderDao.searchById(eq("1"), any()))
                .thenReturn(page);

        Page<OrderPojo> result =
                orderApi.searchById("1",0,10);

        assertEquals(page, result);
    }
}
