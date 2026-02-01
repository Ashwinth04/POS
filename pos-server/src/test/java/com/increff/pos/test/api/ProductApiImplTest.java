package com.increff.pos.test.api;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductApiImpl productApi;

    // ---------- addProduct ----------

    @Test
    void shouldAddProductWhenBarcodeNotExists() throws ApiException {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(null);
        when(productDao.save(pojo)).thenReturn(pojo);

        ProductPojo result = productApi.addProduct(pojo);

        assertEquals(pojo, result);
        verify(productDao).save(pojo);
    }

    @Test
    void shouldThrowExceptionIfBarcodeExists() {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(new ProductPojo());

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.addProduct(pojo));

        assertEquals("Barcode already exists", ex.getMessage());
    }

    // ---------- editProduct ----------

    @Test
    void shouldEditProduct() throws ApiException {
        ProductPojo existing = new ProductPojo();
        existing.setId("id1");
        existing.setBarcode("b1");

        ProductPojo updated = new ProductPojo();
        updated.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(existing);
        when(productDao.save(any(ProductPojo.class))).thenAnswer(i -> i.getArgument(0));

        ProductPojo result = productApi.editProduct(updated);

        assertEquals("id1", result.getId());
        verify(productDao).save(updated);
    }

    // ---------- getAllProducts ----------

    @Test
    void shouldGetAllProducts() {
        Page<ProductPojo> page =
                new PageImpl<>(List.of(new ProductPojo()));

        when(productDao.findAll(any(PageRequest.class))).thenReturn(page);

        Page<ProductPojo> result = productApi.getAllProducts(0, 10);

        assertEquals(1, result.getContent().size());
    }

    // ---------- addProductsBulk ----------

    @Test
    void shouldAddProductsBulk() throws ApiException {
        List<ProductPojo> pojos = List.of(new ProductPojo());

        when(productDao.saveAll(pojos)).thenReturn(pojos);

        List<ProductPojo> result = productApi.addProductsBulk(pojos);

        assertEquals(1, result.size());
        verify(productDao).saveAll(pojos);
    }

    @Test
    void shouldThrowExceptionWhenBulkInsertFails() {
        List<ProductPojo> pojos = List.of(new ProductPojo());

        when(productDao.saveAll(pojos)).thenThrow(new RuntimeException());

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.addProductsBulk(pojos));

        assertEquals("Failed to insert valid products", ex.getMessage());
    }

    // ---------- validateAllOrderItems ----------

    @Test
    void shouldValidateAllOrderItemsSuccessfully() throws ApiException {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setSellingPrice(50.0);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        ProductPojo product = new ProductPojo();
        product.setBarcode("b1");
        product.setMrp(100.0);

        when(productDao.findByBarcodes(List.of("b1")))
                .thenReturn(List.of(product));

        productApi.validateAllOrderItems(order);

        // no exception = success
    }

    @Test
    void shouldFailValidationForInvalidBarcode() {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setSellingPrice(50.0);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        when(productDao.findByBarcodes(List.of("b1")))
                .thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.validateAllOrderItems(order));

        assertEquals("Invalid barcode: b1", ex.getMessage());
    }

    @Test
    void shouldFailValidationWhenSellingPriceExceedsMrp() {
        OrderItem item = new OrderItem();
        item.setBarcode("b1");
        item.setSellingPrice(200.0);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        ProductPojo product = new ProductPojo();
        product.setBarcode("b1");
        product.setMrp(100.0);

        when(productDao.findByBarcodes(List.of("b1")))
                .thenReturn(List.of(product));

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.validateAllOrderItems(order));

        assertEquals("Selling price exceeds MRP for barcode: b1", ex.getMessage());
    }

    // ---------- findExistingProducts ----------

    @Test
    void shouldFindExistingProducts() {
        ProductPojo p = new ProductPojo();
        p.setBarcode("b1");

        when(productDao.findByBarcodes(List.of("b1")))
                .thenReturn(List.of(p));

        List<String> result = productApi.findExistingProducts(List.of("b1"));

        assertEquals(List.of("b1"), result);
    }

    // ---------- mapBarcodesToProductIds ----------

    @Test
    void shouldMapBarcodesToProductIds() {
        ProductPojo p = new ProductPojo();
        p.setBarcode("b1");
        p.setId("id1");

        when(productDao.findByBarcodes(List.of("b1")))
                .thenReturn(List.of(p));

        Map<String, String> result =
                productApi.mapBarcodesToProductIds(List.of("b1"));

        assertEquals("id1", result.get("b1"));
    }

    @Test
    void shouldReturnEmptyMapWhenBarcodesEmpty() {
        Map<String, String> result =
                productApi.mapBarcodesToProductIds(List.of());

        assertTrue(result.isEmpty());
        verify(productDao, never()).findByBarcodes(anyList());
    }

    // ---------- getCheckByBarcode ----------

    @Test
    void shouldGetProductByBarcode() throws ApiException {
        ProductPojo p = new ProductPojo();
        p.setBarcode("b1");

        when(productDao.findByBarcode("b1")).thenReturn(p);

        ProductPojo result = productApi.getCheckByBarcode("b1");

        assertEquals(p, result);
    }

    @Test
    void shouldThrowIfProductNotFoundByBarcode() {
        when(productDao.findByBarcode("b1")).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> productApi.getCheckByBarcode("b1"));

        assertEquals(
                "Product with this given barcode doesn't exist",
                ex.getMessage()
        );
    }
}
