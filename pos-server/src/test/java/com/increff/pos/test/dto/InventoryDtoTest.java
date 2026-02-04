package com.increff.pos.test.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.RowError;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDtoTest {

    @InjectMocks
    private InventoryDto inventoryDto;

    @Mock
    private InventoryApiImpl inventoryApi;

    @Mock
    private ProductApiImpl productApi;

    @Mock
    private FormValidator formValidator;

    // ---------- updateInventory ----------

    @Test
    void testUpdateInventory_success() throws ApiException {
        InventoryForm form = new InventoryForm();
        InventoryPojo pojo = new InventoryPojo();
        InventoryData data = new InventoryData();

        try (MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class)) {

            helper.when(() -> InventoryHelper.convertToEntity(form,"1cxsv")).thenReturn(pojo);
            helper.when(() -> InventoryHelper.convertToData(pojo)).thenReturn(data);

            InventoryData result = inventoryDto.updateInventory(form);

            assertNotNull(result);
            verify(formValidator).validate(form);
            verify(inventoryApi).updateSingleInventory(pojo);
        }
    }

    // ---------- updateInventoryBulk (SUCCESS) ----------

    @Test
    void testUpdateInventoryBulk_success() throws ApiException {
        FileForm fileForm = new FileForm();
        fileForm.setBase64file("base64");

        List<String[]> rows = List.of(
                new String[]{"barcode", "quantity"},
                new String[]{"b1", "10"}
        );

        Map<String, Integer> headerMap = Map.of("barcode", 0, "quantity", 1);
        ProductPojo productPojo = new ProductPojo();
        productPojo.setId("p1");

        try (
                MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class);
                MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)
        ) {
            tsv.when(() -> TsvParser.parseBase64Tsv("base64")).thenReturn(rows);

            helper.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(rows.get(0)))
                    .thenReturn(headerMap);
            helper.when(() -> InventoryHelper.getAllBarcodes(rows, headerMap))
                    .thenReturn(List.of("b1"));
            helper.when(() ->
                    InventoryHelper.segragateValidAndInvalidEntries(
                            eq(rows), anyList(), anyList(), eq(headerMap), anyMap()
                    )).thenAnswer(invocation -> null);

            validation.when(() -> ValidationUtil.validateHeaders(headerMap)).thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.validateRowLimit(rows)).thenAnswer(i -> null);

            when(productApi.mapBarcodesToProductPojos(anyList()))
                    .thenReturn(Map.of("b1", productPojo));

            fileUtils.when(() -> FileUtils.generateInventoryUpdateResults(anyList()))
                    .thenReturn("result");

            FileData result = inventoryDto.updateInventoryBulk(fileForm);

            assertEquals("SUCCESS", result.getStatus());
            verify(inventoryApi).updateBulkInventory(anyList());
        }
    }

    // ---------- updateInventoryBulk (UNSUCCESSFUL) ----------

    @Test
    void testUpdateInventoryBulk_withInvalidRows() throws ApiException {
        FileForm fileForm = new FileForm();
        fileForm.setBase64file("base64");

        List<String[]> rows = List.of(
                new String[]{"barcode", "quantity"},
                new String[]{"b1", "-1"}
        );

        Map<String, Integer> headerMap = Map.of("barcode", 0, "quantity", 1);
        ProductPojo productPojo = new ProductPojo();
        productPojo.setId("p1");

        try (
                MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
                MockedStatic<ValidationUtil> validation = mockStatic(ValidationUtil.class);
                MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class);
                MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)
        ) {
            tsv.when(() -> TsvParser.parseBase64Tsv("base64")).thenReturn(rows);

            helper.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(rows.get(0)))
                    .thenReturn(headerMap);
            helper.when(() -> InventoryHelper.getAllBarcodes(rows, headerMap))
                    .thenReturn(List.of("b1"));

            helper.when(() ->
                    InventoryHelper.segragateValidAndInvalidEntries(
                            eq(rows),
                            anyList(),
                            anyList(),
                            eq(headerMap),
                            anyMap()
                    )
            ).thenAnswer(invocation -> {
                List<RowError> invalid = invocation.getArgument(2);
                invalid.add(new RowError("",""));
                return null;
            });

            validation.when(() -> ValidationUtil.validateHeaders(headerMap)).thenAnswer(i -> null);
            validation.when(() -> ValidationUtil.validateRowLimit(rows)).thenAnswer(i -> null);

            when(productApi.mapBarcodesToProductPojos(anyList()))
                    .thenReturn(Map.of("b1", productPojo));

            fileUtils.when(() -> FileUtils.generateInventoryUpdateResults(anyList()))
                    .thenReturn("error-file");

            FileData result = inventoryDto.updateInventoryBulk(fileForm);

            assertEquals("UNSUCCESSFUL", result.getStatus());
            verify(inventoryApi).updateBulkInventory(anyList());
        }
    }
}
