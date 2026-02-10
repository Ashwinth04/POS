package com.increff.pos.test.flow;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.constants.ProductSearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductFlowTest {

    @InjectMocks
    private ProductFlow productFlow;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private InventoryApiImpl inventoryApi;

    @Mock
    private ClientApiImpl clientApi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- addProduct ----------
    @Test
    void testAddProduct() throws ApiException {
        ProductPojo p = new ProductPojo();
        p.setId("P1");

        when(productApi.addProduct(p)).thenReturn(p);
        doNothing().when(inventoryApi).createDummyInventoryRecord("P1");

        ProductPojo res = productFlow.addProduct(p);

        assertEquals(p, res);
        verify(productApi).addProduct(p);
        verify(inventoryApi).createDummyInventoryRecord("P1");
    }

    // ---------- addProductsBulk ----------
    @Test
    void testAddProductsBulk() {
        ProductPojo p1 = new ProductPojo();
        p1.setId("P1");
        ProductPojo p2 = new ProductPojo();
        p2.setId("P2");

        List<ProductPojo> inputList = List.of(p1, p2);

        when(productApi.addProductsBulk(inputList)).thenReturn(inputList);
        doNothing().when(inventoryApi).createDummyInventoryRecordsBulk(List.of("P1", "P2"));

        productFlow.addProductsBulk(inputList);

        verify(productApi).addProductsBulk(inputList);
        verify(inventoryApi).createDummyInventoryRecordsBulk(List.of("P1", "P2"));
    }

    // ---------- getInventoryForProducts ----------
    @Test
    void testGetInventoryForProducts() {
        ProductPojo p1 = new ProductPojo();
        p1.setId("P1");
        ProductPojo p2 = new ProductPojo();
        p2.setId("P2");

        InventoryPojo i1 = new InventoryPojo();
        i1.setProductId("P1");
        InventoryPojo i2 = new InventoryPojo();
        i2.setProductId("P2");

        Page<ProductPojo> page = new PageImpl<>(List.of(p1, p2));
        when(inventoryApi.getInventoryForProductIds(List.of("P1", "P2")))
                .thenReturn(List.of(i1, i2));

        Map<String, InventoryPojo> map = productFlow.getInventoryForProducts(page);

        assertEquals(2, map.size());
        assertEquals(i1, map.get("P1"));
        assertEquals(i2, map.get("P2"));
    }

    // ---------- searchProducts ----------
    @Test
    void testSearchProducts() throws ApiException {
        Page<ProductPojo> page = new PageImpl<>(List.of());
        when(productApi.searchProducts(ProductSearchType.BARCODE, "q", 0, 10))
                .thenReturn(page);

        Page<ProductPojo> result = productFlow.searchProducts(ProductSearchType.BARCODE, "q", 0, 10);
        assertEquals(page, result);
        verify(productApi).searchProducts(ProductSearchType.BARCODE, "q", 0, 10);
    }

    // ---------- getCheckByClientName ----------
    @Test
    void testGetCheckByClientName() throws ApiException {
        ClientPojo client = new ClientPojo();
        client.setName("C1");

        when(clientApi.getCheckByClientName("C1")).thenReturn(client);

        ClientPojo result = productFlow.getCheckByClientName("C1");

        assertEquals(client, result);
        verify(clientApi).getCheckByClientName("C1");
    }

    // ---------- fetchExistingClientNames ----------
    @Test
    void testFetchExistingClientNames() {
        ClientPojo c1 = new ClientPojo();
        c1.setName("A");
        ClientPojo c2 = new ClientPojo();
        c2.setName("B");

        List<ClientPojo> clients = List.of(c1, c2);
        List<String> names = List.of("A", "B");

        when(clientApi.fetchExistingClientNames(names)).thenReturn(clients);

        Map<String, ClientPojo> map = productFlow.fetchExistingClientNames(names);

        assertEquals(2, map.size());
        assertEquals(c1, map.get("A"));
        assertEquals(c2, map.get("B"));
        verify(clientApi).fetchExistingClientNames(names);
    }
}
