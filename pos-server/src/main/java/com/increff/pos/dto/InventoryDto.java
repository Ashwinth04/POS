package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.RowError;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.increff.pos.helper.InventoryHelper.*;

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
        NormalizationUtil.normalizeInventoryForm(inventoryForm);
        String barcode = inventoryForm.getBarcode();
        String productId = getProductIdFromBarcode(barcode);
        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(inventoryForm, productId);
        inventoryApi.updateSingleInventory(inventoryPojo);
        return InventoryHelper.convertToData(inventoryPojo);
    }

    public FileData updateInventoryBulk(FileForm fileForm) throws ApiException {

        formValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        Map<String, Integer> headerIndexMap = extractInventoryHeaderIndexMap(rows.get(0));
        ValidationUtil.validateHeaders(headerIndexMap);
        ValidationUtil.validateRowLimit(rows);

        List<String> barcodes = getAllBarcodes(rows, headerIndexMap);

        Map<String, ProductPojo> barcodeToProductPojo = productApi.mapBarcodesToProductPojos(new ArrayList<>(barcodes));

        List<InventoryPojo> validInventory = new ArrayList<>();
        List<RowError> invalidInventory = new ArrayList<>();

        segragateValidAndInvalidEntries(rows, validInventory, invalidInventory, headerIndexMap, barcodeToProductPojo);

        inventoryApi.updateBulkInventory(validInventory);

        return buildBulkUpdateResponse(invalidInventory);
    }

    private FileData buildBulkUpdateResponse(List<RowError> invalidInventory) {

        FileData fileData = new FileData();
        fileData.setBase64file(FileUtils.generateInventoryUpdateResults(invalidInventory));
        fileData.setStatus(invalidInventory.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");

        return fileData;
    }

    // TODO: Dont use this, ust use productApi's method for single update as well
    private String getProductIdFromBarcode(String barcode) throws ApiException {

        Map<String, ProductPojo> barcodeToProductId = productApi.mapBarcodesToProductPojos(Collections.singletonList(barcode));
        ProductPojo productPojo = barcodeToProductId.get(barcode);
        if (productPojo == null) {
            throw new ApiException("Product not found for barcode: " + barcode);
        }

        return productPojo.getId();
    }
}
