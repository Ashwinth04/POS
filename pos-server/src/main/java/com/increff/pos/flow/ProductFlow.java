package com.increff.pos.flow;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.InventoryApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.constants.ProductSearchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {
        ProductPojo res = productApi.addProduct(productPojo);
        inventoryApi.createDummyInventoryRecord(productPojo.getId());
        return res;
    }

    @Transactional(readOnly = true)
    public Map<String, InventoryPojo> getInventoryForProducts(Page<ProductPojo> page) {

        List<String> productIds = page.getContent()
                .stream()
                .map(ProductPojo::getId)
                .toList();

        List<InventoryPojo> inventoryPojos = inventoryApi.getInventoryForProductIds(productIds);

         return inventoryPojos
                .stream()
                .collect(Collectors.toMap(
                        InventoryPojo::getProductId,
                        Function.identity()
                ));
    }

    @Transactional(rollbackFor = Exception.class)
    public void addProductsBulk(List<ProductPojo> productPojos) {
        List<ProductPojo> savedProducts = productApi.addProductsBulk(productPojos);
        List<String> productIds = savedProducts.stream().map(ProductPojo::getId).toList();
        inventoryApi.createDummyInventoryRecordsBulk(productIds);
    }

    @Transactional(readOnly = true)
    public Page<ProductPojo> searchProducts(ProductSearchType type, String query, int page, int size) throws ApiException {
        return productApi.searchProducts(type, query, page, size);
    }

    @Transactional(readOnly = true)
    public ClientPojo getCheckByClientName(String clientName) throws ApiException {
        return clientApi.getCheckByClientName(clientName);
    }

    @Transactional(readOnly = true)
    public Map<String, ClientPojo> fetchExistingClientNames(List<String> clientNames) {
        List<ClientPojo> clientPojos = clientApi.fetchExistingClientNames(clientNames);

        return clientPojos.stream()
                .collect(Collectors.toMap(
                        ClientPojo::getName,
                        Function.identity()
                ));
    }
}
