package com.increff.pos.test.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.dto.ProductDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDtoTest {

    @InjectMocks
    private ProductDto productDto;

    @Mock
    private ProductFlow productFlow;

    @Mock
    private ClientApiImpl clientApi;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private FormValidator formValidator;

    // ---------- helpers ----------

    private ProductForm productForm() {
        ProductForm f = new ProductForm();
        f.setBarcode("b1");
        f.setName("p1");
        f.setClientName("client1");
        f.setMrp(100.0);
        return f;
    }

    private ProductPojo productPojo() {
        ProductPojo p = new ProductPojo();
        p.setId("p1");
        p.setBarcode("b1");
        p.setClientName("client1");
        return p;
    }

    // ---------- createProduct ----------



    // ---------- editProduct ----------

    // ---------- getAllProducts ----------

    @Test
    void testGetAllProducts() throws ApiException {
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        ProductPojo pojo = productPojo();
        InventoryPojo inv = new InventoryPojo();
        inv.setQuantity(5);

        Page<ProductPojo> page = new PageImpl<>(List.of(pojo));

        doNothing().when(formValidator).validate(form);
        when(productFlow.getAllProducts(0, 10)).thenReturn(page);
        when(productFlow.getInventoryForProducts(page))
                .thenReturn(Map.of("p1", inv));

        Page<ProductData> result = productDto.getAllProducts(form);

        assertEquals(1, result.getTotalElements());
    }

    // ---------- extractHeaderIndexMap ----------

    @Test
    void testExtractHeaderIndexMap() {
        String[] header = {"barcode", "name", "client"};

        Map<String, Integer> map =
                ProductDto.extractHeaderIndexMap(header);

        assertEquals(1, map.get("name"));
    }

    // ---------- createProducts (bulk upload) ----------

    @Test
    void testCreateProduct_fixed() throws ApiException {
        ProductForm form = productForm();
        ProductPojo pojo = productPojo();

        // normal mocks
        doNothing().when(formValidator).validate(any(ProductForm.class));
        doNothing().when(clientApi).getCheckByClientName(anyString());
        when(productFlow.addProduct(any())).thenReturn(pojo);

        // static mocks for ProductHelper and NormalizationUtil
        try (MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class);
             MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class)) {

            helper.when(() -> ProductHelper.convertToEntity(any(ProductForm.class)))
                    .thenReturn(pojo);

            norm.when(() -> NormalizationUtil.normalizeProductForm(any(ProductForm.class)))
                    .thenAnswer(invocation -> null);

            ProductData data = productDto.createProduct(form);

            assertEquals("b1", data.getBarcode());
            verify(clientApi).getCheckByClientName("client1");
            verify(productFlow).addProduct(pojo);
        }
    }

    @Test
    void testEditProduct_fixed() throws ApiException {
        ProductForm form = productForm();
        ProductPojo pojo = productPojo();

        doNothing().when(formValidator).validate(any(ProductForm.class));
        doNothing().when(clientApi).getCheckByClientName(anyString());
        when(productFlow.editProduct(any())).thenReturn(pojo);

        try (MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class);
             MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class)) {

            helper.when(() -> ProductHelper.convertToEntity(any(ProductForm.class)))
                    .thenReturn(pojo);

            norm.when(() -> NormalizationUtil.normalizeProductForm(any(ProductForm.class)))
                    .thenAnswer(invocation -> null);

            ProductData data = productDto.editProduct(form);

            assertEquals("b1", data.getBarcode());
            verify(clientApi).getCheckByClientName("client1");
            verify(productFlow).editProduct(pojo);
        }
    }

    @Test
    void testCreateProducts_bulk_fixed() throws Exception {
        FileForm fileForm = new FileForm();
        fileForm.setBase64file("dummy");

        ProductPojo pojo1 = productPojo();
        pojo1.setBarcode("b1");

        ProductPojo pojo2 = productPojo();
        pojo2.setBarcode("b2");

        String[] header = {"barcode", "name", "client", "mrp"};
        String[] row1 = {"b1", "p1", "client1", "100"};
        String[] row2 = {"b2", "p2", "client1", "200"};
        String[] emptyRow = {"", "", "", ""};

        List<String[]> rows = List.of(header, row1, row2, emptyRow);

        doNothing().when(formValidator).validate(fileForm);

        // static mocks
        try (MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
             MockedStatic<ProductHelper> helper = mockStatic(ProductHelper.class);
             MockedStatic<NormalizationUtil> norm = mockStatic(NormalizationUtil.class);
             MockedStatic<ValidationUtil> val = mockStatic(ValidationUtil.class)) {

            tsv.when(() -> TsvParser.parseBase64Tsv("dummy"))
                    .thenReturn(rows);

            val.when(() -> ValidationUtil.validateProductHeaders(any())).thenReturn(null);
            val.when(() -> ValidationUtil.isRowEmpty(emptyRow)).thenReturn(true);
            val.when(() -> ValidationUtil.isRowEmpty(any())).thenReturn(false);

            // simulate converting rows to ProductPojo
            helper.when(() -> ProductHelper.convertRowToProductPojo(row1, any()))
                    .thenReturn(pojo1);
            helper.when(() -> ProductHelper.convertRowToProductPojo(row2, any()))
                    .thenReturn(pojo2);

            when(clientApi.fetchExistingClientNames(any()))
                    .thenReturn(Map.of("client1", new ClientPojo()));
            when(productApi.findExistingProducts(any()))
                    .thenReturn(Collections.emptyMap());

            doNothing().when(productFlow).addProductsBulk(any());

            FileData result = productDto.createProducts(fileForm);

            assertEquals("SUCCESS", result.getStatus());
            verify(productFlow).addProductsBulk(any());
        }
    }


    // ---------- getFinalValidProducts ----------

    @Test
    void testGetFinalValidProducts_allRejectionBranches() {
        ProductPojo p1 = productPojo();
        ProductPojo p2 = productPojo();
        p2.setBarcode("b1");

        List<ProductPojo> valid = List.of(p1, p2);
        List<RowError> errors = new ArrayList<>();

        Map<String, ClientPojo> validClients = Map.of();
        Map<String, ProductPojo> existingBarcodes = Map.of("b1", p1);

        List<ProductPojo> result =
                productDto.getFinalValidProducts(
                        valid,
                        errors,
                        validClients,
                        existingBarcodes
                );

        assertTrue(result.isEmpty());
        assertFalse(errors.isEmpty());
    }

    // ---------- getValidClients ----------

    @Test
    void testGetValidClients() {
        ProductPojo pojo = productPojo();

        when(clientApi.fetchExistingClientNames(List.of("client1")))
                .thenReturn(Map.of("client1", new ClientPojo()));

        Map<String, ClientPojo> result =
                productDto.getValidClients(List.of(pojo));

        assertEquals(1, result.size());
    }

    // ---------- getValidBarcodes ----------

    @Test
    void testGetValidBarcodes() {
        ProductPojo pojo = productPojo();

        when(productApi.findExistingProducts(List.of("b1")))
                .thenReturn(Map.of());

        Map<String, ProductPojo> result =
                productDto.getValidBarcodes(List.of(pojo));

        assertNotNull(result);
    }

    // ---------- convertProductResultsToBase64 ----------

    @Test
    void testConvertProductResultsToBase64_success() {
        FileData data =
                productDto.convertProductResultsToBase64(List.of());

        assertEquals("SUCCESS", data.getStatus());
    }

    @Test
    void testConvertProductResultsToBase64_failure() {
        FileData data =
                productDto.convertProductResultsToBase64(
                        List.of(new RowError("b1", "err"))
                );

        assertEquals("UNSUCCESSFUL", data.getStatus());
    }

    // ---------- search ----------

    @Test
    void testSearch() throws ApiException {
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        ProductPojo pojo = productPojo();
        Page<ProductPojo> page = new PageImpl<>(List.of(pojo));

        doNothing().when(formValidator).validate(form);
        when(productFlow.searchProducts("name", "p", 0, 10))
                .thenReturn(page);

        when(productFlow.getInventoryForProducts(page))
                .thenReturn(Map.of());

        Page<ProductData> result =
                productDto.search("name", "p", form);

        assertEquals(1, result.getTotalElements());
    }
}
