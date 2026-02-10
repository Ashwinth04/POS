package com.increff.pos.test.api;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.helper.InventoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryApiImplTest {

    @InjectMocks
    private InventoryApiImpl inventoryApi;

    @Mock
    private InventoryDao inventoryDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- updateSingleInventory ----------------
    @Test
    void testUpdateSingleInventory() throws Exception {
        InventoryPojo pojo = new InventoryPojo();
        inventoryApi.updateSingleInventory(pojo);
        verify(inventoryDao).updateInventory(pojo);
    }

    // ---------------- reserveInventory ----------------
    @Test
    void testReserveInventorySuccess() {
        InventoryPojo p = new InventoryPojo();
        p.setProductId("p1");
        p.setQuantity(5);

        InventoryPojo dbPojo = new InventoryPojo();
        dbPojo.setProductId("p1");
        dbPojo.setQuantity(10);

        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(dbPojo));

        boolean result = inventoryApi.reserveInventory(List.of(p));

        assertTrue(result);
        verify(inventoryDao).bulkUpdate(any());
    }

    @Test
    void testReserveInventoryFail() {
        InventoryPojo p = new InventoryPojo();
        p.setProductId("p1");
        p.setQuantity(50);

        InventoryPojo dbPojo = new InventoryPojo();
        dbPojo.setProductId("p1");
        dbPojo.setQuantity(10);

        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(dbPojo));

        boolean result = inventoryApi.reserveInventory(List.of(p));

        assertFalse(result);
        verify(inventoryDao, never()).bulkUpdate(any());
    }

    // ---------------- createDummyInventoryRecord ----------------
    @Test
    void testCreateDummyInventoryRecord() {
        inventoryApi.createDummyInventoryRecord("p1");
        verify(inventoryDao).save(any());
    }

    // ---------------- createDummyInventoryRecordsBulk ----------------
    @Test
    void testCreateDummyInventoryRecordsBulkEmpty() {
        inventoryApi.createDummyInventoryRecordsBulk(Collections.emptyList());
        verify(inventoryDao, never()).saveAll(any());
    }

    @Test
    void testCreateDummyInventoryRecordsBulkSuccess() {
        inventoryApi.createDummyInventoryRecordsBulk(List.of("p1","p2"));
        verify(inventoryDao).saveAll(any());
    }

    // ---------------- updateBulkInventory ----------------
    @Test
    void testUpdateBulkInventoryClamp() {
        InventoryPojo update = new InventoryPojo();
        update.setProductId("p1");
        update.setQuantity(-20);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(5);

        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(existing));

        List<String> clamped =
                inventoryApi.updateBulkInventory(List.of(update));

        assertEquals(1, clamped.size());
        verify(inventoryDao).bulkUpdate(any());
    }

    @Test
    void testUpdateBulkInventoryNormal() {
        InventoryPojo update = new InventoryPojo();
        update.setProductId("p1");
        update.setQuantity(10);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(5);

        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(existing));

        List<String> clamped =
                inventoryApi.updateBulkInventory(List.of(update));

        assertTrue(clamped.isEmpty());
    }

    // ---------------- aggregateItemsByProductId ----------------
    @Test
    void testAggregateItemsByProductId() {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("p1");
        p1.setQuantity(5);

        InventoryPojo p2 = new InventoryPojo();
        p2.setProductId("p1");
        p2.setQuantity(10);

        Map<String,Integer> map =
                inventoryApi.aggregateItemsByProductId(List.of(p1,p2));

        assertEquals(15, map.get("p1"));
    }

    // ---------------- getInventoryForProductIds ----------------
    @Test
    void testGetInventoryForProductIds() {
        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(new InventoryPojo()));

        List<InventoryPojo> list =
                inventoryApi.getInventoryForProductIds(List.of("p1"));

        assertEquals(1, list.size());
    }

    // ---------------- calculateAndUpdateDeltaInventory ----------------
    @Test
    void testCalculateAndUpdateDeltaInventory() {
        Map<String,Integer> existing = Map.of("p1",5);
        Map<String,Integer> incoming = Map.of("p1",10);

        inventoryApi.calculateAndUpdateDeltaInventory(existing,incoming);

        verify(inventoryDao).bulkUpdate(any());
    }

    // ---------------- updateDeltaInventory ----------------
    @Test
    void testUpdateDeltaInventory() {
        Map<String,Integer> delta = Map.of("p1",5);
        inventoryApi.updateDeltaInventory(delta);
        verify(inventoryDao).bulkUpdate(any());
    }

    // ---------------- checkOrderFulfillable ----------------
    @Test
    void testCheckOrderFulfillableTrue() {
        InventoryPojo p = new InventoryPojo();
        p.setProductId("p1");
        p.setQuantity(5);

        InventoryPojo db = new InventoryPojo();
        db.setProductId("p1");
        db.setQuantity(10);

        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(db));

        assertTrue(inventoryApi.checkOrderFulfillable(List.of(p)));
    }

    @Test
    void testCheckOrderFulfillableFalse() {
        InventoryPojo p = new InventoryPojo();
        p.setProductId("p1");
        p.setQuantity(50);

        InventoryPojo db = new InventoryPojo();
        db.setProductId("p1");
        db.setQuantity(10);

        when(inventoryDao.findByProductIds(any()))
                .thenReturn(List.of(db));

        assertFalse(inventoryApi.checkOrderFulfillable(List.of(p)));
    }
}
