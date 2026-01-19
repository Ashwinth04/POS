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

    public FileData createProducts(FileForm base64file) throws ApiException {

        List<ProductForm> forms = parseBase64File(base64file.getBase64file());
        List<ProductUploadResult> results = upload(forms);
        return convertResultsToBase64(results);
    }

    public FileData addProductsInventory(FileForm base64file) throws ApiException {

        List<InventoryUpdateForm> inventoryForms = getInventoryFormsFromFile(base64file.getBase64file());
        System.out.println("Inventory forms: " + inventoryForms);
        List<InventoryUpdateResult> inventoryResults = uploadInventory(inventoryForms);
//        return convertInventoryResults(inventoryResults);
        return null;
    }

    public FileData convertResultsToBase64(List<ProductUploadResult> results) throws ApiException {
        String resultFile = getBase64String(results);

        FileData fileData = new FileData();
        fileData.setBase64file(resultFile);

        return fileData;
    }

    public List<ProductUploadResult> upload(List<ProductForm> forms) throws ApiException {

        List<ProductUploadResult> results = new ArrayList<>();
        List<ProductPojo> validForms = new ArrayList<>();

        for (ProductForm form : forms) {
            ProductUploadResult result = createInitialResult(form);

            try {
                ProductPojo pojo = validateAndConvert(form);
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

    public List<InventoryUpdateResult> uploadInventory(List<InventoryUpdateForm> inventoryForms) throws ApiException {

        List<InventoryUpdateResult> results = new ArrayList<>();
        List<InventoryPojo> validForms = new ArrayList<>();

        for (InventoryUpdateForm form : inventoryForms) {

            InventoryUpdateResult result = new InventoryUpdateResult();
            result.setBarcode(form.getBarcode());
            result.setQuantity(form.getQuantity());

            try {
                InventoryPojo pojo = new InventoryPojo();

                pojo.setBarcode(form.getBarcode());
                pojo.setQuantity(form.getQuantity());

                validForms.add(pojo);
                result.setStatus("PENDING");
            } catch (Exception e) {
                e.printStackTrace();
                result.setStatus("FAILED");
                result.setMessage(e.getMessage());
            }

            results.add(result);
        }

        return getFinalResultsForInventoryUpdate(results, validForms);

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

    private ProductPojo validateAndConvert(ProductForm form) throws ApiException {
        ValidationUtil.validateProductForm(form);
        return ProductHelper.convertToEntity(form);
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

    private List<InventoryUpdateResult> getFinalResultsForInventoryUpdate(List<InventoryUpdateResult> results, List<InventoryPojo> validForms) {

        if (!validForms.isEmpty()) {

            //Get API layer's response
            //API layer returns a map where the key represents the status and the value represents the object
            Map<String, InventoryUpdateResult> apiResults = productApi.bulkInventoryUpdate(validForms);

            // Update all the results
            for (InventoryUpdateResult r : results) {
                if ("PENDING".equals(r.getStatus())) {
                    InventoryUpdateResult apiResult = apiResults.get(r.getBarcode());
                    r.setStatus(apiResult.getStatus());
                    r.setMessage(apiResult.getMessage());
                    r.setBarcode(apiResult.getBarcode());
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

    public InventoryData updateInventory(String barcode, InventoryForm inventoryForm) throws ApiException {
        ValidationUtil.validateInventoryForm(inventoryForm);
        InventoryPojo inventoryPojo = ProductHelper.convertToInventoryEntity(barcode, inventoryForm);
        InventoryPojo updatedInventoryPojo = productApi.updateInventory(inventoryPojo);
        return ProductHelper.convertToInventoryDto(updatedInventoryPojo);
    }

    public Page<ProductData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> productPage = productApi.getAll(form.getPage(), form.getSize());
        return productPage.map(ProductHelper::convertToDto);
    }
}
