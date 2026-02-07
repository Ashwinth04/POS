package com.increff.pos.test.api;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
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

import static com.increff.pos.model.constants.ProductSearchType.BARCODE;
import static com.increff.pos.model.constants.ProductSearchType.NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductApiImpl productApi;

    // ---------- addProduct ----------

    @Test
    void addProduct_success() throws ApiException {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(null);
        when(productDao.save(pojo)).thenReturn(pojo);

        ProductPojo result = productApi.addProduct(pojo);

        assertEquals("b1", result.getBarcode());
        verify(productDao).save(pojo);
    }

    @Test
    void addProduct_barcodeExists() {
        ProductPojo existing = new ProductPojo();
        existing.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(existing);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.addProduct(new ProductPojo() {{
                    setBarcode("b1");
                }}));

        assertEquals("Barcode already exists", ex.getMessage());
    }

    // ---------- editProduct ----------

    @Test
    void editProduct_success() throws ApiException {
        ProductPojo existing = new ProductPojo();
        existing.setId("10");
        existing.setBarcode("b1");

        ProductPojo updated = new ProductPojo();
        updated.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(existing);
        when(productDao.save(any(ProductPojo.class)))
                .thenAnswer(i -> i.getArgument(0));

        ProductPojo result = productApi.editProduct(updated);

        assertEquals("10", result.getId());
        verify(productDao).save(updated);
    }

    @Test
    void editProduct_notFound() {
        when(productDao.findByBarcode("b1")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.editProduct(new ProductPojo() {{
                    setBarcode("b1");
                }}));

        assertEquals("Product with this given barcode doesn't exist", ex.getMessage());
    }

    // ---------- getAllProducts ----------

    @Test
    void getAllProducts_success() {
        Page<ProductPojo> page = new PageImpl<>(List.of(new ProductPojo()));
        when(productDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProductPojo> result = productApi.getAllProducts(0, 10);

        assertEquals(1, result.getContent().size());
    }

    // ---------- addProductsBulk ----------

    @Test
    void addProductsBulk_success() throws ApiException {
        List<ProductPojo> products = List.of(new ProductPojo(), new ProductPojo());

        when(productDao.saveAll(products)).thenReturn(products);

        List<ProductPojo> result = productApi.addProductsBulk(products);

        assertEquals(2, result.size());
    }

    @Test
    void addProductsBulk_exception() {
        when(productDao.saveAll(any()))
                .thenThrow(new RuntimeException("DB error"));

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.addProductsBulk(List.of(new ProductPojo())));

        assertEquals("Failed to insert valid products", ex.getMessage());
    }

    // ---------- checkBarcodeExists ----------

    @Test
    void checkBarcodeExists_success() throws ApiException {
        when(productDao.findByBarcode("b1")).thenReturn(null);

        assertDoesNotThrow(() -> productApi.checkBarcodeExists("b1"));
    }

    @Test
    void checkBarcodeExists_throwsException() {
        when(productDao.findByBarcode("b1")).thenReturn(new ProductPojo());

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.checkBarcodeExists("b1"));

        assertEquals("Barcode already exists", ex.getMessage());
    }

    // ---------- findExistingProducts ----------

    @Test
    void findExistingProducts_success() {
        ProductPojo p1 = new ProductPojo();
        p1.setBarcode("b1");

        ProductPojo p2 = new ProductPojo();
        p2.setBarcode("b2");

        when(productDao.findByBarcodes(List.of("b1", "b2")))
                .thenReturn(List.of(p1, p2));

        Map<String, ProductPojo> result =
                productApi.findExistingProducts(List.of("b1", "b2"));

        assertEquals(2, result.size());
        assertTrue(result.containsKey("b1"));
        assertTrue(result.containsKey("b2"));
    }

    // ---------- mapBarcodesToProductPojos ----------

    @Test
    void mapBarcodesToProductPojos_nullInput() {
        Map<String, ProductPojo> result =
                productApi.mapBarcodesToProductPojos(null);

        assertTrue(result.isEmpty());
        verify(productDao, never()).findByBarcodes(any());
    }

    @Test
    void mapBarcodesToProductPojos_emptyList() {
        Map<String, ProductPojo> result =
                productApi.mapBarcodesToProductPojos(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void mapBarcodesToProductPojos_success() {
        ProductPojo p = new ProductPojo();
        p.setBarcode("b1");

        when(productDao.findByBarcodes(List.of("b1")))
                .thenReturn(List.of(p));

        Map<String, ProductPojo> result =
                productApi.mapBarcodesToProductPojos(List.of("b1"));

        assertEquals(1, result.size());
        assertEquals(p, result.get("b1"));
    }

    // ---------- getCheckByBarcode ----------

    @Test
    void getCheckByBarcode_success() throws ApiException {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(pojo);

        ProductPojo result = productApi.getCheckByBarcode("b1");

        assertEquals("b1", result.getBarcode());
    }

    @Test
    void getCheckByBarcode_notFound() {
        when(productDao.findByBarcode("b1")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.getCheckByBarcode("b1"));

        assertEquals("Product with this given barcode doesn't exist", ex.getMessage());
    }

    // ---------- searchProducts ----------

    @Test
    void searchProducts_byBarcode() throws ApiException {
        Page<ProductPojo> page = new PageImpl<>(List.of(new ProductPojo()));

        when(productDao.searchByBarcode(eq("b1"), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductPojo> result =
                productApi.searchProducts(BARCODE, "b1", 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchProducts_byName() throws ApiException {
        Page<ProductPojo> page = new PageImpl<>(List.of(new ProductPojo()));

        when(productDao.searchByName(eq("prod"), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductPojo> result =
                productApi.searchProducts(NAME, "prod", 0, 10);

        assertEquals(1, result.getContent().size());
    }

//    @Test
//    void searchProducts_invalidType() {
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.searchProducts("", "x", 0, 10));
//
//        assertEquals("Invalid search type: invalid", ex.getMessage());
//    }
}
