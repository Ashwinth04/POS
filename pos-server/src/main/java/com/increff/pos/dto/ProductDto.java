package com.increff.pos.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.FileUtils;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.increff.pos.util.FileUtils.getBase64String;
import static com.increff.pos.util.FileUtils.parseBase64File;

@Service
public class ProductDto {

    @Autowired
    private ProductApiImpl productApi;

    public FileData createProducts(FileForm base64file) throws ApiException {

        List<ProductForm> forms = parseBase64File(base64file.getBase64file());
        List<ProductUploadResult> results = upload(forms);
        return convertResultsToBase64(results);
    }

    public FileData convertResultsToBase64(List<ProductUploadResult> results) throws ApiException {
        String resultFile = getBase64String(results);

        FileData fileData = new FileData();
        fileData.setBase64file(resultFile);

        return fileData;
    }

    public List<ProductUploadResult> upload(List<ProductForm> forms) throws ApiException {

        List<ProductUploadResult> results = new ArrayList<>(); //Contains the final list of responses for each product
        List<ProductPojo> validForms = new ArrayList<>(); //Contains all the valid products which are ready to be sent to the API layer

        // Iterate through all the forms and validate them. Only send the valid ones to the API layer
        for (ProductForm form : forms) {

            ProductUploadResult result = new ProductUploadResult();
            result.setBarcode(form.getBarcode());
            result.setClientId(form.getClientId());
            result.setName(form.getName());
            result.setMrp(form.getMrp());
            result.setImageUrl(form.getImageUrl());


            // Statuses are required to track whether a product createProductsBulk is success or fail. This will be later used in the output TSV file which will be sent to the frontend
            try {
                ValidationUtil.validateProductForm(form);
                validForms.add(ProductHelper.convertToEntity(form));
                result.setStatus("PENDING");
            } catch (ApiException e) {
                // If validation fails, mark it as failed. Don't send it to the API layer
                result.setStatus("FAILED");
                result.setMessage(e.getMessage());
            }

            results.add(result);
        }

        return getFinalResults(results, validForms);
    }

    private List<ProductUploadResult> getFinalResults(List<ProductUploadResult> results, List<ProductPojo> validForms) {

        if (!validForms.isEmpty()) {

            //Get API layer's response
            //API layer returns a map where the key represents the status and the value represents the object
            Map<String, ProductUploadResult> apiResults = productApi.bulkAdd(validForms);

            // Update all the results
            for (ProductUploadResult r : results) {
                if ("PENDING".equals(r.getStatus())) {
                    ProductUploadResult apiResult = apiResults.get(r.getBarcode());
                    r.setStatus(apiResult.getStatus());
                    r.setMessage(apiResult.getMessage());
                    r.setProductId(apiResult.getProductId());
                }
            }
        }

        return results;
    }

    public ProductData create(ProductForm productForm) throws ApiException {
        ValidationUtil.validateProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo savedProductPojo = productApi.add(productPojo);

        productApi.addInventory(productPojo);
        return ProductHelper.convertToDto(savedProductPojo);
    }

    public InventoryData updateInventory(String productId, InventoryForm inventoryForm) throws ApiException {
        ValidationUtil.validateInventoryForm(inventoryForm);
        InventoryPojo inventoryPojo = ProductHelper.convertToInventoryEntity(productId, inventoryForm);
        InventoryPojo updatedInventoryPojo = productApi.updateInventory(inventoryPojo);
        return ProductHelper.convertToInventoryDto(updatedInventoryPojo);
    }

    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> productPage = productApi.getAll(form.getPage(), form.getSize());
        return productPage.map(ProductHelper::convertToDto);
    }
}
