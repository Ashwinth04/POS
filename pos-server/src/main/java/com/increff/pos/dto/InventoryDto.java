package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.RowError;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.increff.pos.helper.InventoryHelper.extractInventoryHeaderIndexMap;
import static com.increff.pos.helper.ProductHelper.toProductPojo;
import static com.increff.pos.util.ValidationUtil.*;

@Service
public class InventoryDto {

    @Autowired
    private InventoryApiImpl inventoryApi;

    @Autowired
    private ProductApiImpl productApi;

    public InventoryData updateInventory(InventoryForm inventoryForm) throws ApiException {

        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(inventoryForm);
        inventoryApi.updateSingleInventory(inventoryPojo);
        return InventoryHelper.convertToDto(inventoryPojo);

    }

    public FileData updateInventoryBulk(FileForm fileForm) throws ApiException {

        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        List<InventoryPojo> validInventory = new ArrayList<>();
        List<RowError> invalidInventory = new ArrayList<>();

        Map<String, Integer> headerIndexMap = extractInventoryHeaderIndexMap(rows.get(0));
        InventoryHelper.validateHeaders(headerIndexMap);

        if (rows.size() > 5000) throw new ApiException("Maximum row limit exceeded!");

        List<String> barcodes = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);

            Integer barcodeIndex = headerIndexMap.get("barcode");
            if (barcodeIndex != null && barcodeIndex < row.length) {
                String barcode = row[barcodeIndex].trim();
                if (!barcode.isEmpty()) {
                    barcodes.add(barcode);
                }
            }
        }

        Map<String, String> barcodeToProductId = productApi.mapBarcodesToProductIds(new ArrayList<>(barcodes));

        for (int i = 1; i < rows.size(); i++) {
            try {
                InventoryPojo pojo = InventoryHelper.toInventoryPojo(
                        rows.get(i),
                        headerIndexMap,
                        barcodeToProductId
                );
                validInventory.add(pojo);
            } catch (Exception e) {
                invalidInventory.add(
                        new RowError(rows.get(i)[0], e.getMessage())
                );
            }
        }

        inventoryApi.bulkInventoryUpdate(validInventory);

        return buildBulkUpdateResponse(invalidInventory);
    }

    private FileData buildBulkUpdateResponse(List<RowError> invalidInventory) {

        FileData fileData = new FileData();
        fileData.setBase64file(FileUtils.generateInventoryUpdateResults(invalidInventory));
        fileData.setStatus(invalidInventory.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");

        return fileData;
    }
}
