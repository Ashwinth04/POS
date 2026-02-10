package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
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
import static com.increff.pos.util.FileUtils.buildInventoryBulkUpdateResponse;

@Service
public class InventoryDto {

    @Autowired
    private InventoryApiImpl inventoryApi;

    @Autowired
    private ProductApiImpl productApi;

    public InventoryData updateInventory(InventoryForm inventoryForm) throws ApiException {
        NormalizationUtil.normalizeInventoryForm(inventoryForm);
        FormValidator.validate(inventoryForm);
        ProductPojo productPojo = productApi.getCheckByBarcode(inventoryForm.getBarcode());
        InventoryPojo inventoryPojo = InventoryHelper.convertToEntity(inventoryForm, productPojo.getId());
        inventoryApi.updateSingleInventory(inventoryPojo);
        return InventoryHelper.convertToData(inventoryPojo);
    }

    //TODO:: Do not ignore the negative quantity from the TSV , just sum all the rows of same product id ,
    // and increment it by that value. IF the overall qty becomes zero for the product after inc operation
    // then do that but still show them in the failed entries to the used
    public FileData updateInventoryBulk(FileForm fileForm) throws ApiException {
        FormValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        Map<String, Integer> headerIndexMap = extractInventoryHeaderIndexMap(rows.get(0));
        ValidationUtil.validateHeaders(headerIndexMap);
        ValidationUtil.validateRowLimit(rows);

        List<String> barcodes = getAllBarcodes(rows, headerIndexMap);
        List<ProductPojo> products = productApi.getProductPojosForBarcodes(new ArrayList<>(barcodes));
        Map<String, ProductPojo> barcodeToProductPojoMap = InventoryHelper.getBarcodeToProductPojoMap(products);

        List<InventoryPojo> validInventory = new ArrayList<>();
        List<RowError> invalidInventory = new ArrayList<>();

        segragateValidAndInvalidEntries(rows, validInventory, invalidInventory, headerIndexMap, barcodeToProductPojoMap);
        inventoryApi.updateBulkInventory(validInventory);
        return buildInventoryBulkUpdateResponse(invalidInventory);
    }
}