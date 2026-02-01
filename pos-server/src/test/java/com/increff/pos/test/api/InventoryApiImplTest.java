package com.increff.pos.test.api;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryApiImplTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApiImpl inventoryApi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- updateSingleInventory ----------

    @Test
    void shouldUpdateSingleInventory() throws ApiException {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(10);

        doNothing().when(inventoryDao).updateInventory(pojo);

        InventoryPojo result = inventoryApi.updateSingleInventory(pojo);

        assertEquals(pojo, result);
        verify(inventoryDao).updateInventory(pojo);
    }

    // ---------- bulkInventoryUpdate ----------

    @Test
    void shouldCallBulkUpdateIfListNotEmpty() {
        InventoryPojo p1 = new InventoryPojo();
        List<InventoryPojo> list = List.of(p1);

        // Use when().thenReturn() for non-void methods
        when(inventoryDao.bulkUpdate(list)).thenReturn(null); // or a map if needed

        inventoryApi.bulkInventoryUpdate(list);

        verify(inventoryDao).bulkUpdate(list);
    }


    @Test
    void shouldNotCallBulkUpdateIfListEmpty() {
        inventoryApi.bulkInventoryUpdate(Collections.emptyList());
        verify(inventoryDao, never()).bulkUpdate(anyList());
    }

    // ---------- reserveInventory ----------

    @Test
    void shouldReserveInventoryWhenFulfillable() throws ApiException {
        InventoryPojo item = new InventoryPojo();
        item.setProductId("p1");
        item.setQuantity(5);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(10);

        // Mock the DAO call used by fetchRecordsForOrderItems
        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(existing));
        when(inventoryDao.bulkUpdate(anyList())).thenReturn(null); // <-- FIXED

        boolean result = inventoryApi.reserveInventory(List.of(item));

        assertTrue(result);
        assertEquals(-5, item.getQuantity()); // quantity negated
        verify(inventoryDao).bulkUpdate(anyList());
    }


    @Test
    void shouldNotReserveInventoryIfNotFulfillable() throws ApiException {
        InventoryPojo item = new InventoryPojo();
        item.setProductId("p1");
        item.setQuantity(15);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("p1")))
                .thenReturn(List.of(existing));

        boolean result = inventoryApi.reserveInventory(List.of(item));

        assertFalse(result);
        verify(inventoryDao, never()).bulkUpdate(anyList());
    }

    // ---------- editOrder ----------

    // ---------- editOrder ----------
    @Test
    void shouldEditOrder() throws ApiException {
        Map<String, Integer> existing = Map.of("p1", 5);
        Map<String, Integer> incoming = Map.of("p1", 8);

        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(3);

        try (MockedStatic<InventoryHelper> helperMock = mockStatic(InventoryHelper.class)) {
            helperMock.when(() -> InventoryHelper.getPojosFromMap(anyMap()))
                    .thenReturn(List.of(pojo));

            // bulkUpdate returns a Map<String, String>, so stub the return value
            when(inventoryDao.bulkUpdate(anyList())).thenReturn(null);

            inventoryApi.editOrder(existing, incoming);

            helperMock.verify(() -> InventoryHelper.getPojosFromMap(Map.of("p1", 3)));
            verify(inventoryDao).bulkUpdate(List.of(pojo));
        }
    }

    // ---------- revertInventory ----------
    @Test
    void shouldRevertInventory() throws ApiException {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("p1");
        p1.setQuantity(5);

        // bulkUpdate returns a Map<String,String>, so stub with thenReturn
        when(inventoryDao.bulkUpdate(List.of(p1))).thenReturn(null);

        inventoryApi.revertInventory(List.of(p1));

        verify(inventoryDao).bulkUpdate(List.of(p1));
    }

    // ---------- createDummyInventoryRecord ----------
    @Test
    void shouldCreateDummyInventoryRecord() throws ApiException {
        // save returns a non-void value, so stub with thenReturn
        when(inventoryDao.save(any(InventoryPojo.class))).thenReturn(new InventoryPojo());

        inventoryApi.createDummyInventoryRecord("p1");

        ArgumentCaptor<InventoryPojo> captor = ArgumentCaptor.forClass(InventoryPojo.class);
        verify(inventoryDao).save(captor.capture());

        InventoryPojo saved = captor.getValue();
        assertEquals("p1", saved.getProductId());
        assertEquals(0, saved.getQuantity());
    }


    // ---------- fetchRecordsForOrderItems ----------

    @Test
    void shouldFetchRecordsForOrderItems() {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("p1");
        List<InventoryPojo> input = List.of(p1);

        when(inventoryDao.findByProductIds(List.of("p1"))).thenReturn(List.of(p1));

        Map<String, InventoryPojo> map = inventoryApi.fetchRecordsForOrderItems(input);

        assertEquals(1, map.size());
        assertEquals(p1, map.get("p1"));
    }

    // ---------- checkOrderFulfillable / isItemFulfillable ----------

    @Test
    void shouldReturnTrueIfOrderFulfillable() throws ApiException {
        InventoryPojo item = new InventoryPojo();
        item.setProductId("p1");
        item.setQuantity(5);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("p1"))).thenReturn(List.of(existing));

        assertTrue(inventoryApi.checkOrderFulfillable(List.of(item)));
    }

    @Test
    void shouldReturnFalseIfOrderNotFulfillable() throws ApiException {
        InventoryPojo item = new InventoryPojo();
        item.setProductId("p1");
        item.setQuantity(15);

        InventoryPojo existing = new InventoryPojo();
        existing.setProductId("p1");
        existing.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of("p1"))).thenReturn(List.of(existing));

        assertFalse(inventoryApi.checkOrderFulfillable(List.of(item)));
    }

    // ---------- aggregateItemsByProductId ----------

    @Test
    void shouldAggregateItemsByProductId() {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("p1");
        p1.setQuantity(5);

        InventoryPojo p2 = new InventoryPojo();
        p2.setProductId("p1");
        p2.setQuantity(3);

        InventoryPojo p3 = new InventoryPojo();
        p3.setProductId("p2");
        p3.setQuantity(7);

        Map<String, Integer> aggregated = inventoryApi.aggregateItemsByProductId(List.of(p1, p2, p3));

        assertEquals(2, aggregated.size());
        assertEquals(8, aggregated.get("p1"));
        assertEquals(7, aggregated.get("p2"));
    }

    // ---------- getInventoryForProductIds ----------

    @Test
    void shouldReturnInventoryMapForProductIds() {
        InventoryPojo p1 = new InventoryPojo();
        p1.setProductId("p1");

        InventoryPojo p2 = new InventoryPojo();
        p2.setProductId("p2");

        when(inventoryDao.findByProductIds(List.of("p1", "p2"))).thenReturn(List.of(p1, p2));

        Map<String, InventoryPojo> result = inventoryApi.getInventoryForProductIds(List.of("p1", "p2"));

        assertEquals(2, result.size());
        assertEquals(p1, result.get("p1"));
        assertEquals(p2, result.get("p2"));
    }
}
