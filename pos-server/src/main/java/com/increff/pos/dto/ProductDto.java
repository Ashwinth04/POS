package com.increff.pos.dto;

import com.increff.pos.api.InventoryApiImpl;
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

    private final ProductApiImpl productApi;

    private final InventoryApiImpl inventoryApi;

    public ProductDto(ProductApiImpl productApi, InventoryApiImpl inventoryApi) {
        this.productApi = productApi;
        this.inventoryApi = inventoryApi;
    }

    public ProductData createProduct(ProductForm productForm) throws ApiException {
        ValidationUtil.validateProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo savedProductPojo = productApi.addProduct(productPojo);

        return ProductHelper.convertToDto(savedProductPojo);
    }

    public Page<ProductData> getAllProducts(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> productPage = productApi.getAllProducts(form.getPage(), form.getSize());
        return productPage.map(ProductHelper::convertToDto);
    }

    public FileData createProducts(FileForm base64file) throws ApiException {

        List<ProductForm> forms = readProductFormsFromBase64(base64file.getBase64file());
        List<ProductUploadResult> results = uploadProducts(forms);
        return convertProductResultsToBase64(results);
    }

    public List<ProductUploadResult> uploadProducts(List<ProductForm> forms) {

        Map<String, Integer> barcodeCount = countBarcodes(forms);

        List<ProductUploadResult> results = new ArrayList<>();
        List<ProductPojo> validForms = new ArrayList<>();

        for (ProductForm form : forms) {
            ProductUploadResult result = processSingleForm(form, barcodeCount, validForms);
            results.add(result);
        }

        return getProductUploadResults(results, validForms);
    }

    private Map<String, Integer> countBarcodes(List<ProductForm> forms) {
        Map<String, Integer> barcodeCount = new HashMap<>();

        for (ProductForm form : forms) {
            barcodeCount.merge(form.getBarcode().toLowerCase(), 1, Integer::sum);
        }

        return barcodeCount;
    }

    private ProductUploadResult processSingleForm(ProductForm form, Map<String, Integer> barcodeCount, List<ProductPojo> validForms) {

        ProductUploadResult result = createInitialResult(form);

        try {
            ValidationUtil.validateProductForm(form);
            ProductPojo pojo = ProductHelper.convertToEntity(form);

            if (barcodeCount.get(pojo.getBarcode().toLowerCase()) > 1) {
                markDuplicate(result);
            } else {
                markValid(result);
                validForms.add(pojo);
            }

        } catch (ApiException e) {
            markFailed(result, e.getMessage());
        }

        return result;
    }

    private void markDuplicate(ProductUploadResult result) {
        result.setStatus("FAILED");
        result.setMessage("Duplicate barcode in file");
    }

    private void markValid(ProductUploadResult result) {
        result.setStatus("PENDING");
    }

    private void markFailed(ProductUploadResult result, String message) {
        result.setStatus("FAILED");
        result.setMessage(message);
    }


    private ProductUploadResult createInitialResult(ProductForm form) {
        ProductUploadResult result = new ProductUploadResult();
        result.setBarcode(form.getBarcode().toLowerCase());
        result.setClientName(form.getClientName());
        result.setName(form.getName());
        result.setMrp(form.getMrp());
        result.setImageUrl(form.getImageUrl());
        return result;
    }

    private List<ProductUploadResult> getProductUploadResults(List<ProductUploadResult> results, List<ProductPojo> validPojos) {

        if (validPojos.isEmpty()) return results;

        Map<String, ProductUploadResult> apiResults = productApi.addProductsBulk(validPojos);

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

    public FileData convertProductResultsToBase64(List<ProductUploadResult> results) {

        String resultFile = getBase64String(results);

        FileData fileData = new FileData();
        fileData.setBase64file(resultFile);

        return fileData;
    }


}
