package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductDisplayData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.ProductForm;

import java.util.ArrayList;
import java.util.List;

public class ProductHelper {
    public static ProductPojo convertToEntity(ProductForm productForm) {

        ProductPojo productPojo = new ProductPojo();
        productPojo.setName(productForm.getName());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setBarcode(productForm.getBarcode().toLowerCase());
        productPojo.setClientName(productForm.getClientName());
        productPojo.setImageUrl(productForm.getImageUrl());
        return productPojo;
    }

    public static ProductPojo convertRowToEntity(String[] row) {

        ProductPojo productPojo = new ProductPojo();
        productPojo.setName(row[2].trim());
        productPojo.setMrp(Double.parseDouble(row[3].trim()));
        productPojo.setBarcode(row[0].toLowerCase().trim());
        productPojo.setClientName(row[1].trim());
        productPojo.setImageUrl(row[4].trim());
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
}
