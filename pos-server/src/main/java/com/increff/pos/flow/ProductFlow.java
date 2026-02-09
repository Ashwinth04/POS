package com.increff.pos.flow;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.constants.ProductSearchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductFlow {

    @Autowired
    private ProductApiImpl productApi;

    @Autowired
    private InventoryApiImpl inventoryApi;

    @Autowired
    private ClientApiImpl clientApi;

    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {

        ProductPojo res = productApi.addProduct(productPojo);
        inventoryApi.createDummyInventoryRecord(productPojo.getId());

        return res;
    }

    public Map<String, InventoryPojo> getInventoryForProducts(Page<ProductPojo> page) {
        List<String> productIds = page.getContent()
                .stream()
                .map(ProductPojo::getId)
                .toList();

        List<InventoryPojo> inventoryPojos = inventoryApi.getInventoryForProductIds(productIds);

        Map<String, InventoryPojo> productIdToInventory = inventoryPojos
                .stream()
                .collect(Collectors.toMap(
                        InventoryPojo::getProductId,
                        Function.identity()
                ));

        return productIdToInventory;
    }


    public void addProductsBulk(List<ProductPojo> productPojos) throws ApiException {

        List<ProductPojo> savedProducts = productApi.addProductsBulk(productPojos);

        List<String> productIds = savedProducts.stream().map(ProductPojo::getId).toList();

        inventoryApi.createDummyInventoryRecordsBulk(productIds);

    }

    public Page<ProductPojo> searchProducts(ProductSearchType type, String query, int page, int size) throws ApiException {
        return productApi.searchProducts(type, query, page, size);
    }

    public Map<String, ProductPojo> findExistingProducts(List<String> barcodes) {
        List<ProductPojo> existingPojos = productApi.findExistingProducts(barcodes);

        return existingPojos.stream()
                .collect(Collectors.toMap(
                        ProductPojo::getBarcode,
                        Function.identity()
                ));
    }

    public ClientPojo getCheckByClientName(String clientName) throws ApiException {
        return clientApi.getCheckByClientName(clientName);
    }

    public Map<String, ClientPojo> fetchExistingClientNames(List<String> clientNames) {
        List<ClientPojo> clientPojos = clientApi.fetchExistingClientNames(clientNames);

        return clientPojos.stream()
                .collect(Collectors.toMap(
                        ClientPojo::getName,
                        Function.identity()
                ));
    }
}
