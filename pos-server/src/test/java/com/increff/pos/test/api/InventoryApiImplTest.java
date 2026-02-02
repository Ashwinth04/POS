package com.increff.pos.test.api;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiImplTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApiImpl inventoryApi;

    // ---------- updateSingleInventory ----------

    @Test
    void testUpdateSingleInventory_success() throws ApiException {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("P1");
        pojo.setQuantity(10);

        doNothing().when(inventoryDao).updateInventory(pojo);

        InventoryPojo result =
                inventoryApi.updateSingleInventory(pojo);

        assertThat(result).isEqualTo(pojo);
        verify(inventoryDao).updateInventory(pojo);
    }

    // ---------- bulkInventoryUpdate ----------

    @Test
    void testBulkInventoryUpdate_nonEmpty() {
        List<InventoryPojo> list =
                List.of(new InventoryPojo());

        inventoryApi.bulkInventoryUpdate(list);

        verify(inventoryDao).bulkUpdate(list);
    }

    @Test
    void testBulkInventoryUpdate_empty() {
        inventoryApi.bulkInventoryUpdate(Collections.emptyList());

        verify(inventoryDao, never()).bulkUpdate(any());
    }

    // ---------- reserveInventory ----------

    @Test
    void testReserveInventory_fulfillable() throws ApiException {
        InventoryPojo orderItem = new InventoryPojo();
        orderItem.setProductId("P1");
        orderItem.setQuantity(5);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("P1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("P1")))
                .thenReturn(List.of(existing));

        boolean result =
                inventoryApi.reserveInventory(List.of(orderItem));

        assertThat(result).isTrue();
        assertThat(orderItem.getQuantity()).isEqualTo(-5);
        verify(inventoryDao).bulkUpdate(List.of(orderItem));
    }

    @Test
    void testReserveInventory_notFulfillable() throws ApiException {
        InventoryPojo orderItem = new InventoryPojo();
        orderItem.setProductId("P1");
        orderItem.setQuantity(15);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("P1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("P1")))
                .thenReturn(List.of(existing));

        boolean result =
                inventoryApi.reserveInventory(List.of(orderItem));

        assertThat(result).isFalse();
        verify(inventoryDao, never()).bulkUpdate(any());
    }

    // ---------- editOrder ----------

    @Test
    void testEditOrder_success() throws ApiException {
        Map<String, Integer> existing = Map.of("P1", 2);
        Map<String, Integer> incoming = Map.of("P1", 5);

        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("P1");
        pojo.setQuantity(3);

        try (MockedStatic<InventoryHelper> mocked =
                     mockStatic(InventoryHelper.class)) {

            mocked.when(() -> InventoryHelper.getPojosFromMap(any()))
                    .thenReturn(List.of(pojo));

            inventoryApi.editOrder(existing, incoming);

            verify(inventoryDao).bulkUpdate(List.of(pojo));
        }
    }

    // ---------- revertInventory ----------

    @Test
    void testRevertInventory_success() throws ApiException {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("P1");
        pojo.setQuantity(5);

        inventoryApi.revertInventory(List.of(pojo));

        verify(inventoryDao).bulkUpdate(List.of(pojo));
    }

    // ---------- createDummyInventoryRecord ----------

    @Test
    void testCreateDummyInventoryRecord() {
        inventoryApi.createDummyInventoryRecord("P1");

        verify(inventoryDao).save(argThat(p ->
                p.getProductId().equals("P1") &&
                        p.getQuantity() == 0
        ));
    }

    // ---------- fetchRecordsForOrderItems ----------

    @Test
    void testFetchRecordsForOrderItems_success() {
        InventoryPojo input = new InventoryPojo();
        input.setProductId("P1");

        InventoryPojo db = new InventoryPojo();
        db.setProductId("P1");
        db.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("P1")))
                .thenReturn(List.of(db));

        Map<String, InventoryPojo> result =
                inventoryApi.fetchRecordsForOrderItems(List.of(input));

        assertThat(result).containsKey("P1");
        assertThat(result.get("P1").getQuantity()).isEqualTo(10);
    }

    // ---------- checkOrderFulfillable ----------

    @Test
    void testCheckOrderFulfillable_true() throws ApiException {
        InventoryPojo order = new InventoryPojo();
        order.setProductId("P1");
        order.setQuantity(5);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("P1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("P1")))
                .thenReturn(List.of(existing));

        boolean result =
                inventoryApi.checkOrderFulfillable(List.of(order));

        assertThat(result).isTrue();
    }

    @Test
    void testCheckOrderFulfillable_false() throws ApiException {
        InventoryPojo order = new InventoryPojo();
        order.setProductId("P1");
        order.setQuantity(20);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("P1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("P1")))
                .thenReturn(List.of(existing));

        boolean result =
                inventoryApi.checkOrderFulfillable(List.of(order));

        assertThat(result).isFalse();
    }

    // ---------- updateInventory ----------

    @Test
    void testUpdateInventory_success() throws ApiException {
        List<InventoryPojo> list = List.of(new InventoryPojo());

        inventoryApi.updateInventory(list);

        verify(inventoryDao).bulkUpdate(list);
    }

    // ---------- updateDeltaInventory ----------

    @Test
    void testUpdateDeltaInventory_success() {
        Map<String, Integer> delta = Map.of("P1", 3);

        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("P1");
        pojo.setQuantity(3);

        try (MockedStatic<InventoryHelper> mocked =
                     mockStatic(InventoryHelper.class)) {

            mocked.when(() -> InventoryHelper.getPojosFromMap(delta))
                    .thenReturn(List.of(pojo));

            inventoryApi.updateDeltaInventory(delta);

            verify(inventoryDao).bulkUpdate(List.of(pojo));
        }
    }

    // ---------- aggregateItemsByProductId ----------

    @Test
    void testAggregateItemsByProductId_success() {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("P1");
        p1.setQuantity(2);

        InventoryPojo p2 = new InventoryPojo();
        p2.setProductId("P1");
        p2.setQuantity(3);

        Map<String, Integer> result =
                inventoryApi.aggregateItemsByProductId(List.of(p1, p2));

        assertThat(result.get("P1")).isEqualTo(5);
    }

    // ---------- getInventoryForProductIds ----------

    @Test
    void testGetInventoryForProductIds_success() {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("P1");

        when(inventoryDao.findByProductIds(List.of("P1")))
                .thenReturn(List.of(pojo));

        Map<String, InventoryPojo> result =
                inventoryApi.getInventoryForProductIds(List.of("P1"));

        assertThat(result).containsKey("P1");
        assertThat(result.get("P1")).isEqualTo(pojo);
    }
}
