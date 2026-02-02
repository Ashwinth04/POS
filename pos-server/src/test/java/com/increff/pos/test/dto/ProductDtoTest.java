//package com.increff.pos.test.dto;
//
//import com.increff.pos.api.ClientApiImpl;
//import com.increff.pos.api.ProductApiImpl;
//import com.increff.pos.db.ClientPojo;
//import com.increff.pos.db.InventoryPojo;
//import com.increff.pos.db.ProductPojo;
//import com.increff.pos.dto.ProductDto;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.flow.ProductFlow;
//import com.increff.pos.helper.ProductHelper;
//import com.increff.pos.model.data.*;
//import com.increff.pos.model.form.FileForm;
//import com.increff.pos.model.form.PageForm;
//import com.increff.pos.model.form.ProductForm;
//import com.increff.pos.util.TsvParser;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//
//import java.util.*;
//
//import static com.increff.pos.util.FileUtils.generateProductUploadResults;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProductDtoTest {
//
//    @Mock
//    private ProductFlow productFlow;
//
//    @Mock
//    private ClientApiImpl clientApi;
//
//    @Mock
//    private ProductApiImpl productApi;
//
//    @InjectMocks
//    private ProductDto productDto;
//
//    // ---------- CREATE PRODUCT ----------
//
//    @Test
//    void testCreateProductSuccess() throws ApiException {
//
//        ProductForm form = new ProductForm();
//        form.setClientName("client1");
//
//        ProductPojo pojo = new ProductPojo();
//        ProductPojo savedPojo = new ProductPojo();
//        ProductData data = new ProductData();
//
//        try (MockedStatic<ProductHelper> helperMock = mockStatic(ProductHelper.class)) {
//
//            // ✅ FIX
//            when(clientApi.getCheckByClientName("client1"))
//                    .thenReturn(new ClientPojo());
//
//            helperMock.when(() -> ProductHelper.convertToEntity(form))
//                    .thenReturn(pojo);
//
//            when(productFlow.addProduct(pojo))
//                    .thenReturn(savedPojo);
//
//            helperMock.when(() -> ProductHelper.convertToData(savedPojo))
//                    .thenReturn(data);
//
//            ProductData result = productDto.createProduct(form);
//
//            assertNotNull(result);
//            verify(clientApi).getCheckByClientName("client1");
//            verify(productFlow).addProduct(pojo);
//        }
//    }
//
//
//    // ---------- EDIT PRODUCT ----------
//
//    @Test
//    void testEditProductSuccess() throws ApiException {
//
//        ProductForm form = new ProductForm();
//        form.setClientName("client1");
//
//        ProductPojo pojo = new ProductPojo();
//        ProductPojo editedPojo = new ProductPojo();
//        ProductData data = new ProductData();
//
//        try (MockedStatic<ProductHelper> helperMock = mockStatic(ProductHelper.class)) {
//
//            // ✅ FIX
//            when(clientApi.getCheckByClientName("client1"))
//                    .thenReturn(new ClientPojo());
//
//            helperMock.when(() -> ProductHelper.convertToEntity(form))
//                    .thenReturn(pojo);
//
//            when(productFlow.editProduct(pojo))
//                    .thenReturn(editedPojo);
//
//            helperMock.when(() -> ProductHelper.convertToData(editedPojo))
//                    .thenReturn(data);
//
//            ProductData result = productDto.editProduct(form);
//
//            assertNotNull(result);
//            verify(clientApi).getCheckByClientName("client1");
//            verify(productFlow).editProduct(pojo);
//        }
//    }
//
//
//    // ---------- GET ALL PRODUCTS ----------
//
//    @Test
//    void testGetAllProductsSuccess() throws ApiException {
//
//        ProductPojo productPojo = new ProductPojo();
//        Page<ProductPojo> page = new PageImpl<>(List.of(productPojo));
//
//        Map<String, InventoryPojo> inventoryMap = new HashMap<>();
//
//        when(productFlow.getAllProducts(0, 10)).thenReturn(page);
//        when(productFlow.getInventoryForProducts(page)).thenReturn(inventoryMap);
//
//        try (MockedStatic<ProductHelper> helperMock = mockStatic(ProductHelper.class)) {
//
//            helperMock.when(() ->
//                    ProductHelper.convertToData(productPojo, inventoryMap)
//            ).thenReturn(new ProductData());
//
//            PageForm form = new PageForm();
//            form.setPage(0);
//            form.setSize(10);
//
//            Page<ProductData> result = productDto.getAllProducts(form);
//
//            assertEquals(1, result.getTotalElements());
//        }
//    }
//
//    // ---------- BULK CREATE PRODUCTS ----------
//
//    @Test
//    void testCreateProductsBulkSuccess() throws ApiException {
//
//        FileForm fileForm = new FileForm();
//        fileForm.setBase64file("dummy");
//
//        String[] header = {"barcode", "clientName"};
//        String[] row = {"b1", "client1"};
//        List<String[]> rows = List.of(header, row);
//
//        ProductPojo pojo = new ProductPojo();
//        pojo.setBarcode("b1");
//        pojo.setClientName("client1");
//
//        try (
//                MockedStatic<TsvParser> tsvMock = mockStatic(TsvParser.class);
//                MockedStatic<ProductHelper> helperMock = mockStatic(ProductHelper.class);
//                MockedStatic<com.increff.pos.util.FileUtils> fileUtilsMock =
//                        mockStatic(com.increff.pos.util.FileUtils.class)
//        ) {
//
//            tsvMock.when(() -> TsvParser.parseBase64Tsv("dummy"))
//                    .thenReturn(rows);
//
//            helperMock.when(() -> ProductHelper.validateHeaders(anyMap()))
//                    .thenAnswer(invocation -> null);
//
//            helperMock.when(() ->
//                    ProductHelper.toProductPojo(row, ProductDto.extractHeaderIndexMap(header))
//            ).thenReturn(pojo);
//
//            when(clientApi.fetchExistingClientNames(anyList()))
//                    .thenReturn(List.of("client1"));
//
//            when(productApi.findExistingProducts(anyList()))
//                    .thenReturn(Collections.emptyList());
//
//            doNothing().when(productFlow).addProductsBulk(anyList());
//
//            fileUtilsMock.when(() ->
//                    generateProductUploadResults(anyList())
//            ).thenReturn("base64");
//
//            FileData response = productDto.createProducts(fileForm);
//
//            assertEquals("SUCCESS", response.getStatus());
//            assertEquals("base64", response.getBase64file());
//        }
//    }
//
//    // ---------- BULK CREATE MAX ROWS ----------
//
//    @Test
//    void testCreateProductsMaxRowsExceeded() {
//
//        FileForm fileForm = new FileForm();
//        fileForm.setBase64file("dummy");
//
//        List<String[]> rows = new ArrayList<>();
//        rows.add(new String[]{"barcode", "clientName"});
//
//        for (int i = 0; i < 5001; i++) {
//            rows.add(new String[]{"b" + i, "c"});
//        }
//
//        try (MockedStatic<TsvParser> tsvMock = mockStatic(TsvParser.class)) {
//
//            tsvMock.when(() -> TsvParser.parseBase64Tsv("dummy"))
//                    .thenReturn(rows);
//
//            assertThrows(ApiException.class, () ->
//                    productDto.createProducts(fileForm)
//            );
//        }
//    }
//
//    // ---------- SEARCH PRODUCTS ----------
//
//    @Test
//    void testSearchProductsSuccess() throws ApiException {
//
//        ProductPojo productPojo = new ProductPojo();
//        Page<ProductPojo> page = new PageImpl<>(List.of(productPojo));
//
//        Map<String, InventoryPojo> inventoryMap = new HashMap<>();
//
//        when(productFlow.searchProducts("name", "abc", 0, 5))
//                .thenReturn(page);
//
//        when(productFlow.getInventoryForProducts(page))
//                .thenReturn(inventoryMap);
//
//        try (MockedStatic<ProductHelper> helperMock = mockStatic(ProductHelper.class)) {
//
//            helperMock.when(() ->
//                    ProductHelper.convertToData(productPojo, inventoryMap)
//            ).thenReturn(new ProductData());
//
//            PageForm form = new PageForm();
//            form.setPage(0);
//            form.setSize(5);
//
//            Page<ProductData> result =
//                    productDto.search("name", "abc", form);
//
//            assertEquals(1, result.getTotalElements());
//        }
//    }
//}
