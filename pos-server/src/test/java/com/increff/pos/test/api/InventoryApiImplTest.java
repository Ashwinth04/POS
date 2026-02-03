package com.increff.pos.test.api;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiImplTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApiImpl inventoryApi;

    private InventoryPojo inv(String productId, int qty) {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(productId);
        pojo.setQuantity(qty);
        return pojo;
    }

    // ---------- updateSingleInventory ----------

    @Test
    void testUpdateSingleInventory() throws ApiException {
        InventoryPojo pojo = inv("p1", 10);

        doNothing().when(inventoryDao).updateInventory(pojo);

        InventoryPojo result = inventoryApi.updateSingleInventory(pojo);

        assertEquals(pojo, result);
        verify(inventoryDao).updateInventory(pojo);
    }

    // ---------- bulkInventoryUpdate ----------

    @Test
    void testBulkInventoryUpdateWithItems() throws ApiException {
        List<InventoryPojo> items = List.of(inv("p1", 1));

        inventoryApi.updateBulkInventory(items);

        verify(inventoryDao).bulkUpdate(items);
    }

    @Test
    void testBulkInventoryUpdateEmpty() throws ApiException {
        inventoryApi.updateBulkInventory(List.of());

        verify(inventoryDao, never()).bulkUpdate(any());
    }

    // ---------- reserveInventory ----------

    @Test
    void testReserveInventoryFulfillable() throws ApiException {
        InventoryPojo orderItem = inv("p1", 5);
        InventoryPojo stock = inv("p1", 10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(stock));

        boolean result = inventoryApi.reserveInventory(List.of(orderItem));

        assertTrue(result);
        assertEquals(-5, orderItem.getQuantity());
        verify(inventoryDao).bulkUpdate(anyList());
    }

    @Test
    void testReserveInventoryNotFulfillable() throws ApiException {
        InventoryPojo orderItem = inv("p1", 15);
        InventoryPojo stock = inv("p1", 10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(stock));

        boolean result = inventoryApi.reserveInventory(List.of(orderItem));

        assertFalse(result);
        verify(inventoryDao, never()).bulkUpdate(any());
    }

    // ---------- editOrder ----------

    @Test
    void testEditOrder() throws ApiException {
        Map<String, Integer> existing = Map.of("p1", 2);
        Map<String, Integer> incoming = Map.of("p1", 5);

        try (MockedStatic<InventoryHelper> mocked =
                     Mockito.mockStatic(InventoryHelper.class)) {

            List<InventoryPojo> pojos = List.of(inv("p1", 3));
            mocked.when(() -> InventoryHelper.getPojosFromMap(any()))
                    .thenReturn(pojos);

            inventoryApi.editOrder(existing, incoming);

            verify(inventoryDao).bulkUpdate(pojos);
        }
    }

    // ---------- revertInventory ----------

    @Test
    void testRevertInventory() throws ApiException {
        InventoryPojo pojo = inv("p1", 5);

        inventoryApi.revertInventory(List.of(pojo));

        verify(inventoryDao).bulkUpdate(List.of(pojo));
    }

    // ---------- createDummyInventoryRecord ----------

    @Test
    void testCreateDummyInventoryRecord() {
        inventoryApi.createDummyInventoryRecord("p1");

        verify(inventoryDao).save(argThat(p ->
                p.getProductId().equals("p1") && p.getQuantity() == 0
        ));
    }

    // ---------- createDummyInventoryRecordsBulk ----------

    @Test
    void testCreateDummyInventoryRecordsBulkNull() {
        inventoryApi.createDummyInventoryRecordsBulk(null);

        verify(inventoryDao, never()).saveAll(any());
    }

    @Test
    void testCreateDummyInventoryRecordsBulkEmpty() {
        inventoryApi.createDummyInventoryRecordsBulk(List.of());

        verify(inventoryDao, never()).saveAll(any());
    }

    @Test
    void testCreateDummyInventoryRecordsBulkSuccess() {
        inventoryApi.createDummyInventoryRecordsBulk(List.of("p1", "p2"));

        verify(inventoryDao).saveAll(anyList());
    }

    // ---------- fetchRecordsForOrderItems ----------

    @Test
    void testFetchRecordsForOrderItems() {
        InventoryPojo pojo = inv("p1", 10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(pojo));

        Map<String, InventoryPojo> result =
                inventoryApi.fetchRecordsForOrderItems(List.of(inv("p1", 1)));

        assertEquals(pojo, result.get("p1"));
    }

    // ---------- checkOrderFulfillable ----------

    @Test
    void testCheckOrderFulfillableTrue() throws ApiException {
        InventoryPojo order = inv("p1", 5);
        InventoryPojo stock = inv("p1", 10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(stock));

        assertTrue(inventoryApi.checkOrderFulfillable(List.of(order)));
    }

    @Test
    void testCheckOrderFulfillableFalse() throws ApiException {
        InventoryPojo order = inv("p1", 15);
        InventoryPojo stock = inv("p1", 10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(stock));

        assertFalse(inventoryApi.checkOrderFulfillable(List.of(order)));
    }

    // ---------- updateBulkInventory ----------

    @Test
    void testUpdateBulkInventory() throws ApiException {
        List<InventoryPojo> pojos = List.of(inv("p1", 1));

        inventoryApi.updateBulkInventory(pojos);

        verify(inventoryDao).bulkUpdate(pojos);
    }

    // ---------- updateDeltaInventory ----------

    @Test
    void testUpdateDeltaInventory() {
        Map<String, Integer> delta = Map.of("p1", 5);

        try (MockedStatic<InventoryHelper> mocked =
                     Mockito.mockStatic(InventoryHelper.class)) {

            List<InventoryPojo> pojos = List.of(inv("p1", 5));
            mocked.when(() -> InventoryHelper.getPojosFromMap(delta))
                    .thenReturn(pojos);

            inventoryApi.updateDeltaInventory(delta);

            verify(inventoryDao).bulkUpdate(pojos);
        }
    }

    // ---------- aggregateItemsByProductId ----------

    @Test
    void testAggregateItemsByProductId() {
        List<InventoryPojo> list = List.of(
                inv("p1", 2),
                inv("p1", 3)
        );

        Map<String, Integer> result =
                inventoryApi.aggregateItemsByProductId(list);

        assertEquals(5, result.get("p1"));
    }

    // ---------- getInventoryForProductIds ----------

    @Test
    void testGetInventoryForProductIds() {
        InventoryPojo pojo = inv("p1", 10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(pojo));

        Map<String, InventoryPojo> result =
                inventoryApi.getInventoryForProductIds(List.of("p1"));

        assertEquals(pojo, result.get("p1"));
    }
}
