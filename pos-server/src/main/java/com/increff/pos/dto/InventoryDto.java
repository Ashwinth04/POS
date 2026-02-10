package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.InventoryFlow;
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
import static com.increff.pos.util.FileUtils.buildInventoryBulkUpdateResponse;

@Service
public class InventoryDto {

    @Autowired
    private InventoryApiImpl inventoryApi;

    @Autowired
    private InventoryFlow inventoryFlow;

    public InventoryData updateInventory(InventoryForm inventoryForm) throws ApiException {
        NormalizationUtil.normalizeInventoryForm(inventoryForm);
        FormValidator.validate(inventoryForm);
        ProductPojo productPojo = inventoryFlow.getCheckByBarcode(inventoryForm.getBarcode());
        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(inventoryForm, productPojo.getId());
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
        List<ProductPojo> products = inventoryFlow.getProductPojosForBarcodes(new ArrayList<>(barcodes));
        Map<String, ProductPojo> barcodeToProductPojoMap = InventoryHelper.getBarcodeToProductPojoMap(products);

        List<InventoryPojo> validInventory = new ArrayList<>();
        List<RowError> invalidInventory = new ArrayList<>();

        segragateValidAndInvalidEntries(rows, validInventory, invalidInventory, headerIndexMap, barcodeToProductPojoMap);
        List<String> invalidProductIds = inventoryApi.updateBulkInventory(validInventory);
        Map<String, ProductPojo> productIdToProductPojoMap = InventoryHelper.mapProductIdToProductPojo(barcodeToProductPojoMap);

        for (String productId: invalidProductIds) {
            RowError err = new RowError();
            String barcode = productIdToProductPojoMap.get(productId).getBarcode();
            err.setBarcode(barcode);
            err.setMessage("Negative net inventory update. Setting the inventory to zero");
            invalidInventory.add(err);
        }

        return buildInventoryBulkUpdateResponse(invalidInventory);
    }
}