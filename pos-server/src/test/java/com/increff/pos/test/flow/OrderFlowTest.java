package com.increff.pos.test.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.data.MessageData;
import com.increff.pos.model.data.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderFlowTest {

    @InjectMocks
    private OrderFlow orderFlow;

    @Mock
    private OrderApiImpl orderApi;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private InventoryApiImpl inventoryApi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- createOrder ----------------
    @Test
    void shouldCreateOrderAndReserveInventory() throws ApiException {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setOrderedQuantity(5);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        InventoryPojo inv = new InventoryPojo();
        inv.setProductId("p1");
        inv.setQuantity(5);

        when(productApi.mapBarcodesToProductIds(List.of("b1"))).thenReturn(Map.of("b1", "p1"));
        when(inventoryApi.reserveInventory(anyList())).thenReturn(true);
        when(orderApi.placeOrder(order, true)).thenReturn(order);

        OrderPojo result = orderFlow.createOrder(order);

        assertNotNull(result);
        verify(productApi).mapBarcodesToProductIds(List.of("b1"));
        verify(inventoryApi).reserveInventory(anyList());
        verify(orderApi).placeOrder(order, true);
    }

    // ---------------- editOrder ----------------
    @Test
    void shouldEditOrderAndUpdateInventory() throws ApiException {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setOrderedQuantity(5);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        when(orderApi.getOrderStatus("o1")).thenReturn("FULFILLABLE");
        when(productApi.mapBarcodesToProductIds(List.of("b1"))).thenReturn(Map.of("b1", "p1"));
        when(inventoryApi.checkOrderFulfillable(anyList())).thenReturn(true);
        when(orderApi.getOrderByOrderId("o1")).thenReturn(order);
        doNothing().when(inventoryApi).editOrder(anyMap(), anyMap());
        when(orderApi.editOrder(order, true)).thenReturn(order);

        OrderPojo result = orderFlow.editOrder(order, "o1");

        assertNotNull(result);
        verify(inventoryApi).editOrder(anyMap(), anyMap());
        verify(orderApi).editOrder(order, true);
    }

    @Test
    void shouldThrowExceptionIfOrderNotEditable() {
        ApiException ex1 = assertThrows(ApiException.class, () -> orderFlow.checkOrderEditable("PLACED"));
        assertEquals("PLACED ORDERS CANNOT BE EDITED", ex1.getMessage());

        ApiException ex2 = assertThrows(ApiException.class, () -> orderFlow.checkOrderEditable("CANCELLED"));
        assertEquals("CANCELLED ORDERS CANNOT BE EDITED", ex2.getMessage());
    }

    // ---------------- cancelOrder ----------------
    @Test
    void shouldCancelFulfillableOrderAndRevertInventory() throws ApiException {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setOrderedQuantity(5);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        when(orderApi.getOrderByOrderId("o1")).thenReturn(order);
        when(orderApi.getOrderStatus("o1")).thenReturn("FULFILLABLE");
        doNothing().when(inventoryApi).revertInventory(anyList());
        when(orderApi.cancelOrder("o1")).thenReturn(new MessageData("Cancelled"));

        MessageData result = orderFlow.cancelOrder("o1");

        assertEquals("Cancelled", result.getMessage());
        verify(inventoryApi).revertInventory(anyList());
        verify(orderApi).cancelOrder("o1");
    }

    @Test
    void shouldThrowExceptionIfOrderNotCancellable() {
        ApiException ex1 = assertThrows(ApiException.class, () -> orderFlow.checkOrderCancellable("PLACED"));
        assertEquals("PLACED ORDERS CANNOT BE CANCELLED", ex1.getMessage());

        ApiException ex2 = assertThrows(ApiException.class, () -> orderFlow.checkOrderCancellable("CANCELLED"));
        assertEquals("ORDER CANCELLED ALREADY", ex2.getMessage());
    }

    // ---------------- getInventoryPojosForOrder ----------------
    @Test
    void shouldMapBarcodesToInventoryPojos() {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setOrderedQuantity(5);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        when(productApi.mapBarcodesToProductIds(List.of("b1"))).thenReturn(Map.of("b1", "p1"));

        List<InventoryPojo> invPojos = orderFlow.getInventoryPojosForOrder(order.getOrderItems());

        assertEquals(1, invPojos.size());
        assertEquals("p1", invPojos.get(0).getProductId());
        assertEquals(5, invPojos.get(0).getQuantity());
    }

    // ---------------- getAllOrders ----------------
    @Test
    void shouldGetAllOrders() {
        OrderPojo o1 = new OrderPojo();
        Page<OrderPojo> page = new PageImpl<>(List.of(o1));

        when(orderApi.getAllOrders(0, 10)).thenReturn(page);

        Page<OrderPojo> result = orderFlow.getAllOrders(0, 10);

        assertEquals(1, result.getContent().size());
        verify(orderApi).getAllOrders(0, 10);
    }
}
