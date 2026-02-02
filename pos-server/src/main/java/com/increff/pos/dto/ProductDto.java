package com.increff.pos.dto;

import com.increff.pos.api.ClientApiImpl;
import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
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
import java.util.stream.Collectors;

import static com.increff.pos.helper.ProductHelper.convertRowToProductPojo;
import static com.increff.pos.util.FileUtils.*;

@Service
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ClientApiImpl clientApi;

    @Autowired
    private ProductApiImpl productApi;

    @Autowired
    private FormValidator formValidator;

    public ProductData createProduct(ProductForm productForm) throws ApiException {

        formValidator.validate(productForm);
        NormalizationUtil.normalizeProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        clientApi.getCheckByClientName(productPojo.getClientName());
        ProductPojo savedProductPojo = productFlow.addProduct(productPojo);

        return ProductHelper.convertToData(savedProductPojo);
    }

    public ProductData editProduct(ProductForm productForm) throws ApiException {

        formValidator.validate(productForm);
        NormalizationUtil.normalizeProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        clientApi.getCheckByClientName(productPojo.getClientName());
        ProductPojo editedPojo = productFlow.editProduct(productPojo);

        return ProductHelper.convertToData(editedPojo);
    }

    public Page<ProductData> getAllProducts(PageForm form) throws ApiException {

        formValidator.validate(form);
        Page<ProductPojo> productPage = productFlow.getAllProducts(form.getPage(), form.getSize());
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

        formValidator.validate(fileForm);
        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        List<ProductPojo> validProducts = new ArrayList<>();
        List<RowError> invalidProducts = new ArrayList<>();

        Map<String, Integer> headerIndexMap = extractHeaderIndexMap(rows.get(0));
        ValidationUtil.validateProductHeaders(headerIndexMap);

        if (rows.size() > 5000) throw new ApiException("Maximum row limit exceeded!");

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String firstColumn = null;

            if (ValidationUtil.isRowEmpty(row)) continue;

            if (row != null && row.length > 0) {
                firstColumn = row[0].toLowerCase();
            }

            try {

                ProductPojo pojo = convertRowToProductPojo(row, headerIndexMap);
                validProducts.add(pojo);
            } catch (Exception e) {
                String errorMessage = (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error";
                invalidProducts.add(new RowError(firstColumn, errorMessage));
            }
        }

        Set<String> validClientSet = getValidClients(validProducts);
        Set<String> existingBarcodeSet = getValidBarcodes(validProducts);

        List<ProductPojo> finalValidProducts = getFinalValidProducts(validProducts, invalidProducts, validClientSet, existingBarcodeSet);

         productFlow.addProductsBulk(finalValidProducts);

        return convertProductResultsToBase64(invalidProducts);
    }

    public List<ProductPojo> getFinalValidProducts(List<ProductPojo> validProducts, List<RowError> invalidProducts, Set<String> validClientSet, Set<String> existingBarcodeSet) {

        Map<String, Long> barcodeCountMap = validProducts.stream()
                .map(ProductPojo::getBarcode)
                .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        List<ProductPojo> finalValidProducts = new ArrayList<>();

        for (int i = 0; i < validProducts.size(); i++) {
            ProductPojo product = validProducts.get(i);

            String clientName = product.getClientName();
            String barcode = product.getBarcode();

            if (barcodeCountMap.get(barcode) > 1) {
                invalidProducts.add(
                        new RowError(barcode, "Duplicate barcode found in upload: " + barcode)
                );
                continue;
            }

            if (!validClientSet.contains(clientName)) {
                invalidProducts.add(
                        new RowError(barcode, "Client does not exist: " + clientName)
                );
                continue;
            }

            if (existingBarcodeSet.contains(barcode)) {
                invalidProducts.add(
                        new RowError(barcode, "Product with barcode already exists: " + barcode)
                );
                continue;
            }

            finalValidProducts.add(product);
        }

        return finalValidProducts;
    }

    public Set<String> getValidClients(List<ProductPojo> validProducts) {

        List<String> clientNames = validProducts.stream()
                .map(ProductPojo::getClientName)
                .toList();

        List<String> validClients = clientApi.fetchExistingClientNames(clientNames);

        return new HashSet<>(validClients);
    }

    public Set<String> getValidBarcodes(List<ProductPojo> validProducts) {

        List<String> barcodes = validProducts.stream()
                .map(ProductPojo::getBarcode)
                .toList();

        List<String> validBarcodes = productApi.findExistingProducts(barcodes);

        return new HashSet<>(validBarcodes);
    }

    public FileData convertProductResultsToBase64(List<RowError> results) {

        String resultFile = generateProductUploadResults(results);

        FileData fileData = new FileData();
        fileData.setStatus(results.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");
        fileData.setBase64file(resultFile);

        return fileData;
    }

    public Page<ProductData> search(String type, String query, PageForm form) throws ApiException {

        formValidator.validate(form);
        Page<ProductPojo> productPage = productFlow.searchProducts(type, query, form.getPage(), form.getSize());
        Map<String, InventoryPojo> productIdToInventoryPojo = productFlow.getInventoryForProducts(productPage);

        return productPage.map(
                product -> ProductHelper.convertToData(product, productIdToInventoryPojo)
        );

    }
}
