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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFlowTest {

    @InjectMocks
    private OrderFlow orderFlow;

    @Mock
    private OrderApiImpl orderApi;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private InventoryApiImpl inventoryApi;

    private OrderPojo orderPojo;
    private OrderItem orderItem;
    private ProductPojo productPojo;

    @BeforeEach
    void setup() {
        orderItem = new OrderItem();
        orderItem.setBarcode("barcode-1");
        orderItem.setOrderedQuantity(2);

        orderPojo = new OrderPojo();
        orderPojo.setOrderItems(List.of(orderItem));
        orderPojo.setOrderStatus("CREATED");

        productPojo = new ProductPojo();
        productPojo.setId("product-1");
    }

    // ---------- createOrder ----------

    @Test
    void testCreateOrder_fulfillable() throws ApiException {
        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("barcode-1", productPojo));
        when(inventoryApi.reserveInventory(anyList())).thenReturn(true);
        when(orderApi.createOrder(any(), eq(true))).thenReturn(orderPojo);

        OrderPojo result = orderFlow.createOrder(orderPojo);

        assertNotNull(result);
        verify(inventoryApi).reserveInventory(anyList());
        verify(orderApi).createOrder(any(), eq(true));
    }

    // ---------- editOrder ----------

    @Test
    void testEditOrder_existingFulfillable_incomingFulfillable() throws ApiException {
        orderPojo.setOrderStatus("FULFILLABLE");

        when(orderApi.getOrderStatus("order-1")).thenReturn("FULFILLABLE");
        when(orderApi.getCheckByOrderId("order-1")).thenReturn(orderPojo);
        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("barcode-1", productPojo));
        when(inventoryApi.checkOrderFulfillable(anyList())).thenReturn(true);
        when(inventoryApi.aggregateItemsByProductId(anyList()))
                .thenReturn(Map.of("product-1", 2));
        when(orderApi.editOrder(any(), eq(true))).thenReturn(orderPojo);

        OrderPojo result = orderFlow.editOrder(orderPojo, "order-1");

        assertNotNull(result);
        verify(inventoryApi).calculateAndUpdateDeltaInventory(anyMap(), anyMap());
    }

    @Test
    void testEditOrder_existingNotFulfillable_incomingNotFulfillable() throws ApiException {
        when(orderApi.getOrderStatus("order-2")).thenReturn("CREATED");
        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("barcode-1", productPojo));
        when(inventoryApi.checkOrderFulfillable(anyList())).thenReturn(false);
        when(orderApi.editOrder(any(), eq(false))).thenReturn(orderPojo);

        OrderPojo result = orderFlow.editOrder(orderPojo, "order-2");

        assertNotNull(result);
        verify(inventoryApi).calculateAndUpdateDeltaInventory(anyMap(), anyMap());
    }

    // ---------- cancelOrder ----------

    @Test
    void testCancelOrder_fulfillable() throws ApiException {
        orderPojo.setOrderStatus("FULFILLABLE");

        when(orderApi.getCheckByOrderId("order-3")).thenReturn(orderPojo);
        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("barcode-1", productPojo));
        when(orderApi.cancelOrder("order-3")).thenReturn(new MessageData());

        MessageData result = orderFlow.cancelOrder("order-3");

        assertNotNull(result);
        verify(inventoryApi).revertInventory(anyList());
    }

    @Test
    void testCancelOrder_notFulfillable() throws ApiException {
        orderPojo.setOrderStatus("CREATED");

        when(orderApi.getCheckByOrderId("order-4")).thenReturn(orderPojo);
        when(orderApi.cancelOrder("order-4")).thenReturn(new MessageData());

        MessageData result = orderFlow.cancelOrder("order-4");

        assertNotNull(result);
        verify(inventoryApi, never()).revertInventory(anyList());
    }

    // ---------- getters & simple pass-throughs ----------

    @Test
    void testGetAllOrders() {
        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
        when(orderApi.getAllOrders(0, 10)).thenReturn(page);

        Page<OrderPojo> result = orderFlow.getAllOrders(0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetOrder() throws ApiException {
        when(orderApi.getCheckByOrderId("order-5")).thenReturn(orderPojo);

        OrderPojo result = orderFlow.getOrder("order-5");

        assertNotNull(result);
    }

    @Test
    void testUpdatePlacedStatus() throws ApiException {
        doNothing().when(orderApi).updatePlacedStatus("order-6");

        orderFlow.updatePlacedStatus("order-6");

        verify(orderApi).updatePlacedStatus("order-6");
    }

    // ---------- checkInvoiceDownloadable ----------

    @Test
    void testCheckInvoiceDownloadable_success() throws ApiException {
        orderPojo.setOrderStatus("PLACED");
        when(orderApi.getCheckByOrderId("order-7")).thenReturn(orderPojo);

        orderFlow.checkInvoiceDownloadable("order-7");
    }

    @Test
    void testCheckInvoiceDownloadable_orderNotPlaced() throws ApiException {
        orderPojo.setOrderStatus("CREATED");
        when(orderApi.getCheckByOrderId("order-8")).thenReturn(orderPojo);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderFlow.checkInvoiceDownloadable("order-8"));

        assertTrue(ex.getMessage().contains("ORDER NOT PLACED"));
    }

    @Test
    void testCheckInvoiceDownloadable_orderDoesNotExist() throws ApiException {
        when(orderApi.getCheckByOrderId("order-9")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> orderFlow.checkInvoiceDownloadable("order-9"));

        assertTrue(ex.getMessage().contains("DOESN'T EXIST"));
    }

    // ---------- filterOrders ----------

    @Test
    void testFilterOrders() {
        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        when(orderApi.filterOrders(start, end, 0, 10)).thenReturn(page);

        Page<OrderPojo> result = orderFlow.filterOrders(start, end, 0, 10);

        assertEquals(1, result.getContent().size());
    }

    // ---------- inventory helpers ----------

    @Test
    void testGetInventoryPojosForOrder() {
        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("barcode-1", productPojo));

        List<InventoryPojo> result =
                orderFlow.getInventoryPojosForOrder(List.of(orderItem));

        assertEquals(1, result.size());
        assertEquals("product-1", result.get(0).getProductId());
        assertEquals(2, result.get(0).getQuantity());
    }

    @Test
    void testMapBarcodesToProductPojos() {
        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Map.of("barcode-1", productPojo));

        Map<String, ProductPojo> map =
                orderFlow.mapBarcodesToProductPojos(List.of("barcode-1"));

        assertEquals(1, map.size());
        assertTrue(map.containsKey("barcode-1"));
    }
}
