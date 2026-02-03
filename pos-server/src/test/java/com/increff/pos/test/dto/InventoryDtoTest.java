//package com.increff.pos.test.dto;
//
//import com.increff.pos.api.InventoryApiImpl;
//import com.increff.pos.api.ProductApiImpl;
//import com.increff.pos.db.InventoryPojo;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.dto.InventoryDto;
//import com.increff.pos.helper.InventoryHelper;
//import com.increff.pos.model.data.FileData;
//import com.increff.pos.model.data.InventoryData;
//import com.increff.pos.model.form.FileForm;
//import com.increff.pos.model.form.InventoryForm;
//import com.increff.pos.util.FormValidator;
//import com.increff.pos.util.TsvParser;
//import com.increff.pos.util.ValidationUtil;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class InventoryDtoTest {
//
//    @InjectMocks
//    private InventoryDto inventoryDto;
//
//    @Mock
//    private InventoryApiImpl inventoryApi;
//
//    @Mock
//    private ProductApiImpl productApi;
//
//    @Mock
//    private FormValidator formValidator;
//
//    // Helper methods
//    private InventoryForm inventoryForm() {
//        InventoryForm f = new InventoryForm();
//        f.setProductId("p1");
//        f.setQuantity(10);
//        return f;
//    }
//
//    private InventoryPojo inventoryPojo(String productId, int qty) {
//        InventoryPojo p = new InventoryPojo();
//        p.setProductId(productId);
//        p.setQuantity(qty);
//        return p;
//    }
//
//    // ---------------- Single Update ----------------
//    @Test
//    void testUpdateInventory() throws ApiException {
//        InventoryForm form = inventoryForm();
//        InventoryPojo pojo = inventoryPojo("p1", 10);
//
//        doNothing().when(formValidator).validate(any(InventoryForm.class));
//
//        try (MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class)) {
//
//            helper.when(() -> InventoryHelper.convertToEntity(form)).thenReturn(pojo);
//            helper.when(() -> InventoryHelper.convertToData(pojo)).thenReturn(new InventoryData());
//
//            inventoryDto.updateInventory(form);
//
//            verify(inventoryApi).updateSingleInventory(pojo);
//        }
//    }
//
//    // ---------------- Bulk Update ----------------
//    @Test
//    void testUpdateInventoryBulk_allBranches() throws Exception {
//        FileForm fileForm = new FileForm();
//        fileForm.setBase64file("dummy");
//
//        String[] header = {"productId", "quantity"};
//        String[] row1 = {"p1", "10"};
//        String[] row2 = {"p2", "20"};
//        String[] emptyRow = {"", ""};
//        List<String[]> rows = List.of(header, row1, row2, emptyRow);
//
//        doNothing().when(formValidator).validate(fileForm);
//
//        try (MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
//             MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class);
//             MockedStatic<ValidationUtil> val = mockStatic(ValidationUtil.class);
//             MockedStatic<com.increff.pos.util.FileUtils> fileUtils = mockStatic(com.increff.pos.util.FileUtils.class)) {
//
//            tsv.when(() -> TsvParser.parseBase64Tsv("dummy")).thenReturn(rows);
//
//            val.when(() -> ValidationUtil.isRowEmpty(emptyRow)).thenReturn(true);
//            val.when(() -> ValidationUtil.isRowEmpty(any())).thenReturn(false);
//
//            helper.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(header))
//                    .thenReturn(Map.of("productId", 0, "quantity", 1));
//
//            // ✅ Correct way to stub static void
//            helper.when(() -> InventoryHelper.validateHeaders(any()))
//                    .thenAnswer(invocation -> null);
//
//            Map<String, String> productIdMap = Map.of("p1", "p1", "p2", "p2");
//            when(productApi.mapBarcodesToProductPojos(any())).thenReturn(productIdMap);
//
//            InventoryPojo pojo1 = inventoryPojo("p1", 10);
//            InventoryPojo pojo2 = inventoryPojo("p2", 20);
//
//            helper.when(() -> InventoryHelper.convertRowToInventoryPojo(row1, any(), any())).thenReturn(pojo1);
//            helper.when(() -> InventoryHelper.convertRowToInventoryPojo(row2, any(), any())).thenReturn(pojo2);
//
//            fileUtils.when(() -> com.increff.pos.util.FileUtils.generateInventoryUpdateResults(any()))
//                    .thenReturn("base64dummy");
//
//            FileData result = inventoryDto.updateInventoryBulk(fileForm);
//
//            assertEquals("SUCCESS", result.getStatus());
//            assertEquals("base64dummy", result.getBase64file());
//
//            verify(inventoryApi).updateBulkInventory(List.of(pojo1, pojo2));
//        }
//    }
//
//    @Test
//    void testUpdateInventoryBulk_rowConversionException() throws Exception {
//        FileForm fileForm = new FileForm();
//        fileForm.setBase64file("dummy");
//
//        String[] header = {"productId", "quantity"};
//        String[] row1 = {"p1", "10"};
//        String[] row2 = {"p2", "20"}; // conversion fails
//        List<String[]> rows = List.of(header, row1, row2);
//
//        doNothing().when(formValidator).validate(fileForm);
//
//        try (MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
//             MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class);
//             MockedStatic<ValidationUtil> val = mockStatic(ValidationUtil.class);
//             MockedStatic<com.increff.pos.util.FileUtils> fileUtils = mockStatic(com.increff.pos.util.FileUtils.class)) {
//
//            tsv.when(() -> TsvParser.parseBase64Tsv("dummy")).thenReturn(rows);
//
//            helper.when(() -> InventoryHelper.validateHeaders(any()))
//                    .thenAnswer(invocation -> null);
//
//            val.when(() -> ValidationUtil.isRowEmpty(any())).thenReturn(false);
//
//            Map<String, String> productIdMap = Map.of("p1", "p1", "p2", "p2");
//            when(productApi.mapBarcodesToProductPojos(any())).thenReturn(productIdMap);
//
//            InventoryPojo pojo1 = inventoryPojo("p1", 10);
//            helper.when(() -> InventoryHelper.convertRowToInventoryPojo(row1, any(), any())).thenReturn(pojo1);
//            helper.when(() -> InventoryHelper.convertRowToInventoryPojo(row2, any(), any()))
//                    .thenThrow(new RuntimeException("Conversion failed"));
//
//            fileUtils.when(() -> com.increff.pos.util.FileUtils.generateInventoryUpdateResults(any()))
//                    .thenReturn("base64dummy");
//
//            FileData result = inventoryDto.updateInventoryBulk(fileForm);
//
//            assertEquals("UNSUCCESSFUL", result.getStatus());
//        }
//    }
//
//    @Test
//    void testUpdateInventoryBulk_maxRowsExceeded() {
//        FileForm fileForm = new FileForm();
//        List<String[]> rows = new ArrayList<>();
//        rows.add(new String[]{"productId", "quantity"});
//        for (int i = 0; i < 5001; i++) rows.add(new String[]{"p" + i, "10"});
//
//        try (MockedStatic<TsvParser> tsv = mockStatic(TsvParser.class);
//             MockedStatic<InventoryHelper> helper = mockStatic(InventoryHelper.class)) {
//
//            // TSV parsing
//            tsv.when(() -> TsvParser.parseBase64Tsv(anyString())).thenReturn(rows);
//
//            // Header extraction
//            helper.when(() -> InventoryHelper.extractInventoryHeaderIndexMap(any()))
//                    .thenReturn(Map.of("productId", 0, "quantity", 1));
//
//            // Void method stub — do nothing
//            helper.when(() -> InventoryHelper.validateHeaders(any())).thenAnswer(invocation -> null);
//
//            // Test exception for exceeding max rows
//            assertThrows(ApiException.class, () -> inventoryDto.updateInventoryBulk(fileForm));
//        }
//    }
//
//}
