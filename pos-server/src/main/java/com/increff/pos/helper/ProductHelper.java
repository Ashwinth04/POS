package com.increff.pos.helper;

import com.increff.pos.controller.ProductController;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductDisplayData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.ProductForm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    public static ProductData convertToDto(ProductPojo productPojo) {

        ProductData productData = new ProductData();
        productData.setId(productPojo.getId());
        productData.setName(productPojo.getName());
        productData.setMrp(productPojo.getMrp());
        productData.setBarcode(productPojo.getBarcode());
        productData.setClientName(productPojo.getClientName());
        productData.setImageUrl(productPojo.getImageUrl());

        return productData;
    }

    public static void validateHeaders(Map<String, Integer> headerIndexMap) throws ApiException {

        List<String> requiredHeaders = List.of(
                BARCODE,
                PRODUCT_NAME,
                CLIENT_NAME,
                MRP
        );

        List<String> missing = requiredHeaders.stream()
                .filter(h -> !headerIndexMap.containsKey(h))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException("Missing required columns: " + missing);
        }
    }

    private static String getValue(String[] row, Map<String, Integer> headerIndexMap, String header) {
        Integer index = headerIndexMap.get(header);
        if (index == null || index >= row.length) {
            return null;
        }
        return row[index].trim();
    }

    public static ProductPojo toProductPojo(
            String[] row,
            Map<String, Integer> headerIndexMap
    ) throws ApiException {

        ProductPojo pojo = new ProductPojo();

        pojo.setBarcode(getValue(row, headerIndexMap, BARCODE));
        pojo.setName(getValue(row, headerIndexMap, PRODUCT_NAME).toLowerCase());
        pojo.setClientName(getValue(row, headerIndexMap, CLIENT_NAME));

        String mrpStr = getValue(row, headerIndexMap, MRP);
        if (mrpStr != null) {
            try {
                pojo.setMrp(Double.parseDouble(mrpStr));
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid MRP: " + mrpStr);
            }
        }

        pojo.setImageUrl(getValue(row, headerIndexMap, IMAGE_URL));

        return pojo;
    }

}
