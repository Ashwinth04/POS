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
import com.increff.pos.util.TsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.increff.pos.helper.ProductHelper.toProductPojo;
import static com.increff.pos.util.FileUtils.*;

@Service
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ClientApiImpl clientApi;

    @Autowired
    private ProductApiImpl productApi;

    public ProductData createProduct(ProductForm productForm) throws ApiException {

        clientApi.getCheckByClientName(productForm.getClientName());
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo savedProductPojo = productFlow.addProduct(productPojo);

        return ProductHelper.convertToDto(savedProductPojo);
    }

    public ProductData editProduct(ProductForm productForm) throws ApiException {

        clientApi.getCheckByClientName(productForm.getClientName());
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo editedPojo = productFlow.editProduct(productPojo);

        return ProductHelper.convertToDto(editedPojo);
    }

    public Page<ProductData> getAllProducts(PageForm form) throws ApiException {
        Page<ProductPojo> productPage = productFlow.getAllProducts(form.getPage(), form.getSize());
        Map<String, InventoryPojo> productIdToInventoryPojo = productFlow.getInventoryForProducts(productPage);

        return productPage.map(
                product -> ProductHelper.convertToDto(product, productIdToInventoryPojo)
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

        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        List<ProductPojo> validProducts = new ArrayList<>();
        List<RowError> invalidProducts = new ArrayList<>();

        Map<String, Integer> headerIndexMap = extractHeaderIndexMap(rows.get(0));
        ProductHelper.validateHeaders(headerIndexMap);

        if (rows.size() > 5000) throw new ApiException("Maximum row limit exceeded!");

        for (int i = 1; i < rows.size(); i++) {
            try {
                ProductPojo pojo = toProductPojo(rows.get(i), headerIndexMap);
                validProducts.add(pojo);
            } catch (Exception e) {
                invalidProducts.add(new RowError(rows.get(i)[0], e.getMessage()));
            }
        }

        Set<String> validClientSet = getValidClients(validProducts);
        Set<String> existingBarcodeSet = getValidBarcodes(validProducts);

        List<ProductPojo> finalValidProducts = getFinalValidProducts(validProducts, invalidProducts, validClientSet, existingBarcodeSet);

         productFlow.addProductsBulk(finalValidProducts);

        return convertProductResultsToBase64(invalidProducts);
    }

    public List<ProductPojo> getFinalValidProducts(List<ProductPojo> validProducts, List<RowError> invalidProducts, Set<String> validClientSet, Set<String> existingBarcodeSet) {

        List<ProductPojo> finalValidProducts = new ArrayList<>();

        for (int i = 0; i < validProducts.size(); i++) {
            ProductPojo product = validProducts.get(i);

            String clientName = product.getClientName();
            String barcode = product.getBarcode();

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
}
