//package com.increff.pos.test.flow;
//
//import com.increff.pos.api.InventoryApiImpl;
//import com.increff.pos.api.OrderApiImpl;
//import com.increff.pos.api.ProductApiImpl;
//import com.increff.pos.db.InventoryPojo;
//import com.increff.pos.db.OrderPojo;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.flow.OrderFlow;
//import com.increff.pos.model.data.MessageData;
//import com.increff.pos.model.data.OrderItem;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//
//import java.time.ZonedDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderFlowTest {
//
//    @Mock
//    private OrderApiImpl orderApi;
//
//    @Mock
//    private ProductApiImpl productApi;
//
//    @Mock
//    private InventoryApiImpl inventoryApi;
//
//    @InjectMocks
//    private OrderFlow orderFlow;
//
//    private OrderItem item(String barcode, int qty) {
//        OrderItem item = new OrderItem();
//        item.setBarcode(barcode);
//        item.setOrderedQuantity(qty);
//        return item;
//    }
//
//    private OrderPojo order(String status) {
//        OrderPojo pojo = new OrderPojo();
//        pojo.setOrderStatus(status);
//        pojo.setOrderItems(List.of(item("b1", 2)));
//        return pojo;
//    }
//
//    // ---------- createOrder ----------
//
//    @Test
//    void testCreateOrderFulfillable() throws ApiException {
//        OrderPojo order = order("CREATED");
//
//        when(productApi.mapBarcodesToProductPojos(List.of("b1")))
//                .thenReturn(Map.of("b1", "p1"));
//
//        when(inventoryApi.reserveInventory(anyList())).thenReturn(true);
//        when(orderApi.createOrder(eq(order), eq(true))).thenReturn(order);
//
//        OrderPojo result = orderFlow.createOrder(order);
//
//        assertEquals(order, result);
//        verify(orderApi).createOrder(order, true);
//    }
//
//    @Test
//    void testCreateOrderNotFulfillable() throws ApiException {
//        OrderPojo order = order("CREATED");
//
//        when(productApi.mapBarcodesToProductPojos(List.of("b1")))
//                .thenReturn(Map.of("b1", "p1"));
//
//        when(inventoryApi.reserveInventory(anyList())).thenReturn(false);
//        when(orderApi.createOrder(eq(order), eq(false))).thenReturn(order);
//
//        OrderPojo result = orderFlow.createOrder(order);
//
//        assertEquals(order, result);
//    }
//
//    // ---------- editOrder ----------
//
//    @Test
//    void testEditOrderSuccessExistingFulfillable() throws ApiException {
//        OrderPojo incoming = order("CREATED");
//        OrderPojo existing = order("FULFILLABLE");
//
//        when(orderApi.getOrderStatus("o1")).thenReturn("FULFILLABLE");
//        when(orderApi.getCheckByOrderId("o1")).thenReturn(existing);
//
//        when(productApi.mapBarcodesToProductPojos(any()))
//                .thenReturn(Map.of("b1", "p1"));
//
//        when(inventoryApi.checkOrderFulfillable(anyList())).thenReturn(true);
//        when(inventoryApi.aggregateItemsByProductId(any()))
//                .thenReturn(Map.of("p1", 2));
//
//        when(orderApi.editOrder(incoming, true)).thenReturn(incoming);
//
//        OrderPojo result = orderFlow.editOrder(incoming, "o1");
//
//        assertEquals(incoming, result);
//        verify(inventoryApi).editOrder(anyMap(), anyMap());
//    }
//
//    @Test
//    void testEditOrderCancelled() throws ApiException {
//        when(orderApi.getOrderStatus("o1")).thenReturn("CANCELLED");
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.editOrder(order("CREATED"), "o1"));
//
//        assertTrue(ex.getMessage().contains("CANNOT BE EDITED"));
//    }
//
//    @Test
//    void testEditOrderPlaced() throws ApiException {
//        when(orderApi.getOrderStatus("o1")).thenReturn("PLACED");
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.editOrder(order("CREATED"), "o1"));
//
//        assertTrue(ex.getMessage().contains("CANNOT BE EDITED"));
//    }
//
//    // ---------- cancelOrder ----------
//
//    @Test
//    void testCancelOrderFulfillable() throws ApiException {
//        OrderPojo order = order("FULFILLABLE");
//
//        when(orderApi.getCheckByOrderId("o1")).thenReturn(order);
//        when(orderApi.getOrderStatus("o1")).thenReturn("FULFILLABLE");
//        when(productApi.mapBarcodesToProductPojos(any()))
//                .thenReturn(Map.of("b1", "p1"));
//        when(orderApi.cancelOrder("o1"))
//                .thenReturn(new MessageData("cancelled"));
//
//        MessageData result = orderFlow.cancelOrder("o1");
//
//        assertEquals("cancelled", result.getMessage());
//        verify(inventoryApi).revertInventory(anyList());
//    }
//
//    @Test
//    void testCancelOrderAlreadyCancelled() throws ApiException {
//        when(orderApi.getOrderStatus("o1")).thenReturn("CANCELLED");
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.cancelOrder("o1"));
//
//        assertTrue(ex.getMessage().contains("ALREADY"));
//    }
//
//    @Test
//    void testCancelOrderPlaced() throws ApiException {
//        when(orderApi.getOrderStatus("o1")).thenReturn("PLACED");
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.cancelOrder("o1"));
//
//        assertTrue(ex.getMessage().contains("CANNOT BE CANCELLED"));
//    }
//
//    // ---------- checkInvoiceDownloadable ----------
//
//    @Test
//    void testCheckInvoiceDownloadableSuccess() throws ApiException {
//        OrderPojo order = order("PLACED");
//
//        when(orderApi.getCheckByOrderId("o1")).thenReturn(order);
//
//        assertDoesNotThrow(() -> orderFlow.checkInvoiceDownloadable("o1"));
//    }
//
//    @Test
//    void testCheckInvoiceDownloadableNotPlaced() throws ApiException {
//        OrderPojo order = order("CREATED");
//
//        when(orderApi.getCheckByOrderId("o1")).thenReturn(order);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.checkInvoiceDownloadable("o1"));
//
//        assertTrue(ex.getMessage().contains("NOT PLACED"));
//    }
//
//    @Test
//    void testCheckInvoiceDownloadableNullOrder() throws ApiException {
//        when(orderApi.getCheckByOrderId("o1")).thenReturn(null);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.checkInvoiceDownloadable("o1"));
//
//        assertTrue(ex.getMessage().contains("DOESN'T EXIST"));
//    }
//
//    // ---------- passthrough methods ----------
//
//    @Test
//    void testGetAllOrders() {
//        Page<OrderPojo> page = new PageImpl<>(List.of(order("CREATED")));
//        when(orderApi.getAllOrders(0, 10)).thenReturn(page);
//
//        assertEquals(1, orderFlow.getAllOrders(0, 10).getTotalElements());
//    }
//
//    @Test
//    void testGetOrder() throws ApiException {
//        OrderPojo order = order("CREATED");
//        when(orderApi.getCheckByOrderId("o1")).thenReturn(order);
//
//        assertEquals(order, orderFlow.getOrder("o1"));
//    }
//
//    @Test
//    void testUpdatePlacedStatus() throws ApiException {
//        orderFlow.updatePlacedStatus("o1");
//
//        verify(orderApi).updatePlacedStatus("o1");
//    }
//
//    @Test
//    void testFilterOrders() {
//        Page<OrderPojo> page = new PageImpl<>(List.of(order("CREATED")));
//
//        when(orderApi.filterOrders(any(), any(), eq(0), eq(10)))
//                .thenReturn(page);
//
//        assertEquals(1,
//                orderFlow.filterOrders(ZonedDateTime.now(), ZonedDateTime.now(), 0, 10)
//                        .getTotalElements());
//    }
//
//    // ---------- getInventoryPojosForOrder ----------
//
//    @Test
//    void testGetInventoryPojosForOrder() {
//        OrderItem item = item("b1", 3);
//
//        when(productApi.mapBarcodesToProductPojos(List.of("b1")))
//                .thenReturn(Map.of("b1", "p1"));
//
//        List<InventoryPojo> result =
//                orderFlow.getInventoryPojosForOrder(List.of(item));
//
//        assertEquals(1, result.size());
//        assertEquals("p1", result.get(0).getProductId());
//        assertEquals(3, result.get(0).getQuantity());
//    }
//}
