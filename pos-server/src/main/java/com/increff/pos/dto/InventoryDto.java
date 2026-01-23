package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.increff.pos.util.FileUtils.getInventoryFormsFromFile;

@Service
public class InventoryDto {

    private final InventoryApiImpl inventoryApi;

    public InventoryDto(InventoryApiImpl inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    public InventoryData updateInventory(String barcode, InventoryForm inventoryForm) throws ApiException {
        ValidationUtil.validateInventoryForm(inventoryForm);
        InventoryPojo inventoryPojo = ProductHelper.convertToInventoryEntity(barcode, inventoryForm);
        InventoryPojo updatedInventoryPojo = inventoryApi.updateInventory(inventoryPojo);
        return ProductHelper.convertToInventoryDto(updatedInventoryPojo);
    }

    public FileData addProductsInventory(FileForm base64file) throws ApiException {

        List<InventoryPojo> inventoryPojos =
                getInventoryFormsFromFile(base64file.getBase64file());

        List<InventoryPojo> validPojos = new ArrayList<>();
        Map<String, String> invalidPojos = new HashMap<>();

        validateAndSplit(inventoryPojos, validPojos, invalidPojos);

        if (!validPojos.isEmpty()) {
            Map<String, String> dbInvalid = inventoryApi.bulkInventoryUpdate(validPojos);
            invalidPojos.putAll(dbInvalid);
        }

        return buildResponse(invalidPojos);
    }

    private void validateAndSplit(List<InventoryPojo> pojos, List<InventoryPojo> valid, Map<String, String> invalid) {

        for (InventoryPojo pojo : pojos) {
            String error = ValidationUtil.validateInventoryUpdateForm(pojo);

            if (error == null) {

                valid.add(pojo);
            } else {
                invalid.put(pojo.getBarcode(), error);
            }
        }
    }

    private FileData buildResponse(Map<String, String> invalidForms) {

        FileData fileData = new FileData();
        fileData.setBase64file(FileUtils.getBase64InventoryUpdate(invalidForms));
        fileData.setStatus(invalidForms.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");

        return fileData;
    }
}
