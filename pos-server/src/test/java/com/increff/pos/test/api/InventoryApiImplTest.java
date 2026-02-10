package com.increff.pos.test.api;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiImplTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApiImpl inventoryApi;

    // ---------- updateSingleInventory ----------

    @Test
    void updateSingleInventory_success() throws ApiException {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(10);

        inventoryApi.updateSingleInventory(pojo);

        verify(inventoryDao).updateInventory(pojo);
    }

    // ---------- reserveInventory ----------

    @Test
    void reserveInventory_fulfillable() throws ApiException {
        InventoryPojo orderItem = new InventoryPojo();
        orderItem.setProductId("p1");
        orderItem.setQuantity(5);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(existing));

        boolean result = inventoryApi.reserveInventory(List.of(orderItem));

        assertTrue(result);
        assertEquals(-5, orderItem.getQuantity());
        verify(inventoryDao).bulkUpdate(anyList());
    }

    @Test
    void reserveInventory_notFulfillable() throws ApiException {
        InventoryPojo orderItem = new InventoryPojo();
        orderItem.setProductId("p1");
        orderItem.setQuantity(15);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(existing));

        boolean result = inventoryApi.reserveInventory(List.of(orderItem));

        assertFalse(result);
        verify(inventoryDao, never()).bulkUpdate(any());
    }

    // ---------- editOrder ----------

    @Test
    void calculateAndUpdateDeltaInventory_success() throws ApiException {
        Map<String, Integer> existing = Map.of("p1", 5);
        Map<String, Integer> incoming = Map.of("p1", 8, "p2", 2);

        try (MockedStatic<InventoryHelper> mocked = mockStatic(InventoryHelper.class)) {

            List<InventoryPojo> pojos = List.of(new InventoryPojo());
            mocked.when(() -> InventoryHelper.getPojosFromMap(anyMap()))
                    .thenReturn(pojos);

            inventoryApi.calculateAndUpdateDeltaInventory(existing, incoming);

            verify(inventoryDao).bulkUpdate(pojos);
        }
    }

    // ---------- revertInventory ----------

    @Test
    void revertInventory_success() {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(5);

        inventoryApi.revertInventory(List.of(pojo));

        verify(inventoryDao).bulkUpdate(anyList());
    }

    // ---------- createDummyInventoryRecord ----------

    @Test
    void createDummyInventoryRecord_success() {
        inventoryApi.createDummyInventoryRecord("p1");

        verify(inventoryDao).save(any(InventoryPojo.class));
    }

    // ---------- createDummyInventoryRecordsBulk ----------

    @Test
    void createDummyInventoryRecordsBulk_nullInput() {
        inventoryApi.createDummyInventoryRecordsBulk(null);

        verify(inventoryDao, never()).saveAll(any());
    }

    @Test
    void createDummyInventoryRecordsBulk_emptyList() {
        inventoryApi.createDummyInventoryRecordsBulk(List.of());

        verify(inventoryDao, never()).saveAll(any());
    }

    @Test
    void createDummyInventoryRecordsBulk_success() {
        inventoryApi.createDummyInventoryRecordsBulk(List.of("p1", "p2"));

        verify(inventoryDao).saveAll(anyList());
    }

    // ---------- checkOrderFulfillable ----------

    @Test
    void checkOrderFulfillable_true() {
        InventoryPojo item = new InventoryPojo();
        item.setProductId("p1");
        item.setQuantity(3);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(5);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(existing));

        boolean result = inventoryApi.checkOrderFulfillable(List.of(item));

        assertTrue(result);
    }

    @Test
    void checkOrderFulfillable_false() {
        InventoryPojo item = new InventoryPojo();
        item.setProductId("p1");
        item.setQuantity(10);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(5);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(existing));

        boolean result = inventoryApi.checkOrderFulfillable(List.of(item));

        assertFalse(result);
    }

    // ---------- updateBulkInventory ----------

    @Test
    void updateBulkInventory_success() {
        inventoryApi.updateBulkInventory(List.of(new InventoryPojo()));

        verify(inventoryDao).bulkUpdate(anyList());
    }

    // ---------- updateDeltaInventory ----------

    @Test
    void updateDeltaInventory_success() {
        try (MockedStatic<InventoryHelper> mocked = mockStatic(InventoryHelper.class)) {

            List<InventoryPojo> pojos = List.of(new InventoryPojo());
            mocked.when(() -> InventoryHelper.getPojosFromMap(anyMap()))
                    .thenReturn(pojos);

            inventoryApi.updateDeltaInventory(Map.of("p1", 5));

            verify(inventoryDao).bulkUpdate(pojos);
        }
    }

    // ---------- aggregateItemsByProductId ----------

    @Test
    void aggregateItemsByProductId_success() {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("p1");
        p1.setQuantity(5);

        InventoryPojo p2 = new InventoryPojo();
        p2.setProductId("p1");
        p2.setQuantity(3);

        Map<String, Integer> result =
                inventoryApi.aggregateItemsByProductId(List.of(p1, p2));

        assertEquals(8, result.get("p1"));
    }

    // ---------- getInventoryForProductIds ----------

    @Test
    void getInventoryForProductIds_success() {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(pojo));

        List<InventoryPojo> result =
                inventoryApi.getInventoryForProductIds(List.of("p1"));

        assertEquals(1, result.size());
        assertTrue(result.contains("p1"));
    }
}
