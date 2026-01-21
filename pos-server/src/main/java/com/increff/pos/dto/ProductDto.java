package com.increff.pos.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.ValidationUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.increff.pos.util.FileUtils.*;

@Service
public class ProductDto {

    @Autowired
    private ProductApiImpl productApi;

    public ProductData createProduct(ProductForm productForm) throws ApiException {
        ValidationUtil.validateProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo savedProductPojo = productApi.add(productPojo);

        productApi.addInventory(productPojo);
        return ProductHelper.convertToDto(savedProductPojo);
    }

    public InventoryData updateInventory(String barcode, InventoryForm inventoryForm) throws ApiException {
        ValidationUtil.validateInventoryForm(inventoryForm);
        InventoryPojo inventoryPojo = ProductHelper.convertToInventoryEntity(barcode, inventoryForm);
        InventoryPojo updatedInventoryPojo = productApi.updateInventory(inventoryPojo);
        return ProductHelper.convertToInventoryDto(updatedInventoryPojo);
    }

    public Page<ProductData> getAllProducts(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> productPage = productApi.getAll(form.getPage(), form.getSize());
        return productPage.map(ProductHelper::convertToDto);
    }

    public FileData createProducts(FileForm base64file) {

        List<ProductForm> forms = parseBase64File(base64file.getBase64file());
        List<ProductUploadResult> results = uploadProducts(forms);
        return convertResultsToBase64(results);
    }

    public List<ProductUploadResult> uploadProducts(List<ProductForm> forms) {

        List<ProductUploadResult> results = new ArrayList<>();
        List<ProductPojo> validForms = new ArrayList<>();

        for (ProductForm form : forms) {
            ProductUploadResult result = createInitialResult(form);

            try {
                ProductPojo pojo = validateAndConvertToProductEntity(form);
                validForms.add(pojo);
                result.setStatus("PENDING");
            } catch (ApiException e) {
                result.setStatus("FAILED");
                result.setMessage(e.getMessage());
            }

            results.add(result);
        }

        return getFinalResults(results, validForms);
    }

    private ProductUploadResult createInitialResult(ProductForm form) {
        ProductUploadResult result = new ProductUploadResult();
        result.setBarcode(form.getBarcode());
        result.setClientId(form.getClientId());
        result.setName(form.getName());
        result.setMrp(form.getMrp());
        result.setImageUrl(form.getImageUrl());
        return result;
    }

    private ProductPojo validateAndConvertToProductEntity(ProductForm form) throws ApiException {
        ValidationUtil.validateProductForm(form);
        return ProductHelper.convertToEntity(form);
    }

    private List<ProductUploadResult> getFinalResults(List<ProductUploadResult> results, List<ProductPojo> validForms) {

        if (validForms.isEmpty()) return results;

        Map<String, ProductUploadResult> apiResults = productApi.bulkAdd(validForms);

        for (ProductUploadResult r : results) {
            if ("PENDING".equals(r.getStatus())) {

                ProductUploadResult apiResult = apiResults.get(r.getBarcode());

                if (apiResult != null) {
                    r.setStatus(apiResult.getStatus());
                    r.setMessage(apiResult.getMessage());
                    r.setProductId(apiResult.getProductId());
                }
            }
        }

        return results;
    }

    public FileData convertResultsToBase64(List<ProductUploadResult> results) {

        String resultFile = getBase64String(results);

        FileData fileData = new FileData();
        fileData.setBase64file(resultFile);

        return fileData;
    }

    public FileData addProductsInventory(FileForm base64file) {

        List<InventoryUpdateForm> inventoryForms =
                getInventoryFormsFromFile(base64file.getBase64file());

        List<InventoryPojo> validForms = new ArrayList<>();
        Map<String, String> invalidForms = new HashMap<>();

        validateAndSplit(inventoryForms, validForms, invalidForms);

        if (!validForms.isEmpty()) {
            Map<String, String> dbInvalid = productApi.bulkInventoryUpdate(validForms);
            invalidForms.putAll(dbInvalid);
        }

        return buildResponse(invalidForms);
    }

    private void validateAndSplit(List<InventoryUpdateForm> forms, List<InventoryPojo> valid, Map<String, String> invalid) {

        for (InventoryUpdateForm form : forms) {
            String error = ValidationUtil.validateInventoryUpdateForm(form);

            if (error == null) {
                InventoryPojo pojo = new InventoryPojo();
                pojo.setBarcode(form.getBarcode());
                pojo.setQuantity(form.getQuantity());
                valid.add(pojo);
            } else {
                invalid.put(form.getBarcode(), error);
            }
        }
    }

    private FileData buildResponse(Map<String, String> invalidForms) {

        FileData fileData = new FileData();
        fileData.setBase64file(FileUtils.getBase64InventoryUpdate(invalidForms));
        fileData.setStatus(invalidForms.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");

        return fileData;
    }

}
