package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.increff.pos.helper.InventoryHelper.extractInventoryHeaderIndexMap;

@Service
public class InventoryDto {

    @Autowired
    private InventoryApiImpl inventoryApi;

    @Autowired
    private ProductApiImpl productApi;

    @Autowired
    private FormValidator formValidator;

    public InventoryData updateInventory(InventoryForm inventoryForm) throws ApiException {

        formValidator.validate(inventoryForm);
        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(inventoryForm);
        inventoryApi.updateSingleInventory(inventoryPojo);
        return InventoryHelper.convertToData(inventoryPojo);

    }

    public FileData updateInventoryBulk(FileForm fileForm) throws ApiException {

        formValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        List<InventoryPojo> validInventory = new ArrayList<>();
        List<RowError> invalidInventory = new ArrayList<>();

        Map<String, Integer> headerIndexMap = extractInventoryHeaderIndexMap(rows.get(0));
        InventoryHelper.validateHeaders(headerIndexMap);

        if (rows.size() > 5000) throw new ApiException("Maximum row limit exceeded!");

        List<String> barcodes = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (ValidationUtil.isRowEmpty(row)) continue;

            Integer barcodeIndex = headerIndexMap.get("barcode");
            if (barcodeIndex != null && barcodeIndex < row.length) {
                String barcode = row[barcodeIndex].trim();
                if (!barcode.isEmpty()) {
                    barcodes.add(barcode.toLowerCase());
                }
            }
        }

        Map<String, String> barcodeToProductId = productApi.mapBarcodesToProductIds(new ArrayList<>(barcodes));

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (ValidationUtil.isRowEmpty(row)) continue;

            // TODO: check if this is actually useful
            Integer barcodeIndex = headerIndexMap.get("barcode");
            String barcode = rows.get(i)[0];
            if (barcodeIndex != null && barcodeIndex < rows.get(i).length) {
                barcode = rows.get(i)[barcodeIndex].trim();
                if (!barcode.isEmpty()) {
                    barcodes.add(barcode);
                }
            }

            try {
                InventoryPojo pojo = InventoryHelper.convertRowToInventoryPojo(
                        rows.get(i),
                        headerIndexMap,
                        barcodeToProductId
                );
                validInventory.add(pojo);
            } catch (Exception e) {
                invalidInventory.add(
                        new RowError(barcode, e.getMessage())
                );
            }
        }

        inventoryApi.updateBulkInventory(validInventory);

        return buildBulkUpdateResponse(invalidInventory);
    }

    private FileData buildBulkUpdateResponse(List<RowError> invalidInventory) {

        FileData fileData = new FileData();
        fileData.setBase64file(FileUtils.generateInventoryUpdateResults(invalidInventory));
        fileData.setStatus(invalidInventory.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");

        return fileData;
    }
}
