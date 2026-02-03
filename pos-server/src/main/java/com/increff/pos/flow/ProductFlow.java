package com.increff.pos.flow;

import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductFlow {

    @Autowired
    private ProductApiImpl productApi;

    @Autowired
    private InventoryApiImpl inventoryApi;

    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {

        ProductPojo res = productApi.addProduct(productPojo);
        inventoryApi.createDummyInventoryRecord(productPojo.getId());

        return res;
    }

    public ProductPojo editProduct(ProductPojo productPojo) throws ApiException {

        return productApi.editProduct(productPojo);
    }

    public Page<ProductPojo> getAllProducts(int page, int size) {
        return productApi.getAllProducts(page, size);
    }

    public Map<String, InventoryPojo> getInventoryForProducts(Page<ProductPojo> page) {
        List<String> productIds = page.getContent()
                .stream()
                .map(ProductPojo::getId)
                .toList();

        return inventoryApi.getInventoryForProductIds(productIds);
    }


    public void addProductsBulk(List<ProductPojo> productPojos) throws ApiException {

        List<ProductPojo> savedProducts = productApi.addProductsBulk(productPojos);

        List<String> productIds = savedProducts.stream().map(ProductPojo::getId).toList();

        inventoryApi.createDummyInventoryRecordsBulk(productIds);

    }

    public Page<ProductPojo> searchProducts(String type, String query, int page,int size) throws ApiException {
        return productApi.searchProducts(type, query, page, size);
    }

    public Map<String, ProductPojo> findExistingProducts(List<String> barcodes) {
        return productApi.findExistingProducts(barcodes);
    }
}
