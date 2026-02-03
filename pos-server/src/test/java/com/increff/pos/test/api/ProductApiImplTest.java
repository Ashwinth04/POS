//package com.increff.pos.test.api;
//
//import com.increff.pos.api.ProductApiImpl;
//import com.increff.pos.dao.ProductDao;
//import com.increff.pos.db.OrderPojo;
//import com.increff.pos.db.ProductPojo;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.model.data.OrderItem;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProductApiImplTest {
//
//    @Mock
//    private ProductDao productDao;
//
//    @InjectMocks
//    private ProductApiImpl productApi;
//
//    private ProductPojo createProduct(String id, String barcode, double mrp) {
//        ProductPojo pojo = new ProductPojo();
//        pojo.setId(id);
//        pojo.setBarcode(barcode);
//        pojo.setName("Product-" + barcode);
//        pojo.setMrp(mrp);
//        pojo.setUpdatedAt(ZonedDateTime.now());
//        return pojo;
//    }
//
//    private OrderItem createOrderItem(String barcode, double sellingPrice) {
//        OrderItem item = new OrderItem();
//        item.setBarcode(barcode);
//        item.setSellingPrice(sellingPrice);
//        return item;
//    }
//
//    // ---------- addProduct ----------
//
//    @Test
//    void testAddProductSuccess() throws ApiException {
//        ProductPojo pojo = createProduct("1", "b1", 100);
//
//        when(productDao.findByBarcode("b1")).thenReturn(null);
//        when(productDao.save(pojo)).thenReturn(pojo);
//
//        ProductPojo result = productApi.addProduct(pojo);
//
//        assertEquals(pojo, result);
//    }
//
//    @Test
//    void testAddProductBarcodeExists() {
//        when(productDao.findByBarcode("b1"))
//                .thenReturn(createProduct("1", "b1", 100));
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.addProduct(createProduct("1", "b1", 100)));
//
//        assertEquals("Barcode already exists", ex.getMessage());
//    }
//
//    // ---------- editProduct ----------
//
//    @Test
//    void testEditProduct() throws ApiException {
//        ProductPojo existing = createProduct("1", "b1", 100);
//        ProductPojo updated = createProduct(null, "b1", 120);
//
//        when(productDao.findByBarcode("b1")).thenReturn(existing);
//        when(productDao.save(any(ProductPojo.class)))
//                .thenAnswer(i -> i.getArgument(0));
//
//        ProductPojo result = productApi.editProduct(updated);
//
//        assertEquals("1", result.getId());
//    }
//
//    // ---------- getAllProducts ----------
//
//    @Test
//    void testGetAllProducts() {
//        Page<ProductPojo> page =
//                new PageImpl<>(List.of(createProduct("1", "b1", 100)));
//
//        when(productDao.findAll(any(Pageable.class))).thenReturn(page);
//
//        Page<ProductPojo> result = productApi.getAllProducts(0, 10);
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    // ---------- addProductsBulk + persistValidProducts ----------
//
//    @Test
//    void testAddProductsBulkSuccess() throws ApiException {
//        List<ProductPojo> products = List.of(
//                createProduct("1", "b1", 100),
//                createProduct("2", "b2", 200)
//        );
//
//        when(productDao.saveAll(products)).thenReturn(products);
//
//        List<ProductPojo> result = productApi.addProductsBulk(products);
//
//        assertEquals(2, result.size());
//    }
//
//    @Test
//    void testPersistValidProductsException() {
//        List<ProductPojo> products = List.of(createProduct("1", "b1", 100));
//
//        when(productDao.saveAll(products)).thenThrow(RuntimeException.class);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.addProductsBulk(products));
//
//        assertEquals("Failed to insert valid products", ex.getMessage());
//    }
//
//    // ---------- checkBarcodeExists ----------
//
//    @Test
//    void testCheckBarcodeExistsThrows() {
//        when(productDao.findByBarcode("b1"))
//                .thenReturn(createProduct("1", "b1", 100));
//
//        assertThrows(ApiException.class,
//                () -> productApi.checkBarcodeExists("b1"));
//    }
//
//    @Test
//    void testCheckBarcodeExistsSuccess() throws ApiException {
//        when(productDao.findByBarcode("b1")).thenReturn(null);
//
//        assertDoesNotThrow(() -> productApi.checkBarcodeExists("b1"));
//    }
//
//    // ---------- validateAllOrderItems ----------
//
//    @Test
//    void testValidateAllOrderItemsSuccess() throws ApiException {
//        OrderItem item = createOrderItem("b1", 50);
//        OrderPojo order = new OrderPojo();
//        order.setOrderItems(List.of(item));
//
//        ProductPojo product = createProduct("1", "b1", 100);
//
//        when(productDao.findByBarcodes(List.of("b1")))
//                .thenReturn(List.of(product));
//
//        assertDoesNotThrow(() -> productApi.validateAllOrderItems(order));
//    }
//
//    @Test
//    void testValidateAllOrderItemsInvalidItem() {
//        OrderItem item = createOrderItem("b1", 150);
//        OrderPojo order = new OrderPojo();
//        order.setOrderItems(List.of(item));
//
//        ProductPojo product = createProduct("1", "b1", 100);
//
//        when(productDao.findByBarcodes(List.of("b1")))
//                .thenReturn(List.of(product));
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.validateAllOrderItems(order));
//
//        assertTrue(ex.getMessage().contains("Selling price exceeds MRP"));
//    }
//
//    // ---------- validateItem ----------
//
//    @Test
//    void testValidateItemInvalidBarcode() {
//        OrderItem item = createOrderItem("b1", 50);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.validateItem(item, Map.of()));
//
//        assertTrue(ex.getMessage().contains("Invalid barcode"));
//    }
//
//    @Test
//    void testValidateItemInvalidPrice() {
//        OrderItem item = createOrderItem("b1", 0);
//        ProductPojo product = createProduct("1", "b1", 100);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.validateItem(item, Map.of("b1", product)));
//
//        assertTrue(ex.getMessage().contains("Selling price exceeds MRP"));
//    }
//
//    // ---------- findExistingProducts ----------
//
//    @Test
//    void testFindExistingProducts() {
//        ProductPojo product = createProduct("1", "b1", 100);
//
//        when(productDao.findByBarcodes(List.of("b1")))
//                .thenReturn(List.of(product));
//
//        Map<String, ProductPojo> result =
//                productApi.findExistingProducts(List.of("b1"));
//
//        assertEquals(product, result.get("b1"));
//    }
//
//    // ---------- mapBarcodesToProductPojos ----------
//
//    @Test
//    void testMapBarcodesToProductPojosNullOrEmpty() {
//        assertTrue(productApi.mapBarcodesToProductPojos(null).isEmpty());
//        assertTrue(productApi.mapBarcodesToProductPojos(List.of()).isEmpty());
//    }
//
//    // ---------- getCheckByBarcode ----------
//
//    @Test
//    void testGetCheckByBarcodeSuccess() throws ApiException {
//        ProductPojo product = createProduct("1", "b1", 100);
//
//        when(productDao.findByBarcode("b1")).thenReturn(product);
//
//        ProductPojo result = productApi.getCheckByBarcode("b1");
//
//        assertEquals(product, result);
//    }
//
//    @Test
//    void testGetCheckByBarcodeThrows() {
//        when(productDao.findByBarcode("b1")).thenReturn(null);
//
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.getCheckByBarcode("b1"));
//
//        assertTrue(ex.getMessage().contains("doesn't exist"));
//    }
//
//    // ---------- searchProducts ----------
//
//    @Test
//    void testSearchProductsByBarcode() throws ApiException {
//        Page<ProductPojo> page =
//                new PageImpl<>(List.of(createProduct("1", "b1", 100)));
//
//        when(productDao.searchByBarcode(eq("b1"), any(Pageable.class)))
//                .thenReturn(page);
//
//        Page<ProductPojo> result =
//                productApi.searchProducts("barcode", "b1", 0, 10);
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    @Test
//    void testSearchProductsByName() throws ApiException {
//        Page<ProductPojo> page =
//                new PageImpl<>(List.of(createProduct("1", "b1", 100)));
//
//        when(productDao.searchByName(eq("prod"), any(Pageable.class)))
//                .thenReturn(page);
//
//        Page<ProductPojo> result =
//                productApi.searchProducts("name", "prod", 0, 10);
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    @Test
//    void testSearchProductsInvalidType() {
//        ApiException ex = assertThrows(ApiException.class,
//                () -> productApi.searchProducts("invalid", "x", 0, 10));
//
//        assertTrue(ex.getMessage().contains("Invalid search type"));
//    }
//}
