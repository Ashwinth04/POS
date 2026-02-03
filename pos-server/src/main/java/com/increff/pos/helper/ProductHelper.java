package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.ValidationUtil;

import java.util.Map;

import static com.increff.pos.constants.Constants.*;
import static com.increff.pos.util.FileUtils.getValueFromRow;

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

    public static ProductPojo convertRowToProductPojo(String[] row, Map<String, Integer> headerIndexMap) throws ApiException {

        ValidationUtil.validateProductRow(row, headerIndexMap);
        ProductPojo pojo = new ProductPojo();

        String barcode = getValueFromRow(row, headerIndexMap, BARCODE);
        pojo.setBarcode(barcode.toLowerCase());

        String name = getValueFromRow(row, headerIndexMap, PRODUCT_NAME);
        pojo.setName(name.toLowerCase());

        String clientName = getValueFromRow(row, headerIndexMap, CLIENT_NAME);
        pojo.setClientName(clientName);

        String mrpStr = getValueFromRow(row, headerIndexMap, MRP);
        double mrp = Double.parseDouble(mrpStr.trim());
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
