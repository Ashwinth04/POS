package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.RowError;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.increff.pos.constants.Constants.*;
import static com.increff.pos.util.FileUtils.getValueFromRow;

public class InventoryHelper {

    public static InventoryPojo convertToEntity(InventoryForm inventoryForm) {

        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductId(inventoryForm.getProductId());
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        return inventoryPojo;
    }

    public static InventoryData convertToData(InventoryPojo inventoryPojo) {

        InventoryData inventoryData = new InventoryData();
        inventoryData.setProductId(inventoryPojo.getProductId());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        return inventoryData;
    }

    public static List<InventoryPojo> getPojosFromMap(Map<String, Integer> delta) {
        List<InventoryPojo> pojos = new ArrayList<>();

        for (String productId: delta.keySet()) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(productId);
            pojo.setQuantity(-delta.get(productId));
            pojos.add(pojo);
        }

        return pojos;
    }

    public static Map<String, Integer> extractInventoryHeaderIndexMap(String[] headerRow) {

        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < headerRow.length; i++) {
            String normalized = headerRow[i]
                    .trim()
                    .toLowerCase()
                    .replace(" ", "")
                    .replace("_", "");

            map.put(normalized, i);
        }

        return map;
    }

    public static InventoryPojo convertRowToInventoryPojo(String[] row, Map<String, Integer> headerIndexMap, Map<String, ProductPojo> barcodeToProductId) throws ApiException {

        Integer barcodeIndex = headerIndexMap.get("barcode");
        if (barcodeIndex == null || barcodeIndex >= row.length) {
            throw new ApiException("Barcode column missing");
        }

        String barcode = row[barcodeIndex].trim();
        if (barcode.isEmpty()) {
            throw new ApiException("Barcode is empty");
        }

        ProductPojo productPojo = barcodeToProductId.get(barcode);
        if (productPojo == null) {
            throw new ApiException("Product not found for barcode: " + barcode);
        }
        String productId = productPojo.getId();

        Integer qtyIndex = headerIndexMap.get("quantity");
        if (qtyIndex == null || qtyIndex >= row.length) {
            throw new ApiException("Quantity column missing");
        }

        String qtyStr = row[qtyIndex].trim();
        if (qtyStr.isEmpty()) {
            throw new ApiException("Quantity is empty");
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            throw new ApiException("Quantity is not a number");
        }

        if (quantity < 0) {
            throw new ApiException("Quantity cannot be negative");
        }

        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(productId);
        pojo.setQuantity(quantity);

        return pojo;
    }

    public static List<String> getAllBarcodes(List<String[]> rows, Map<String, Integer> headerIndexMap) {

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

        return barcodes;
    }

    public static void segragateValidAndInvalidEntries(List<String[]> rows, List<InventoryPojo> validInventory, List<RowError> invalidInventory, Map<String, Integer> headerIndexMap, Map<String, ProductPojo> barcodeToProductPojo) {

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (ValidationUtil.isRowEmpty(row)) continue;

            try {
                InventoryPojo pojo = InventoryHelper.convertRowToInventoryPojo(
                        rows.get(i),
                        headerIndexMap,
                        barcodeToProductPojo
                );
                validInventory.add(pojo);
            } catch (Exception e) {
                String barcode = getValueFromRow(row, headerIndexMap, "barcode");
                invalidInventory.add(
                        new RowError(barcode, e.getMessage())
                );
            }
        }

    }

}
