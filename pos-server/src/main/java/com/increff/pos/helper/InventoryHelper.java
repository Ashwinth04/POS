package com.increff.pos.helper;

import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.RowError;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.increff.pos.util.FileUtils.getValueFromRow;

public class InventoryHelper {

    public static InventoryPojo convertToEntity(InventoryForm inventoryForm, String productId) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductId(productId);
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

    public static Map<String, ProductPojo> getBarcodeToProductPojoMap(List<ProductPojo> products) {
        Map<String, ProductPojo> barcodeToProductPojo = new HashMap<>();
        for (ProductPojo product : products) {
            barcodeToProductPojo.put(
                    product.getBarcode(),
                    product
            );
        }
        return barcodeToProductPojo;
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
        if (barcode.length() > 15) {
            throw new ApiException("Barcode cannot be more than 15 characters");
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

        if(quantity > 5000) throw new ApiException("Quantity cannot be greater than 5000");

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

    public static void segragateValidAndInvalidEntries(
            List<String[]> rows,
            List<InventoryPojo> validInventory,
            List<RowError> invalidInventory,
            Map<String, Integer> headerIndexMap,
            Map<String, ProductPojo> barcodeToProductPojo) {

        // Aggregation map
        Map<String, InventoryPojo> aggregated = new HashMap<>();

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (ValidationUtil.isRowEmpty(row)) continue;

            try {
                InventoryPojo pojo = InventoryHelper.convertRowToInventoryPojo(
                        row,
                        headerIndexMap,
                        barcodeToProductPojo
                );

                String barcode = getValueFromRow(row, headerIndexMap, "barcode");

                // Aggregate by barcode
                if (aggregated.containsKey(barcode)) {
                    InventoryPojo existing = aggregated.get(barcode);
                    existing.setQuantity(existing.getQuantity() + pojo.getQuantity());
                } else {
                    aggregated.put(barcode, pojo);
                }

            } catch (Exception e) {
                String barcode = getValueFromRow(row, headerIndexMap, "barcode");
                invalidInventory.add(new RowError(barcode, e.getMessage()));
            }
        }

        validInventory.addAll(aggregated.values());
    }

    public static boolean hasSufficientInventory(InventoryPojo item, InventoryPojo existingRecord) {
        int available = existingRecord.getQuantity();
        int required = item.getQuantity();
        return available >= required;
    }

    public static Map<String, ProductPojo> mapProductIdToProductPojo(Map<String, ProductPojo> barcodeToProductPojo) {
        Map<String, ProductPojo> productIdToProductPojo = new HashMap<>();

        for (ProductPojo product : barcodeToProductPojo.values()) {
            String productId = product.getId();
            productIdToProductPojo.put(productId, product);
        }

        return productIdToProductPojo;
    }

}
