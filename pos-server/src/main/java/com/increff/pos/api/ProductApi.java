package com.increff.pos.api;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductApi {
    ProductPojo addProduct(ProductPojo productPojo) throws ApiException;
    Map<String, ProductUploadResult> addProductsBulk(List<ProductPojo> pojos, List<String> existingClientNames) throws ApiException;
    Page<ProductPojo> getAllProducts(int page, int size);
}
