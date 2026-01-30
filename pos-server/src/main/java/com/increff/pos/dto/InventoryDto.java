package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.increff.pos.util.ValidationUtil.validateInventoryRow;
import static com.increff.pos.util.ValidationUtil.validateInventoryRows;

@Service
public class InventoryDto {

    private final InventoryApiImpl inventoryApi;

    public InventoryDto(InventoryApiImpl inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    public InventoryData updateInventory(String barcode, InventoryForm inventoryForm) throws ApiException {
        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(barcode, inventoryForm);
        InventoryPojo normalizedInventoryPojo = InventoryHelper.normalizeInventoryPojo(inventoryPojo);
        InventoryPojo updatedInventoryPojo = inventoryApi.updateSingleInventory(normalizedInventoryPojo);
        return InventoryHelper.convertToDto(updatedInventoryPojo);
    }

    public FileData updateInventoryBulk(FileForm fileForm) throws ApiException {

        //TODO change this this is wrong
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        validateInventoryRows(rows);

        List<InventoryPojo> validPojos = new ArrayList<>();
        Map<String, String> invalidPojos = new HashMap<>();

        List<String[]> dataRows = rows.subList(1, rows.size());

        validateAndSplitInventoryRows(dataRows, validPojos, invalidPojos);

        if (!validPojos.isEmpty()) {
            Map<String, String> dbInvalid = inventoryApi.bulkInventoryUpdate(validPojos);
            invalidPojos.putAll(dbInvalid);
        }

        return buildBulkUpdateResponse(invalidPojos);
    }

    private void validateAndSplitInventoryRows(List<String[]> rows, List<InventoryPojo> valid, Map<String, String> invalid) {

        for (String[] row : rows) {

            try {
                validateInventoryRow(row);
                InventoryPojo inventoryPojo = InventoryHelper.convertRowToEntity(row);
                valid.add(inventoryPojo);
            } catch (ApiException e) {
                invalid.put(row[0], e.getMessage());
            }

        }
    }

    private FileData buildBulkUpdateResponse(Map<String, String> invalidForms) {

        FileData fileData = new FileData();
        fileData.setBase64file(FileUtils.generateInventoryUpdateResults(invalidForms));
        fileData.setStatus(invalidForms.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");

        return fileData;
    }
}
