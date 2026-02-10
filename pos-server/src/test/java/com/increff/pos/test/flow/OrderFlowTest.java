//package com.increff.pos.test.flow;
//
//import com.increff.pos.api.InventoryApiImpl;
//import com.increff.pos.api.OrderApiImpl;
//import com.increff.pos.api.ProductApiImpl;
//import com.increff.pos.db.InventoryPojo;
//import com.increff.pos.db.OrderItemPojo;
//import com.increff.pos.db.OrderPojo;
//import com.increff.pos.db.ProductPojo;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.flow.OrderFlow;
//import com.increff.pos.model.data.MessageData;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//
//import java.time.ZonedDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderFlowTest {
//
//    @InjectMocks
//    private OrderFlow orderFlow;
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
//    private static final String ORDER_ID = "507f1f77bcf86cd799439011";
//    private OrderPojo orderPojo;
//    private OrderItemPojo item;
//
//    @BeforeEach
//    void setUp() {
//        item = new OrderItemPojo();
//        item.setProductId("P1");
//        item.setOrderedQuantity(2);
//
//        orderPojo = new OrderPojo();
//        orderPojo.setOrderId(ORDER_ID);
//        orderPojo.setOrderStatus("FULFILLABLE");
//        orderPojo.setOrderItems(List.of(item));
//    }
//
//    /* ---------------- CREATE ORDER ---------------- */
//
//    @Test
//    void createOrder_success() throws Exception {
//        when(inventoryApi.reserveInventory(anyList())).thenReturn(true);
//        when(orderApi.createOrder(any())).thenReturn(orderPojo);
//
//        OrderPojo result = orderFlow.createOrder(orderPojo);
//
//        assertNotNull(result);
//        verify(orderApi).createOrder(any());
//    }
//
//    /* ---------------- EDIT ORDER ---------------- */
//
//    @Test
//    void editOrder_existingFulfillable_andFulfillableIncoming() throws Exception {
//
//        when(orderApi.getCheckByOrderId(ORDER_ID)).thenReturn(orderPojo);
//        when(inventoryApi.checkOrderFulfillable(anyList())).thenReturn(true);
//        when(inventoryApi.aggregateItemsByProductId(anyList()))
//                .thenReturn(Map.of("P1", 2));
//        when(orderApi.editOrder(any())).thenReturn(orderPojo);
//
//        OrderPojo result = orderFlow.editOrder(orderPojo, ORDER_ID);
//
//        assertNotNull(result);
//        verify(inventoryApi).calculateAndUpdateDeltaInventory(anyMap(), anyMap());
//    }
//
//    /* ---------------- CANCEL ORDER ---------------- */
//
//    @Test
//    void cancelOrder_fulfillable_revertsInventory() throws Exception {
//        when(orderApi.getCheckByOrderId(ORDER_ID)).thenReturn(orderPojo);
//
//        MessageData msg = new MessageData();
//        msg.setMessage("Cancelled");
//
//        when(orderApi.cancelOrder(ORDER_ID)).thenReturn(msg);
//
//        MessageData result = orderFlow.cancelOrder(ORDER_ID);
//
//        assertEquals("Cancelled", result.getMessage());
//        verify(inventoryApi).revertInventory(anyList());
//    }
//
//    /* ---------------- GET ALL ORDERS ---------------- */
//
//    @Test
//    void getAllOrders_success() {
//        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
//
//        when(orderApi.getAllOrders(0, 10)).thenReturn(page);
//
//        Page<OrderPojo> result = orderApi.getAllOrders(0, 10);
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    /* ---------------- GET ORDER ---------------- */
//
//    @Test
//    void getOrder_success() throws Exception {
//        when(orderApi.getCheckByOrderId(ORDER_ID)).thenReturn(orderPojo);
//
//        OrderPojo result = orderApi.getCheckByOrderId(ORDER_ID);
//
//        assertEquals(orderPojo, result);
//    }
//
//    /* ---------------- UPDATE PLACED STATUS ---------------- */
//
//    @Test
//    void updatePlacedStatus_success() throws Exception {
//        orderApi.updatePlacedStatus(ORDER_ID);
//
//        verify(orderApi).updatePlacedStatus(ORDER_ID);
//    }
//
//    /* ---------------- CHECK INVOICE DOWNLOADABLE ---------------- */
//
//    @Test
//    void checkInvoiceDownloadable_success() throws Exception {
//        orderPojo.setOrderStatus("PLACED");
//        when(orderApi.getCheckByOrderId(ORDER_ID)).thenReturn(orderPojo);
//
//        assertDoesNotThrow(() -> orderFlow.checkInvoiceDownloadable(ORDER_ID));
//    }
//
//    @Test
//    void checkInvoiceDownloadable_notPlaced() throws Exception {
//        when(orderApi.getCheckByOrderId(ORDER_ID)).thenReturn(orderPojo);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> orderFlow.checkInvoiceDownloadable(ORDER_ID));
//
//        assertTrue(ex.getMessage().contains("ORDER NOT PLACED"));
//    }
//
//    /* ---------------- FILTER ORDERS ---------------- */
//
//    @Test
//    void filterOrders_success() {
//        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
//
//        when(orderApi.filterOrdersByDate(any(), any(), eq(0), eq(10))).thenReturn(page);
//
//        Page<OrderPojo> result = orderApi.filterOrdersByDate(
//                ZonedDateTime.now().minusDays(1),
//                ZonedDateTime.now(),
//                0,
//                10
//        );
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    /* ---------------- INVENTORY POJOS ---------------- */
//
//    @Test
//    void getInventoryPojosForOrder_success() {
//        List<InventoryPojo> result =
//                orderFlow.getInventoryPojosForOrder(List.of(item));
//
//        assertEquals(1, result.size());
//        assertEquals("P1", result.get(0).getProductId());
//        assertEquals(2, result.get(0).getQuantity());
//    }
//
//    /* ---------------- PRODUCT MAPPERS ---------------- */
//
//    @Test
//    void mapBarcodesToProductPojos_success() {
//        when(productApi.getProductPojosForBarcodes(anyList()))
//                .thenReturn(Map.of("B1", new ProductPojo()));
//
//        Map<String, ProductPojo> result =
//                orderFlow.getProductPojosForBarcodes(List.of("B1"));
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void mapProductIdsToProductPojos_success() {
//        when(productApi.getProductPojosForProductIds(anyList()))
//                .thenReturn(List.of(new ProductPojo()));
//
//        Map<String, ProductPojo> result =
//                orderFlow.getProductPojosForProductIds(List.of("P1"));
//
//        assertEquals(1, result.size());
//    }
//
//    /* ---------------- SEARCH BY ID ---------------- */
//
//    @Test
//    void searchById_success() throws Exception {
//        Page<OrderPojo> page = new PageImpl<>(List.of(orderPojo));
//
//        when(orderApi.searchById(ORDER_ID, 0, 10)).thenReturn(page);
//
//        Page<OrderPojo> result =
//                orderApi.searchById(ORDER_ID, 0, 10);
//
//        assertEquals(1, result.getTotalElements());
//    }
//}
