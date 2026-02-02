package com.increff.pos.test.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.TsvParser;
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

    @Mock
    private InventoryApiImpl inventoryApi;

    @Mock
    private ProductApiImpl productApi;

    @InjectMocks
    private InventoryDto inventoryDto;

    @Test
    void testUpdateInventorySuccess() throws ApiException {
        InventoryForm form = new InventoryForm();
        form.setProductId("p1");
        form.setQuantity(10);

        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(10);

        try (MockedStatic<InventoryHelper> helperMock = mockStatic(InventoryHelper.class)) {

            helperMock.when(() -> InventoryHelper.convertToEntity(form))
                    .thenReturn(pojo);

            helperMock.when(() -> InventoryHelper.convertToData(pojo))
                    .thenReturn(new InventoryData());

            // ✅ FIX: return value instead of doNothing
            when(inventoryApi.updateSingleInventory(pojo)).thenReturn(pojo);

            InventoryData data = inventoryDto.updateInventory(form);

            assertNotNull(data);
            verify(inventoryApi).updateSingleInventory(pojo);
        }
    }


    @Test
    void testUpdateInventoryBulkSuccess() throws ApiException {

        FileForm fileForm = new FileForm();
        fileForm.setBase64file("dummy");

        String[] header = {"barcode", "quantity"};
        String[] row1 = {"b1", "5"};

        List<String[]> rows = List.of(header, row1);

        Map<String, Integer> headerMap = Map.of(
                "barcode", 0,
                "quantity", 1
        );

        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId("p1");
        pojo.setQuantity(5);

        Map<String, String> barcodeToProductMap = Map.of("b1", "p1");

        try (
                MockedStatic<TsvParser> tsvMock = mockStatic(TsvParser.class);
                MockedStatic<InventoryHelper> helperMock = mockStatic(InventoryHelper.class);
                MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)
        ) {

            tsvMock.when(() -> TsvParser.parseBase64Tsv("dummy"))
                    .thenReturn(rows);

            helperMock.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(header))
                    .thenReturn(headerMap);

            helperMock.when(() -> InventoryHelper.validateHeaders(headerMap))
                    .thenAnswer(invocation -> null);

            when(productApi.mapBarcodesToProductIds(anyList()))
                    .thenReturn(barcodeToProductMap);

            helperMock.when(() ->
                    InventoryHelper.convertRowToInventoryPojo(
                            eq(row1),
                            eq(headerMap),
                            eq(barcodeToProductMap)
                    )
            ).thenReturn(pojo);

            doNothing().when(inventoryApi).bulkInventoryUpdate(anyList());

            fileUtilsMock.when(() ->
                    FileUtils.generateInventoryUpdateResults(anyList())
            ).thenReturn("base64");

            FileData response = inventoryDto.updateInventoryBulk(fileForm);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("base64", response.getBase64file());

            verify(inventoryApi).bulkInventoryUpdate(anyList());
        }
    }

    @Test
    void testUpdateInventoryBulkRowError() throws ApiException {

        FileForm fileForm = new FileForm();
        fileForm.setBase64file("dummy");

        String[] header = {"barcode", "quantity"};
        String[] badRow = {"b1", "-10"};

        List<String[]> rows = List.of(header, badRow);

        Map<String, Integer> headerMap = Map.of(
                "barcode", 0,
                "quantity", 1
        );

        try (
                MockedStatic<TsvParser> tsvMock = mockStatic(TsvParser.class);
                MockedStatic<InventoryHelper> helperMock = mockStatic(InventoryHelper.class);
                MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)
        ) {

            tsvMock.when(() -> TsvParser.parseBase64Tsv("dummy"))
                    .thenReturn(rows);

            helperMock.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(header))
                    .thenReturn(headerMap);

            helperMock.when(() -> InventoryHelper.validateHeaders(headerMap))
                    .thenAnswer(invocation -> null);

            when(productApi.mapBarcodesToProductIds(anyList()))
                    .thenReturn(Map.of());

            helperMock.when(() ->
                    InventoryHelper.convertRowToInventoryPojo(any(), any(), any())
            ).thenThrow(new RuntimeException("Invalid quantity"));

            doNothing().when(inventoryApi).bulkInventoryUpdate(anyList());

            fileUtilsMock.when(() ->
                    FileUtils.generateInventoryUpdateResults(anyList())
            ).thenReturn("errorBase64");

            FileData response = inventoryDto.updateInventoryBulk(fileForm);

            assertEquals("UNSUCCESSFUL", response.getStatus());
            assertEquals("errorBase64", response.getBase64file());
        }
    }

    @Test
    void testUpdateInventoryBulkMaxRowsExceeded() {

        FileForm fileForm = new FileForm();
        fileForm.setBase64file("dummy");

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"barcode", "quantity"});

        for (int i = 0; i < 5001; i++) {
            rows.add(new String[]{"b" + i, "1"});
        }

        try (MockedStatic<TsvParser> tsvMock = mockStatic(TsvParser.class)) {

            tsvMock.when(() -> TsvParser.parseBase64Tsv("dummy"))
                    .thenReturn(rows);

            assertThrows(ApiException.class, () ->
                    inventoryDto.updateInventoryBulk(fileForm)
            );
        }
    }
}
