package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;

import java.util.Map;

import static com.increff.pos.constants.Constants.*;

public class ProductHelper {
    public static ProductPojo convertToEntity(ProductForm productForm) {

        ProductPojo productPojo = new ProductPojo();
        productPojo.setName(productForm.getName().trim());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setBarcode(productForm.getBarcode().toLowerCase().trim());
        productPojo.setClientName(productForm.getClientName().trim());
        productPojo.setImageUrl(productForm.getImageUrl().trim());
        return productPojo;
    }

    public static ProductData convertToData(ProductPojo productPojo) {

        ProductData productData = new ProductData();
        productData.setId(productPojo.getId());
        productData.setName(productPojo.getName());
        productData.setMrp(productPojo.getMrp());
        productData.setBarcode(productPojo.getBarcode());
        productData.setClientName(productPojo.getClientName());
        productData.setImageUrl(productPojo.getImageUrl());

        return productData;
    }

    private static String getValueFromRow(String[] row, Map<String, Integer> headerIndexMap, String header) {
        Integer index = headerIndexMap.get(header);
        if (index == null || index >= row.length) {
            return null;
        }
        return row[index].trim();
    }

    public static ProductPojo convertRowToProductPojo(String[] row, Map<String, Integer> headerIndexMap) throws ApiException {

        ProductPojo pojo = new ProductPojo();

        String barcode = getValueFromRow(row, headerIndexMap, BARCODE);
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }
        pojo.setBarcode(barcode.toLowerCase());

        String name = getValueFromRow(row, headerIndexMap, PRODUCT_NAME);
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }
        pojo.setName(name.toLowerCase());

        String clientName = getValueFromRow(row, headerIndexMap, CLIENT_NAME);
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }
        pojo.setClientName(clientName);

        String mrpStr = getValueFromRow(row, headerIndexMap, MRP);
        if (mrpStr == null || mrpStr.trim().isEmpty()) {
            throw new ApiException("MRP cannot be empty");
        }

        double mrp;
        try {
            mrp = Double.parseDouble(mrpStr.trim());
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid MRP: " + mrpStr);
        }

        // simple validations
        if (mrp <= 0 || Double.isNaN(mrp) || Double.isInfinite(mrp) || mrp > 1_000_000) {
            throw new ApiException("Invalid MRP: " + mrpStr);
        }

        // optional: reject scientific notation (contains 'e' or 'E' in original string)
        if (mrpStr.toLowerCase().contains("e")) {
            throw new ApiException("MRP cannot be in scientific notation: " + mrpStr);
        }

        pojo.setMrp(mrp);

        pojo.setImageUrl(getValueFromRow(row, headerIndexMap, IMAGE_URL));

        return pojo;
    }

    public static ProductData convertToData(
            ProductPojo product,
            Map<String, InventoryPojo> inventoryByProductId
    ) {
        ProductData data = new ProductData();

        data.setId(product.getId());
        data.setName(product.getName());
        data.setMrp(product.getMrp());
        data.setClientName(product.getClientName());
        data.setBarcode(product.getBarcode());
        data.setImageUrl(product.getImageUrl());

        InventoryPojo inventory = inventoryByProductId.get(product.getId());
        if (inventory != null) {
            data.setQuantity(inventory.getQuantity());
        }

        return data;
    }
}
