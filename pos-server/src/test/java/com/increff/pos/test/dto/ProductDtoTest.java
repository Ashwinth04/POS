package com.increff.pos.test.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.dto.ProductDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.constants.ProductSearchType;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static com.increff.pos.util.FileUtils.generateProductUploadResults;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDtoTest {

    @InjectMocks
    private ProductDto productDto;

    @Mock
    private ProductFlow productFlow;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private ClientApiImpl clientApi;

    @Mock
    private FormValidator formValidator;

    private ProductForm productForm;
    private ProductPojo productPojo;

    @BeforeEach
    void setup() {
        productForm = new ProductForm();
        productForm.setBarcode("b1");
        productForm.setClientName("client1");

        productPojo = new ProductPojo();
        productPojo.setBarcode("b1");
        productPojo.setClientName("client1");
    }

    // ---------- createProduct ----------

    @Test
    void testCreateProduct_success() throws ApiException {
        ProductData data = new ProductData();

        try (
                MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class);
                MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class)
        ) {
            helper.when(() -> ProductHelper.convertToEntity(productForm)).thenReturn(productPojo);
            helper.when(() -> ProductHelper.convertToData(productPojo)).thenReturn(data);

            when(clientApi.getCheckByClientName("client1")).thenReturn(new ClientPojo());
            when(productFlow.addProduct(productPojo)).thenReturn(productPojo);

            ProductData result = productDto.createProduct(productForm);

            assertNotNull(result);
            verify(formValidator).validate(productForm);
        }
    }

    // ---------- editProduct ----------

    @Test
    void testEditProduct_success() throws ApiException {
        ProductData data = new ProductData();

        try (
                MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class);
                MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class)
        ) {
            helper.when(() -> ProductHelper.convertToEntity(productForm)).thenReturn(productPojo);
            helper.when(() -> ProductHelper.convertToData(productPojo)).thenReturn(data);

            when(clientApi.getCheckByClientName("client1")).thenReturn(new ClientPojo());
            when(productApi.editProduct(productPojo)).thenReturn(productPojo);

            ProductData result = productDto.editProduct(productForm);

            assertNotNull(result);
        }
    }

    // ---------- getAllProducts ----------

    @Test
    void testGetAllProducts() throws ApiException {
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        Page<ProductPojo> page = new PageImpl<>(List.of(productPojo));
        InventoryPojo inventoryPojo = new InventoryPojo();

        try (MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class)) {
            when(productApi.getAllProducts(0, 10)).thenReturn(page);
            when(productFlow.getInventoryForProducts(page))
                    .thenReturn(Map.of("id", inventoryPojo));

            helper.when(() ->
                            ProductHelper.convertToData(any(ProductPojo.class), anyMap()))
                    .thenReturn(new ProductData());

            Page<ProductData> result = productDto.getAllProducts(form);

            assertEquals(1, result.getContent().size());
        }
    }

    // ---------- createProducts (SUCCESS) ----------

    @Test
    void testCreateProducts_success() throws ApiException {
        FileForm fileForm = new FileForm();
        fileForm.setBase64file("base64");

        List<String[]> rows = List.of(
                new String[]{"barcode", "client"},
                new String[]{"b1", "client1"}
        );

        try (
                MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
                MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class);
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<com.increff.pos.util.FileUtils> fileUtils =
                        mockStatic(com.increff.pos.util.FileUtils.class)
        ) {
            tsv.when(() -> TsvParser.parseBase64Tsv("base64")).thenReturn(rows);

            validation.when(() -> ValidationUtil.validateProductHeaders(anyMap()))
                    .thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.validateRowLimit(rows))
                    .thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.isRowEmpty(any()))
                    .thenReturn(false);
            validation.when(() ->
                            ValidationUtil.getFinalValidProducts(anyList(), anyList(), anyMap(), anyMap()))
                    .thenReturn(List.of(productPojo));

            helper.when(() ->
                            ProductHelper.convertRowToProductPojo(any(), anyMap()))
                    .thenReturn(productPojo);

            when(clientApi.fetchExistingClientNames(anyList()))
                    .thenReturn(List.of(new ClientPojo()));
            when(productApi.findExistingProducts(anyList()))
                    .thenReturn(List.of());
            doNothing().when(productFlow).addProductsBulk(anyList());

            fileUtils.when(() -> generateProductUploadResults(anyList()))
                    .thenReturn("result");

            FileData result = productDto.createProducts(fileForm);

            assertEquals("SUCCESS", result.getStatus());
        }
    }

    // ---------- createProducts (UNSUCCESSFUL) ----------

    @Test
    void testCreateProducts_withInvalidRow() throws ApiException {
        FileForm fileForm = new FileForm();
        fileForm.setBase64file("base64");

        List<String[]> rows = List.of(
                new String[]{"barcode", "client"},
                new String[]{"b1", "client1"}
        );

        try (
                MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
                MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class);
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<com.increff.pos.util.FileUtils> fileUtils =
                        mockStatic(com.increff.pos.util.FileUtils.class)
        ) {
            tsv.when(() -> TsvParser.parseBase64Tsv("base64")).thenReturn(rows);

            validation.when(() -> ValidationUtil.validateProductHeaders(anyMap()))
                    .thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.validateRowLimit(rows))
                    .thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.isRowEmpty(any()))
                    .thenReturn(false);
            validation.when(() ->
                            ValidationUtil.getFinalValidProducts(anyList(), anyList(), anyMap(), anyMap()))
                    .thenReturn(List.of());

            helper.when(() ->
                            ProductHelper.convertRowToProductPojo(any(), anyMap()))
                    .thenThrow(new RuntimeException("error"));

            fileUtils.when(() -> generateProductUploadResults(anyList()))
                    .thenReturn("error-file");

            FileData result = productDto.createProducts(fileForm);

            assertEquals("UNSUCCESSFUL", result.getStatus());
        }
    }

    // ---------- getValidClients ----------

    @Test
    void testGetValidClients() {
        when(clientApi.fetchExistingClientNames(anyList()))
                .thenReturn(List.of(new ClientPojo()));

        Map<String, ClientPojo> result =
                productDto.getValidClients(List.of(productPojo));

        assertEquals(1, result.size());
    }

    // ---------- getValidBarcodes ----------

    @Test
    void testGetValidBarcodes() {
        when(productApi.findExistingProducts(anyList()))
                .thenReturn(List.of(productPojo));

        Map<String, ProductPojo> result =
                productDto.getValidBarcodes(List.of(productPojo));

        assertEquals(1, result.size());
    }

    // ---------- searchProducts ----------

    @Test
    void testSearchProducts() throws ApiException {

        ProductSearchForm form = new ProductSearchForm();
        form.setPage(0);
        form.setSize(10);
        form.setType(ProductSearchType.NAME);
        form.setQuery("q");

        Page<ProductPojo> page = new PageImpl<>(List.of(productPojo));

        try (MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class)) {
            when(productFlow.searchProducts(ProductSearchType.NAME, "q", 0, 10))
                    .thenReturn(page);
            when(productFlow.getInventoryForProducts(page))
                    .thenReturn(Map.of());

            helper.when(() ->
                            ProductHelper.convertToData(any(ProductPojo.class), anyMap()))
                    .thenReturn(new ProductData());

            Page<ProductData> result =
                    productDto.searchProducts(form);

            assertEquals(1, result.getContent().size());
        }
    }
}
