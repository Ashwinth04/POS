package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiImplTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private InventoryDao inventoryDao;

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ProductApiImpl productApi;

    private ProductPojo product;
    private ClientPojo client;

    @BeforeEach
    void setup() {
        client = new ClientPojo();
        client.setId("c1");
        client.setName("ClientA");

        product = new ProductPojo();
        product.setId("p1");
        product.setBarcode("BAR123");
        product.setName("Product");
        product.setClientName("ClientA");
        product.setMrp(100.0);
    }

    // ---------- addProduct ----------

    @Test
    void addProduct_success() throws ApiException {
        when(clientDao.findByName("ClientA")).thenReturn(client);
        when(productDao.findByBarcode("BAR123")).thenReturn(null);
        when(productDao.save(product)).thenReturn(product);
        when(inventoryDao.save(any())).thenReturn(new InventoryPojo());

        ProductPojo result = productApi.addProduct(product);

        assertNotNull(result);
        verify(productDao).save(product);
        verify(inventoryDao).save(any());
    }

    @Test
    void addProduct_clientMissing_throwsException() {
        when(clientDao.findByName("ClientA")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.addProduct(product));

        assertEquals("Client with the given name does not exist", ex.getMessage());
    }

    @Test
    void addProduct_duplicateBarcode_throwsException() {
        when(clientDao.findByName("ClientA")).thenReturn(client);
        when(productDao.findByBarcode("BAR123")).thenReturn(product);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.addProduct(product));

        assertEquals("Barcode already exists", ex.getMessage());
    }

    // ---------- editProduct ----------

    @Test
    void editProduct_success() throws ApiException {
        when(clientDao.findByName("ClientA")).thenReturn(client);
        when(productDao.findByBarcode("BAR123")).thenReturn(product);
        when(productDao.save(any())).thenReturn(product);

        ProductPojo updated = productApi.editProduct(product);

        assertNotNull(updated);
        verify(productDao).save(any());
    }

    @Test
    void editProduct_notFound() {
        when(clientDao.findByName("ClientA")).thenReturn(client);
        when(productDao.findByBarcode("BAR123")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.editProduct(product));

        assertEquals("Product with this given barcode doesn't exist", ex.getMessage());
    }

    // ---------- addProductsBulk ----------

    @Test
    void addProductsBulk_success() {
        when(clientDao.findExistingClientNames(any()))
                .thenReturn(List.of("ClientA"));

        when(productDao.findByBarcode(any())).thenReturn(null);
        when(productDao.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(inventoryDao.save(any())).thenReturn(new InventoryPojo());

        Map<String, ProductUploadResult> result =
                productApi.addProductsBulk(List.of(product));

        assertTrue(result.isEmpty()); // success removes from resultMap
    }

    @Test
    void addProductsBulk_invalidClient() {
        when(clientDao.findExistingClientNames(any()))
                .thenReturn(List.of()); // no valid clients

        Map<String, ProductUploadResult> result =
                productApi.addProductsBulk(List.of(product));

        ProductUploadResult r = result.get("BAR123");
        assertEquals("FAILED", r.getStatus());
        assertEquals("Client with the given name does not exist", r.getMessage());
    }

    @Test
    void addProductsBulk_duplicateBarcode() {
        when(clientDao.findExistingClientNames(any()))
                .thenReturn(List.of("ClientA"));
        when(productDao.findByBarcode("BAR123")).thenReturn(product);

        Map<String, ProductUploadResult> result =
                productApi.addProductsBulk(List.of(product));

        ProductUploadResult r = result.get("BAR123");
        assertEquals("FAILED", r.getStatus());
        assertEquals("Product with the given barcode already exists", r.getMessage());
    }

    // ---------- getAllProducts ----------

    @Test
    void getAllProducts_returnsPage() {
        Page<ProductPojo> page = new PageImpl<>(List.of(product));
        when(productDao.findAll((Pageable) any())).thenReturn(page);

        Page<ProductPojo> result = productApi.getAllProducts(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    // ---------- dummy inventory ----------

    @Test
    void getDummyInventoryRecord_setsZeroQuantity() throws ApiException {
        InventoryPojo inv = productApi.getDummyInventoryRecord(product);

        assertEquals("BAR123", inv.getBarcode());
        assertEquals(0, inv.getQuantity());
    }
}
