package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiImplTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApiImpl inventoryApi;

    private InventoryPojo validPojo;
    private InventoryPojo invalidPojo;

    @BeforeEach
    void setup() {
        validPojo = new InventoryPojo();
        validPojo.setBarcode("BAR123");
        validPojo.setQuantity(10);

        invalidPojo = new InventoryPojo();
        invalidPojo.setBarcode("INVALID123");
        invalidPojo.setQuantity(5);
    }

    // ---------- updateInventory ----------

    @Test
    void updateInventory_success() throws ApiException {
        doNothing().when(inventoryDao).updateInventory(validPojo);

        InventoryPojo result = inventoryApi.updateInventory(validPojo);

        assertEquals(validPojo, result);
        verify(inventoryDao).updateInventory(validPojo);
    }

    // ---------- bulkInventoryUpdate ----------

    @Test
    void bulkInventoryUpdate_allValid_updatesAll() {
        when(productDao.findExistingBarcodes(any()))
                .thenReturn(List.of("BAR123"));

        Map<String, String> result =
                inventoryApi.bulkInventoryUpdate(List.of(validPojo));

        assertTrue(result.isEmpty());
        verify(inventoryDao).bulkUpdate(List.of(validPojo));
    }

    @Test
    void bulkInventoryUpdate_someInvalid_returnsErrors() {
        when(productDao.findExistingBarcodes(any()))
                .thenReturn(List.of("BAR123")); // only validPojo exists

        Map<String, String> result =
                inventoryApi.bulkInventoryUpdate(List.of(validPojo, invalidPojo));

        assertEquals(1, result.size());
        assertEquals("Product with the given barcode doesn't exist",
                result.get("INVALID123"));

        verify(inventoryDao).bulkUpdate(List.of(validPojo));
    }

    @Test
    void bulkInventoryUpdate_allInvalid_noDbCall() {
        when(productDao.findExistingBarcodes(any()))
                .thenReturn(List.of()); // nothing exists

        Map<String, String> result =
                inventoryApi.bulkInventoryUpdate(List.of(invalidPojo));

        assertEquals(1, result.size());
        verify(inventoryDao, never()).bulkUpdate(any());
    }

    @Test
    void bulkInventoryUpdate_emptyInput() {
        Map<String, String> result = inventoryApi.bulkInventoryUpdate(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(productDao);
        verifyNoInteractions(inventoryDao);
    }

    @Test
    void bulkInventoryUpdate_nullInput() {
        Map<String, String> result = inventoryApi.bulkInventoryUpdate(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(productDao);
        verifyNoInteractions(inventoryDao);
    }
}
