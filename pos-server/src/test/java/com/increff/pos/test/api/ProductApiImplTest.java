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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductApiImpl productApi;

    // ---------- addProduct ----------

    @Test
    void testAddProduct_success() throws ApiException {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("B1");

        when(productDao.findByBarcode("B1")).thenReturn(null);
        when(productDao.save(pojo)).thenReturn(pojo);

        ProductPojo result = productApi.addProduct(pojo);

        assertThat(result).isEqualTo(pojo);
    }

    @Test
    void testAddProduct_barcodeExists() {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("B1");

        when(productDao.findByBarcode("B1"))
                .thenReturn(new ProductPojo());

        assertThatThrownBy(() -> productApi.addProduct(pojo))
                .isInstanceOf(ApiException.class)
                .hasMessage("Barcode already exists");
    }

    // ---------- editProduct ----------

    @Test
    void testEditProduct_success() throws ApiException {
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode("B1");

        ProductPojo existing = new ProductPojo();
        existing.setId("DB_ID");

        when(productDao.findByBarcode("B1")).thenReturn(existing);
        when(productDao.save(pojo)).thenReturn(pojo);

        ProductPojo result = productApi.editProduct(pojo);

        assertThat(pojo.getId()).isEqualTo("DB_ID");
        assertThat(result).isEqualTo(pojo);
    }

    // ---------- getAllProducts ----------

    @Test
    void testGetAllProducts_success() {
        Page<ProductPojo> page =
                new PageImpl<>(List.of(new ProductPojo()));

        when(productDao.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<ProductPojo> result =
                productApi.getAllProducts(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    // ---------- addProductsBulk ----------

    @Test
    void testAddProductsBulk_success() throws ApiException {
        List<ProductPojo> list = List.of(new ProductPojo());

        when(productDao.saveAll(list)).thenReturn(list);

        List<ProductPojo> result =
                productApi.addProductsBulk(list);

        assertThat(result).hasSize(1);
    }

    @Test
    void testAddProductsBulk_failure() {
        List<ProductPojo> list = List.of(new ProductPojo());

        when(productDao.saveAll(list))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> productApi.addProductsBulk(list))
                .isInstanceOf(ApiException.class)
                .hasMessage("Failed to insert valid products");
    }

    // ---------- validateAllOrderItems ----------

    @Test
    void testValidateAllOrderItems_success() throws ApiException {
        OrderItem item = new OrderItem();
        item.setBarcode("B1");
        item.setSellingPrice(50.0);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        ProductPojo product = new ProductPojo();
        product.setBarcode("B1");
        product.setMrp(100.0);

        when(productDao.findByBarcodes(List.of("B1")))
                .thenReturn(List.of(product));

        productApi.validateAllOrderItems(order);
    }

    @Test
    void testValidateAllOrderItems_invalidBarcode() {
        OrderItem item = new OrderItem();
        item.setBarcode("B1");
        item.setSellingPrice(50.0);

        OrderPojo order = new OrderPojo();
        order.setOrderItems(List.of(item));

        when(productDao.findByBarcodes(List.of("B1")))
                .thenReturn(List.of());

        assertThatThrownBy(() -> productApi.validateAllOrderItems(order))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid barcode: B1");
    }

    // ---------- validateItem ----------

    @Test
    void testValidateItem_priceExceedsMrp() {
        OrderItem item = new OrderItem();
        item.setBarcode("B1");
        item.setSellingPrice(200.0);

        ProductPojo product = new ProductPojo();
        product.setMrp(100.0);

        assertThatThrownBy(() ->
                productApi.validateItem(item, Map.of("B1", product)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Selling price exceeds MRP for barcode: B1");
    }

    // ---------- findExistingProducts ----------

    @Test
    void testFindExistingProducts_success() {
        ProductPojo p = new ProductPojo();
        p.setBarcode("B1");

        when(productDao.findByBarcodes(List.of("B1")))
                .thenReturn(List.of(p));

        List<String> result =
                productApi.findExistingProducts(List.of("B1"));

        assertThat(result).containsExactly("B1");
    }

    // ---------- mapBarcodesToProductIds ----------

    @Test
    void testMapBarcodesToProductIds_empty() {
        Map<String, String> result =
                productApi.mapBarcodesToProductIds(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void testMapBarcodesToProductIds_success() {
        ProductPojo p = new ProductPojo();
        p.setBarcode("B1");
        p.setId("P1");

        when(productDao.findByBarcodes(List.of("B1")))
                .thenReturn(List.of(p));

        Map<String, String> result =
                productApi.mapBarcodesToProductIds(List.of("B1"));

        assertThat(result).containsEntry("B1", "P1");
    }

    // ---------- getCheckByBarcode ----------

    @Test
    void testGetCheckByBarcode_success() throws ApiException {
        ProductPojo pojo = new ProductPojo();

        when(productDao.findByBarcode("B1"))
                .thenReturn(pojo);

        ProductPojo result =
                productApi.getCheckByBarcode("B1");

        assertThat(result).isEqualTo(pojo);
    }

    @Test
    void testGetCheckByBarcode_notFound() {
        when(productDao.findByBarcode("B1"))
                .thenReturn(null);

        assertThatThrownBy(() ->
                productApi.getCheckByBarcode("B1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Product with this given barcode doesn't exist");
    }

    // ---------- searchProducts ----------

    @Test
    void testSearchProducts_byBarcode() throws ApiException {
        when(productDao.searchByBarcode(any(), any()))
                .thenReturn(Page.empty());

        Page<ProductPojo> result =
                productApi.searchProducts("barcode", "B1", 0, 10);

        assertThat(result).isNotNull();
    }

    @Test
    void testSearchProducts_byName() throws ApiException {
        when(productDao.searchByName(any(), any()))
                .thenReturn(Page.empty());

        Page<ProductPojo> result =
                productApi.searchProducts("name", "ABC", 0, 10);

        assertThat(result).isNotNull();
    }

    @Test
    void testSearchProducts_invalidType() {
        assertThatThrownBy(() ->
                productApi.searchProducts("invalid", "X", 0, 10))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid search type: invalid");
    }
}
