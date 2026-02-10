package com.increff.pos.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.documents.ClientPojo;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.increff.pos.helper.ProductHelper.convertRowToProductPojo;
import static com.increff.pos.util.FileUtils.*;

@Service
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ProductApiImpl productApi;

    public ProductData createProduct(ProductForm productForm) throws ApiException {
        NormalizationUtil.normalizeProductForm(productForm);
        FormValidator.validate(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        productFlow.getCheckByClientName(productPojo.getClientName());
        ProductPojo savedProductPojo = productFlow.addProduct(productPojo);
        return ProductHelper.convertToData(savedProductPojo);
    }

    public ProductData editProduct(ProductForm productForm) throws ApiException {
        NormalizationUtil.normalizeProductForm(productForm);
        FormValidator.validate(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        productFlow.getCheckByClientName(productPojo.getClientName());
        ProductPojo editedPojo = productApi.editProduct(productPojo);
        return ProductHelper.convertToData(editedPojo);
    }

    public Page<ProductData> getAllProducts(PageForm form) throws ApiException {
        FormValidator.validate(form);
        Page<ProductPojo> productPage = productApi.getAllProducts(form.getPage(), form.getSize());
        Map<String, InventoryPojo> productIdToInventoryPojo = productFlow.getInventoryForProducts(productPage);

        return productPage.map(
                product -> ProductHelper.convertToData(product, productIdToInventoryPojo)
        );
    }

    public FileData createProducts(FileForm fileForm) throws ApiException {
        FormValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        List<ProductPojo> validProducts = new ArrayList<>();
        List<RowError> invalidProducts = new ArrayList<>();

        Map<String, Integer> headerIndexMap = ProductHelper.extractHeaderIndexMap(rows.get(0));

        ValidationUtil.validateProductHeaders(headerIndexMap);
        ValidationUtil.validateRowLimit(rows);
        ValidationUtil.segregateValidInvalidProducts(rows, headerIndexMap, validProducts, invalidProducts);

        Map<String, ClientPojo> clientNamesToPojos = getValidClients(validProducts);
        Map<String, ProductPojo> barcodesToPojos = getValidBarcodes(validProducts);

        List<ProductPojo> finalValidProducts = ValidationUtil.getFinalValidProducts(validProducts, invalidProducts, clientNamesToPojos, barcodesToPojos);

        productFlow.addProductsBulk(finalValidProducts);
        return FileUtils.convertProductResultsToBase64(invalidProducts);
    }

    public Map<String, ClientPojo> getValidClients(List<ProductPojo> validProducts) {

        if (validProducts.isEmpty()) return Map.of();

        List<String> clientNames = validProducts.stream()
                .map(ProductPojo::getClientName)
                .toList();

        return productFlow.fetchExistingClientNames(clientNames);
    }

    public Map<String, ProductPojo> getValidBarcodes(List<ProductPojo> validProducts) {
        List<String> barcodes = validProducts.stream()
                .map(ProductPojo::getBarcode)
                .toList();

        List<ProductPojo> existingPojos = productApi.findExistingProducts(barcodes);

        return existingPojos.stream()
                .collect(Collectors.toMap(
                        ProductPojo::getBarcode,
                        Function.identity()
                ));
    }

    public Page<ProductData> searchProducts(ProductSearchForm searchForm) throws ApiException {
        NormalizationUtil.normalizeSearchProductForm(searchForm);
        FormValidator.validate(searchForm);
        Page<ProductPojo> productPage = productFlow.searchProducts(searchForm.getType(), searchForm.getQuery(), searchForm.getPage(), searchForm.getSize());
        Map<String, InventoryPojo> productIdToInventoryPojo = productFlow.getInventoryForProducts(productPage);

        return productPage.map(
                product -> ProductHelper.convertToData(product, productIdToInventoryPojo)
        );
    }
}
