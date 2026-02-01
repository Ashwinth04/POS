package com.increff.pos.helper;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.form.InventoryForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.increff.pos.constants.Constants.*;
import static com.increff.pos.constants.Constants.CLIENT_NAME;
import static com.increff.pos.constants.Constants.MRP;

public class InventoryHelper {

    public static InventoryPojo convertToEntity(InventoryForm inventoryForm) {

        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductId(inventoryForm.getProductId());
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        return inventoryPojo;
    }

    public static InventoryData convertToDto(InventoryPojo inventoryPojo) {

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

    public static void validateInventoryRows(List<String[]> rows) throws ApiException {

        if (rows == null || rows.isEmpty()) {
            throw new ApiException("TSV file is empty");
        }

        String[] headerRow = rows.get(0);
        Map<String, Integer> headerIndexMap = extractInventoryHeaderIndexMap(headerRow);

        List<String> missingHeaders = new ArrayList<>();

        if (!headerIndexMap.containsKey(BARCODE)) {
            missingHeaders.add("barcode");
        }
        if (!headerIndexMap.containsKey(INVENTORY)) {
            missingHeaders.add("inventory");
        }

        if (!missingHeaders.isEmpty()) {
            throw new ApiException("Missing required columns: " + missingHeaders);
        }
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

    public static void validateHeaders(Map<String, Integer> headerIndexMap) throws ApiException {

        List<String> requiredHeaders = List.of(
                BARCODE,
                INVENTORY
        );

        List<String> missing = requiredHeaders.stream()
                .filter(h -> !headerIndexMap.containsKey(h))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException("Missing required columns: " + missing);
        }
    }

    public static InventoryPojo toInventoryPojo(String[] row, Map<String, Integer> headerIndexMap, Map<String, String> barcodeToProductId) throws ApiException {

        Integer barcodeIndex = headerIndexMap.get("barcode");
        if (barcodeIndex == null || barcodeIndex >= row.length) {
            throw new ApiException("Barcode column missing");
        }

        String barcode = row[barcodeIndex].trim();
        if (barcode.isEmpty()) {
            throw new ApiException("Barcode is empty");
        }

        String productId = barcodeToProductId.get(barcode);
        if (productId == null) {
            throw new ApiException("Product not found for barcode: " + barcode);
        }

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

}
