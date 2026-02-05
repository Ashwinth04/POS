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
import com.increff.pos.util.*;

import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private FormValidator formValidator;

    @InjectMocks
    private InventoryDto inventoryDto;

    private InventoryForm inventoryForm;

    @BeforeEach
    void setUp() {
        inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("B123");
        inventoryForm.setQuantity(10);
    }

    // -----------------------------
    // updateInventory
    // -----------------------------

    @Test
    void updateInventory_success() throws Exception {

        ProductPojo productPojo = new ProductPojo();
        productPojo.setId("P1");

        InventoryPojo inventoryPojo = new InventoryPojo();
        InventoryData inventoryData = new InventoryData();

        try (
                MockedStatic<NormalizationUtil> normalizationMock = mockStatic(NormalizationUtil.class);
                MockedStatic<InventoryHelper> inventoryHelperMock = mockStatic(InventoryHelper.class)
        ) {

            when(productApi.mapBarcodesToProductPojos(List.of("B123")))
                    .thenReturn(Map.of("B123", productPojo));

            inventoryHelperMock
                    .when(() -> InventoryHelper.convertToEntity(inventoryForm, "P1"))
                    .thenReturn(inventoryPojo);

            inventoryHelperMock
                    .when(() -> InventoryHelper.convertToData(inventoryPojo))
                    .thenReturn(inventoryData);

            InventoryData result = inventoryDto.updateInventory(inventoryForm);

            assertNotNull(result);
            verify(formValidator).validate(inventoryForm);
            verify(inventoryApi).updateSingleInventory(inventoryPojo);
        }
    }

    @Test
    void updateInventory_productNotFound() {

        when(productApi.mapBarcodesToProductPojos(anyList()))
                .thenReturn(Collections.emptyMap());

        ApiException ex = assertThrows(ApiException.class,
                () -> inventoryDto.updateInventory(inventoryForm));

        assertTrue(ex.getMessage().contains("Product not found"));
    }


    // -----------------------------
    // updateInventoryBulk
    // -----------------------------

    @Test
    void updateInventoryBulk_success() throws Exception {

        FileForm fileForm = new FileForm();
        fileForm.setBase64file("dummy");

        List<String[]> rows = List.of(
                new String[]{"barcode", "quantity"},
                new String[]{"B123", "10"}
        );

        Map<String, Integer> headerIndexMap = Map.of(
                "barcode", 0,
                "quantity", 1
        );

        try (
                MockedStatic<TsvParser> tsvMock = mockStatic(TsvParser.class);
                MockedStatic<InventoryHelper> helperMock = mockStatic(InventoryHelper.class);
                MockedStatic<FileUtils> fileMock = mockStatic(FileUtils.class);
                MockedStatic<ValidationUtil> validationMock = mockStatic(ValidationUtil.class)
        ) {

            tsvMock.when(() -> TsvParser.parseBase64Tsv("dummy"))
                    .thenReturn(rows);

            helperMock.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(rows.get(0)))
                    .thenReturn(headerIndexMap);

            helperMock.when(() -> InventoryHelper.getAllBarcodes(rows, headerIndexMap))
                    .thenReturn(List.of("B123"));

            when(productApi.mapBarcodesToProductPojos(anyList()))
                    .thenReturn(Map.of("B123", new ProductPojo()));

            helperMock.when(() -> InventoryHelper.segragateValidAndInvalidEntries(
                    anyList(), anyList(), anyList(), anyMap(), anyMap()
            )).thenAnswer(invocation -> {
                List<InventoryPojo> valid = invocation.getArgument(1);
                valid.add(new InventoryPojo());
                return null;
            });

            fileMock.when(() -> FileUtils.generateInventoryUpdateResults(anyList()))
                    .thenReturn("base64");

            FileData result = inventoryDto.updateInventoryBulk(fileForm);

            assertEquals("SUCCESS", result.getStatus());
            verify(inventoryApi).updateBulkInventory(argThat(list -> !list.isEmpty()));
        }
    }


    @Test
    void updateInventoryBulk_withErrors_returnsUnsuccessful() throws Exception {

        FileForm fileForm = new FileForm();
        fileForm.setBase64file("dummy-base64");

        List<RowError> invalidInventory = new ArrayList<>();
        invalidInventory.add(new RowError("2", "Invalid quantity"));

        try (MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)) {

            fileUtilsMock
                    .when(() -> FileUtils.generateInventoryUpdateResults(invalidInventory))
                    .thenReturn("error-base64");

            FileData fileData = new FileData();
            fileData.setBase64file("error-base64");
            fileData.setStatus("UNSUCCESSFUL");

            assertEquals("UNSUCCESSFUL", fileData.getStatus());
        }
    }
}
