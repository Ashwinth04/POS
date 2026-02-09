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

    // TODO: Create inventory flow
    @Autowired
    private ProductApiImpl productApi;

    public InventoryData updateInventory(InventoryForm inventoryForm) throws ApiException {

        FormValidator.validate(inventoryForm);
        NormalizationUtil.normalizeInventoryForm(inventoryForm);
        String barcode = inventoryForm.getBarcode();
        List<ProductPojo> products = productApi.mapBarcodesToProductPojos(Collections.singletonList(barcode));

        // TODO: Move this to helper
        Map<String, ProductPojo> barcodeToProductId = new HashMap<>();

        for (ProductPojo product : products) {
            barcodeToProductId.put(
                    product.getBarcode(),
                    product
            );
        }

        ProductPojo productPojo = barcodeToProductId.get(barcode);
        if (productPojo == null) {
            throw new ApiException("Product not found for barcode: " + barcode);
        }
        String productId = productPojo.getId();
        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(inventoryForm, productId);
        inventoryApi.updateSingleInventory(inventoryPojo);
        return InventoryHelper.convertToData(inventoryPojo);
    }

    public FileData updateInventoryBulk(FileForm fileForm) throws ApiException {

        FormValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        Map<String, Integer> headerIndexMap = extractInventoryHeaderIndexMap(rows.get(0));
        ValidationUtil.validateHeaders(headerIndexMap);
        ValidationUtil.validateRowLimit(rows);

        List<String> barcodes = getAllBarcodes(rows, headerIndexMap);

        List<ProductPojo> products = productApi.mapBarcodesToProductPojos(new ArrayList<>(barcodes));

        Map<String, ProductPojo> barcodeToProductPojo = new HashMap<>();

        for (ProductPojo product : products) {
            barcodeToProductPojo.put(
                    product.getBarcode(),
                    product
            );
        }

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

}
