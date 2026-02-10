package com.increff.pos.api;

import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductApi {
    ProductPojo addProduct(ProductPojo productPojo) throws ApiException;
    List<ProductPojo> addProductsBulk(List<ProductPojo> pojos) throws ApiException;
    Page<ProductPojo> getAllProducts(int page, int size);
}
