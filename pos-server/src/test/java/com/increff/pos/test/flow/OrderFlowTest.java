package com.increff.pos.test.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.OrderApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.OrderPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.db.subdocuments.OrderItemPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.helper.OrderHelper;
import com.increff.pos.model.data.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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

    // ---------- createOrder tests ----------
    @Test
    void testCreateOrderFulfillable() {
        OrderPojo order = new OrderPojo();
        List<InventoryPojo> inventoryList = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mockedHelper = mockStatic(OrderHelper.class)) {
            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(order.getOrderItems()))
                    .thenReturn(inventoryList);

            when(inventoryApi.reserveInventory(inventoryList)).thenReturn(true);
            when(orderApi.saveOrder(order)).thenReturn(order);

            OrderPojo result = orderFlow.createOrder(order);

            assertEquals("FULFILLABLE", result.getOrderStatus());
        }
    }

    @Test
    void testCreateOrderUnfulfillable() {
        OrderPojo order = new OrderPojo();
        List<InventoryPojo> inventoryList = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mockedHelper = mockStatic(OrderHelper.class)) {
            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(order.getOrderItems()))
                    .thenReturn(inventoryList);

            when(inventoryApi.reserveInventory(inventoryList)).thenReturn(false);
            when(orderApi.saveOrder(order)).thenReturn(order);

            OrderPojo result = orderFlow.createOrder(order);

            assertEquals("UNFULFILLABLE", result.getOrderStatus());
        }
    }

    // ---------- editOrder: testing all aggregateAndUpdateInventory branches ----------
    @Test
    void testEditOrder_AllBranches() throws ApiException {
        // Existing FULFILLABLE, incoming FULFILLABLE
        OrderPojo existing = new OrderPojo();
        existing.setOrderStatus("FULFILLABLE");

        OrderPojo update = new OrderPojo();
        update.setOrderItems(List.of(new OrderItemPojo()));

        List<InventoryPojo> incomingInventory = List.of(new InventoryPojo());
        List<InventoryPojo> existingInventory = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mockedHelper = mockStatic(OrderHelper.class)) {

            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(update.getOrderItems()))
                    .thenReturn(incomingInventory);

            mockedHelper.when(() -> OrderHelper.checkOrderEditable("FULFILLABLE")).thenAnswer(i -> null);
            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(existing.getOrderItems()))
                    .thenReturn(existingInventory);

            when(orderApi.getCheckByOrderId("order1")).thenReturn(existing);
            when(inventoryApi.checkOrderFulfillable(incomingInventory)).thenReturn(true);

            // Aggregate results for incoming and existing
            when(inventoryApi.aggregateItemsByProductId(incomingInventory)).thenReturn(Map.of("p1", 5));
            when(inventoryApi.aggregateItemsByProductId(existingInventory)).thenReturn(Map.of("p1", 2));

            doNothing().when(inventoryApi).calculateAndUpdateDeltaInventory(anyMap(), anyMap());
            when(orderApi.editOrder(update)).thenReturn(update);

            OrderPojo result = orderFlow.editOrder(update, "order1");

            assertEquals("FULFILLABLE", result.getOrderStatus());
            verify(inventoryApi).calculateAndUpdateDeltaInventory(Map.of("p1", 2), Map.of("p1", 5));
        }

        // Existing FULFILLABLE, incoming UNFULFILLABLE
        existing.setOrderStatus("FULFILLABLE");
        OrderPojo update2 = new OrderPojo();
        update2.setOrderItems(List.of(new OrderItemPojo()));

        List<InventoryPojo> incomingInventory2 = List.of(new InventoryPojo());
        List<InventoryPojo> existingInventory2 = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mockedHelper = mockStatic(OrderHelper.class)) {

            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(update2.getOrderItems()))
                    .thenReturn(incomingInventory2);

            mockedHelper.when(() -> OrderHelper.checkOrderEditable("FULFILLABLE")).thenAnswer(i -> null);
            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(existing.getOrderItems()))
                    .thenReturn(existingInventory2);

            when(orderApi.getCheckByOrderId("order1")).thenReturn(existing);
            when(inventoryApi.checkOrderFulfillable(incomingInventory2)).thenReturn(false);

            // Only existing aggregation
            when(inventoryApi.aggregateItemsByProductId(existingInventory2)).thenReturn(Map.of("p1", 3));
            when(orderApi.editOrder(update2)).thenReturn(update2);

            OrderPojo result2 = orderFlow.editOrder(update2, "order1");

            assertEquals("UNFULFILLABLE", result2.getOrderStatus());
            verify(inventoryApi).calculateAndUpdateDeltaInventory(Map.of("p1", 3), Map.of());
        }

        // Existing NOT FULFILLABLE, incoming FULFILLABLE
        existing.setOrderStatus("UNFULFILLABLE");
        OrderPojo update3 = new OrderPojo();
        update3.setOrderItems(List.of(new OrderItemPojo()));

        List<InventoryPojo> incomingInventory3 = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mockedHelper = mockStatic(OrderHelper.class)) {
            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(update3.getOrderItems()))
                    .thenReturn(incomingInventory3);
            mockedHelper.when(() -> OrderHelper.checkOrderEditable("UNFULFILLABLE")).thenAnswer(i -> null);

            when(orderApi.getCheckByOrderId("order1")).thenReturn(existing);
            when(inventoryApi.checkOrderFulfillable(incomingInventory3)).thenReturn(true);
            when(inventoryApi.aggregateItemsByProductId(incomingInventory3)).thenReturn(Map.of("p1", 5));
            when(orderApi.editOrder(update3)).thenReturn(update3);

            OrderPojo result3 = orderFlow.editOrder(update3, "order1");

            assertEquals("FULFILLABLE", result3.getOrderStatus());
            verify(inventoryApi).calculateAndUpdateDeltaInventory(Map.of(), Map.of("p1", 5));
        }

        // Existing NOT FULFILLABLE, incoming UNFULFILLABLE
        existing.setOrderStatus("UNFULFILLABLE");
        OrderPojo update4 = new OrderPojo();
        update4.setOrderItems(List.of(new OrderItemPojo()));

        List<InventoryPojo> incomingInventory4 = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mockedHelper = mockStatic(OrderHelper.class)) {
            mockedHelper.when(() -> OrderHelper.getInventoryPojosForOrder(update4.getOrderItems()))
                    .thenReturn(incomingInventory4);
            mockedHelper.when(() -> OrderHelper.checkOrderEditable("UNFULFILLABLE")).thenAnswer(i -> null);

            when(orderApi.getCheckByOrderId("order1")).thenReturn(existing);
            when(inventoryApi.checkOrderFulfillable(incomingInventory4)).thenReturn(false);
            when(orderApi.editOrder(update4)).thenReturn(update4);

            OrderPojo result4 = orderFlow.editOrder(update4, "order1");

            assertEquals("UNFULFILLABLE", result4.getOrderStatus());
            verify(inventoryApi).calculateAndUpdateDeltaInventory(Map.of(), Map.of());
        }
    }

    // ---------- cancelOrder tests ----------
    @Test
    void testCancelOrderFulfillable() throws ApiException {
        OrderPojo order = new OrderPojo();
        order.setOrderStatus("FULFILLABLE");
        List<InventoryPojo> inventoryList = List.of(new InventoryPojo());

        try (MockedStatic<OrderHelper> mocked = mockStatic(OrderHelper.class)) {
            when(orderApi.getCheckByOrderId("id")).thenReturn(order);
            mocked.when(() -> OrderHelper.checkOrderCancellable("FULFILLABLE")).thenAnswer(i -> null);
            mocked.when(() -> OrderHelper.getInventoryPojosForOrder(order.getOrderItems()))
                    .thenReturn(inventoryList);

            doNothing().when(inventoryApi).revertInventory(inventoryList);
            when(orderApi.cancelOrder("id")).thenReturn(new MessageData("success"));

            MessageData result = orderFlow.cancelOrder("id");

            assertEquals("success", result.getMessage());
            verify(inventoryApi).revertInventory(inventoryList);
        }
    }

    @Test
    void testCancelOrderUnfulfillable() throws ApiException {
        OrderPojo order = new OrderPojo();
        order.setOrderStatus("UNFULFILLABLE");

        try (MockedStatic<OrderHelper> mocked = mockStatic(OrderHelper.class)) {
            when(orderApi.getCheckByOrderId("id")).thenReturn(order);
            mocked.when(() -> OrderHelper.checkOrderCancellable("UNFULFILLABLE")).thenAnswer(i -> null);
            when(orderApi.cancelOrder("id")).thenReturn(new MessageData("success"));

            MessageData result = orderFlow.cancelOrder("id");

            assertEquals("success", result.getMessage());
            verify(inventoryApi, never()).revertInventory(anyList());
        }
    }

    // ---------- mapping methods ----------
    @Test
    void testMapBarcodesToProductPojos() {
        ProductPojo p = new ProductPojo();
        p.setBarcode("B1");

        when(productApi.getProductPojosForBarcodes(List.of("B1"))).thenReturn(List.of(p));

        Map<String, ProductPojo> map = orderFlow.mapBarcodesToProductPojos(List.of("B1"));

        assertEquals(1, map.size());
        assertEquals(p, map.get("B1"));
    }

    @Test
    void testMapProductIdsToProductPojos() {
        ProductPojo p = new ProductPojo();
        p.setId("P1");

        when(productApi.getProductPojosForProductIds(List.of("P1"))).thenReturn(List.of(p));

        Map<String, ProductPojo> map = orderFlow.mapProductIdsToProductPojos(List.of("P1"));

        assertEquals(1, map.size());
        assertEquals(p, map.get("P1"));
    }
}
