package com.increff.pos.test.api;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.constants.ProductSearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductApiImplTest {

    @InjectMocks
    private ProductApiImpl productApi;

    @Mock
    private ProductDao productDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- addProduct ----------
    @Test
    void testAddProductSuccess() throws Exception {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("1");

        when(productDao.findByBarcode("1")).thenReturn(null);
        when(productDao.save(pojo)).thenReturn(pojo);

        ProductPojo result = productApi.addProduct(pojo);

        assertEquals(pojo, result);
        verify(productDao).save(pojo);
    }

    @Test
    void testAddProductBarcodeExists() {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("1");

        when(productDao.findByBarcode("1")).thenReturn(new ProductPojo());

        assertThrows(ApiException.class,
                () -> productApi.addProduct(pojo));
    }

    // ---------- editProduct ----------
    @Test
    void testEditProductSuccess() throws Exception {
        ProductPojo existing = new ProductPojo();
        existing.setBarcode("1");

        ProductPojo update = new ProductPojo();
        update.setBarcode("1");

        when(productDao.findByBarcode("1")).thenReturn(existing);
        when(productDao.save(update)).thenReturn(update);

        ProductPojo result = productApi.editProduct(update);

        assertEquals(update, result);
        verify(productDao).save(update);
    }

    @Test
    void testEditProductNotFound() {
        ProductPojo update = new ProductPojo();
        update.setBarcode("1");

        when(productDao.findByBarcode("1")).thenReturn(null);

        assertThrows(ApiException.class,
                () -> productApi.editProduct(update));
    }

    // ---------- getAllProducts ----------
    @Test
    void testGetAllProducts() {
        Page<ProductPojo> page = new PageImpl<>(List.of(new ProductPojo()));
        when(productDao.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProductPojo> result = productApi.getAllProducts(0,10);

        assertEquals(1, result.getContent().size());
    }

    // ---------- addProductsBulk ----------
    @Test
    void testAddProductsBulk() {
        List<ProductPojo> list = List.of(new ProductPojo());
        when(productDao.saveAll(list)).thenReturn(list);

        List<ProductPojo> result = productApi.addProductsBulk(list);

        assertEquals(1, result.size());
    }

    // ---------- checkBarcodeExists ----------
    @Test
    void testCheckBarcodeExistsThrows() {
        when(productDao.findByBarcode("1")).thenReturn(new ProductPojo());

        assertThrows(ApiException.class,
                () -> productApi.checkBarcodeExists("1"));
    }

    @Test
    void testCheckBarcodeExistsPass() throws Exception {
        when(productDao.findByBarcode("1")).thenReturn(null);

        productApi.checkBarcodeExists("1");

        verify(productDao).findByBarcode("1");
    }

    // ---------- findExistingProducts ----------
    @Test
    void testFindExistingProducts() {
        when(productDao.findByBarcodes(any()))
                .thenReturn(List.of(new ProductPojo()));

        List<ProductPojo> result =
                productApi.findExistingProducts(List.of("1"));

        assertEquals(1, result.size());
    }

    // ---------- getProductPojosForBarcodes ----------
    @Test
    void testGetProductPojosForBarcodesEmpty() {
        List<ProductPojo> result =
                productApi.getProductPojosForBarcodes(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProductPojosForBarcodesSuccess() {
        when(productDao.findByBarcodes(any()))
                .thenReturn(List.of(new ProductPojo()));

        List<ProductPojo> result =
                productApi.getProductPojosForBarcodes(List.of("1"));

        assertEquals(1, result.size());
    }

    // ---------- getProductPojosForProductIds ----------
    @Test
    void testGetProductPojosForProductIdsEmpty() {
        List<ProductPojo> result =
                productApi.getProductPojosForProductIds(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProductPojosForProductIdsSuccess() {
        when(productDao.findAllById(any()))
                .thenReturn(List.of(new ProductPojo()));

        List<ProductPojo> result =
                productApi.getProductPojosForProductIds(List.of("1"));

        assertEquals(1, result.size());
    }

    // ---------- getCheckByBarcode ----------
    @Test
    void testGetCheckByBarcodeSuccess() throws Exception {
        ProductPojo pojo = new ProductPojo();
        when(productDao.findByBarcode("1")).thenReturn(pojo);

        ProductPojo result = productApi.getCheckByBarcode("1");

        assertEquals(pojo, result);
    }

    @Test
    void testGetCheckByBarcodeThrows() {
        when(productDao.findByBarcode("1")).thenReturn(null);

        assertThrows(ApiException.class,
                () -> productApi.getCheckByBarcode("1"));
    }

    // ---------- searchProducts ----------
    @Test
    void testSearchProductsBarcode() throws Exception {
        Page<ProductPojo> page =
                new PageImpl<>(List.of(new ProductPojo()));

        when(productDao.searchByBarcode(eq("1"), any()))
                .thenReturn(page);

        Page<ProductPojo> result =
                productApi.searchProducts(ProductSearchType.BARCODE, "1", 0, 10);

        assertEquals(page, result);
    }

    @Test
    void testSearchProductsName() throws Exception {
        Page<ProductPojo> page =
                new PageImpl<>(List.of(new ProductPojo()));

        when(productDao.searchByName(eq("A"), any()))
                .thenReturn(page);

        Page<ProductPojo> result =
                productApi.searchProducts(ProductSearchType.NAME, "A", 0, 10);

        assertEquals(page, result);
    }
}
