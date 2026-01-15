package com.increff.pos.api;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductApi {
    ProductPojo add(ProductPojo productPojo) throws ApiException;
    Map<String, ProductUploadResult> bulkAdd(List<ProductPojo> pojos) throws ApiException;
    InventoryPojo addInventory(ProductPojo productPojo) throws ApiException;
    InventoryPojo updateInventory(InventoryPojo inventoryPojo) throws ApiException;
    Page<ProductPojo> getAll(int page, int size);
}
