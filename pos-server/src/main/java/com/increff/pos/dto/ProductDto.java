package com.increff.pos.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.util.FormValidator;
import com.increff.pos.util.NormalizationUtil;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
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

        FormValidator.validate(productForm);
        NormalizationUtil.normalizeProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        productFlow.getCheckByClientName(productPojo.getClientName());
        ProductPojo savedProductPojo = productFlow.addProduct(productPojo);

        return ProductHelper.convertToData(savedProductPojo);
    }

    public ProductData editProduct(ProductForm productForm) throws ApiException {

        FormValidator.validate(productForm);
        NormalizationUtil.normalizeProductForm(productForm);
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

    public static Map<String, Integer> extractHeaderIndexMap(String[] headerRow) {
        Map<String, Integer> headerIndexMap = new HashMap<>();

        for (int i = 0; i < headerRow.length; i++) {
            headerIndexMap.put(headerRow[i], i);
        }

        return headerIndexMap;
    }

    public FileData createProducts(FileForm fileForm) throws ApiException {

        FormValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        List<ProductPojo> validProducts = new ArrayList<>();
        List<RowError> invalidProducts = new ArrayList<>();

        Map<String, Integer> headerIndexMap = extractHeaderIndexMap(rows.get(0));

        ValidationUtil.validateProductHeaders(headerIndexMap);
        ValidationUtil.validateRowLimit(rows);

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (ValidationUtil.isRowEmpty(row)) continue;

            try {
                ProductPojo pojo = convertRowToProductPojo(row, headerIndexMap);
                validProducts.add(pojo);
            } catch (Exception e) {
                String barcode = getValueFromRow(row, headerIndexMap, "barcode");
                String errorMessage = (e.getMessage() != null) ? e.getMessage() : "Unknown error";
                invalidProducts.add(new RowError(barcode, errorMessage));
            }
        }

        Map<String, ClientPojo> clientNamesToPojos = getValidClients(validProducts);
        Map<String, ProductPojo> barcodesToPojos = getValidBarcodes(validProducts);

        List<ProductPojo> finalValidProducts = ValidationUtil.getFinalValidProducts(validProducts, invalidProducts, clientNamesToPojos, barcodesToPojos);

        productFlow.addProductsBulk(finalValidProducts);

        return convertProductResultsToBase64(invalidProducts);
    }

    public Map<String, ClientPojo> getValidClients(List<ProductPojo> validProducts) {

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

    public FileData convertProductResultsToBase64(List<RowError> results) {

        String resultFile = generateProductUploadResults(results);

        FileData fileData = new FileData();
        fileData.setStatus(results.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");
        fileData.setBase64file(resultFile);

        return fileData;
    }

    public Page<ProductData> searchProducts(ProductSearchForm searchForm) throws ApiException {

        FormValidator.validate(searchForm);
        NormalizationUtil.normalizeSearchProductForm(searchForm);
        Page<ProductPojo> productPage = productFlow.searchProducts(searchForm.getType(), searchForm.getQuery(), searchForm.getPage(), searchForm.getSize());
        Map<String, InventoryPojo> productIdToInventoryPojo = productFlow.getInventoryForProducts(productPage);

        return productPage.map(
                product -> ProductHelper.convertToData(product, productIdToInventoryPojo)
        );

    }
}
