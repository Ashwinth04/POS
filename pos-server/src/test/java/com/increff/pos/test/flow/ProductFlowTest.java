package com.increff.pos.test.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductFlowTest {

    @InjectMocks
    private ProductFlow productFlow;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private InventoryApiImpl inventoryApi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------- addProduct ----------------
    @Test
    void shouldAddProductAndCreateInventory() throws ApiException {
        ProductPojo input = new ProductPojo();
        input.setId("p1");

        ProductPojo saved = new ProductPojo();
        saved.setId("p1");

        when(productApi.addProduct(input)).thenReturn(saved);
        doNothing().when(inventoryApi).createDummyInventoryRecord("p1");

        ProductPojo result = productFlow.addProduct(input);

        assertEquals("p1", result.getId());
        verify(productApi).addProduct(input);
        verify(inventoryApi).createDummyInventoryRecord("p1");
    }

    // ---------------- editProduct ----------------
    @Test
    void shouldEditProduct() throws ApiException {
        ProductPojo input = new ProductPojo();
        input.setId("p1");

        ProductPojo updated = new ProductPojo();
        updated.setId("p1");

        when(productApi.editProduct(input)).thenReturn(updated);

        ProductPojo result = productFlow.editProduct(input);

        assertEquals("p1", result.getId());
        verify(productApi).editProduct(input);
        verifyNoInteractions(inventoryApi); // Inventory not touched on edit
    }

    // ---------------- getAllProducts ----------------
    @Test
    void shouldGetAllProducts() {
        ProductPojo p1 = new ProductPojo();
        p1.setId("p1");

        Page<ProductPojo> page = new PageImpl<>(List.of(p1));
        when(productApi.getAllProducts(0, 10)).thenReturn(page);

        Page<ProductPojo> result = productFlow.getAllProducts(0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("p1", result.getContent().get(0).getId());
        verify(productApi).getAllProducts(0, 10);
    }

    // ---------------- getInventoryForProducts ----------------
    @Test
    void shouldGetInventoryForProducts() {
        ProductPojo p1 = new ProductPojo();
        p1.setId("p1");

        Page<ProductPojo> page = new PageImpl<>(List.of(p1));

        InventoryPojo inv = new InventoryPojo();
        inv.setProductId("p1");

        when(inventoryApi.getInventoryForProductIds(List.of("p1"))).thenReturn(Map.of("p1", inv));

        Map<String, InventoryPojo> result = productFlow.getInventoryForProducts(page);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("p1"));
        verify(inventoryApi).getInventoryForProductIds(List.of("p1"));
    }

    // ---------------- addProductsBulk ----------------
    @Test
    void shouldAddProductsBulkAndCreateInventory() throws ApiException {
        ProductPojo p1 = new ProductPojo();
        p1.setId("p1");
        ProductPojo p2 = new ProductPojo();
        p2.setId("p2");

        List<ProductPojo> input = List.of(p1, p2);
        List<ProductPojo> saved = List.of(p1, p2);

        when(productApi.addProductsBulk(input)).thenReturn(saved);
        doNothing().when(inventoryApi).createDummyInventoryRecord(anyString());

        productFlow.addProductsBulk(input);

        verify(productApi).addProductsBulk(input);
        verify(inventoryApi).createDummyInventoryRecord("p1");
        verify(inventoryApi).createDummyInventoryRecord("p2");
    }
}
