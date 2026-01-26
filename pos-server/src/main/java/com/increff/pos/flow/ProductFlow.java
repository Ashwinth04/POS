package com.increff.pos.flow;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApi;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductFlow {

    private final ProductApiImpl productApi;
    private final InventoryApiImpl inventoryApi;
    private final ClientApiImpl clientApi;

    public ProductFlow(ProductApiImpl productApi, InventoryApiImpl inventoryApi, ClientApiImpl clientApi) {
        this.productApi = productApi;
        this.inventoryApi = inventoryApi;
        this.clientApi = clientApi;
    }

    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {

        String clientName = productPojo.getClientName();
        clientApi.checkClientExists(clientName);

        ProductPojo res = productApi.addProduct(productPojo);
        inventoryApi.createDummyInventoryRecord(productPojo.getBarcode());

        return res;
    }

    public ProductPojo editProduct(ProductPojo productPojo) throws ApiException {

        String clientName = productPojo.getClientName();
        clientApi.checkClientExists(clientName);

        return productApi.editProduct(productPojo);
    }

    public Page<ProductPojo> getAllProducts(int page, int size) {
        return productApi.getAllProducts(page, size);
    }

    public Map<String, ProductUploadResult> addProductsBulk(List<ProductPojo> productPojos) throws ApiException {

        List<String> existingClientNames = clientApi.fetchExistingClientNames(productPojos);

        Map<String, ProductUploadResult> resultMap = productApi.addProductsBulk(productPojos, existingClientNames);

        for (String barcode: resultMap.keySet()) {
            ProductUploadResult r = resultMap.get(barcode);
            if (r.getStatus().equals("SUCCESS")) {
                inventoryApi.createDummyInventoryRecord(barcode);
            }
        }

        return resultMap;
    }
}
