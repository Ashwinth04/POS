package com.increff.pos.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.util.TsvParser;
import com.increff.pos.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.increff.pos.util.FileUtils.*;
import static com.increff.pos.util.ValidationUtil.validateProductRows;

@Service
public class ProductDto {

    private final ProductFlow productFlow;

    public ProductDto(ProductFlow productFlow) {
        this.productFlow = productFlow;
    }

    public ProductData createProduct(ProductForm productForm) throws ApiException {
        ValidationUtil.validateProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo savedProductPojo = productFlow.addProduct(productPojo);

        return ProductHelper.convertToDto(savedProductPojo);
    }

    public ProductData editProduct(ProductForm productForm) throws ApiException {
        System.out.println("Inside DTO");
        ValidationUtil.validateProductForm(productForm);
        ProductPojo productPojo = ProductHelper.convertToEntity(productForm);
        ProductPojo editedPojo = productFlow.editProduct(productPojo);

        return ProductHelper.convertToDto(editedPojo);
    }

    public Page<ProductData> getAllProducts(PageForm form) throws ApiException {
        ValidationUtil.validatePageForm(form);
        Page<ProductPojo> productPage = productFlow.getAllProducts(form.getPage(), form.getSize());
        return productPage.map(ProductHelper::convertToDto);
    }

    public FileData createProducts(FileForm fileForm) throws ApiException {

        List<String[]> rows = TsvParser.parseBase64Tsv(fileForm.getBase64file());

        validateProductRows(rows);

        List<ProductUploadResult> results = uploadProducts(rows);
        return convertProductResultsToBase64(results);
    }

    public List<ProductUploadResult> uploadProducts(List<String[]> rows) throws ApiException {

        Map<String, Integer> barcodeCount = countBarcodes(rows);

        System.out.println("ROWSSS" + rows);

        List<ProductUploadResult> results = new ArrayList<>();
        List<ProductPojo> validForms = new ArrayList<>();

        for (String[] row : rows) {
            ProductUploadResult result = processSingleForm(row, barcodeCount, validForms);
            results.add(result);
        }

        return getProductUploadResults(results, validForms);
    }

    private Map<String, Integer> countBarcodes(List<String[]> rows) {
        Map<String, Integer> barcodeCount = new HashMap<>();

        for (String[] row : rows) {
            barcodeCount.merge(row[0].toLowerCase(), 1, Integer::sum);
        }

        return barcodeCount;
    }

    private ProductUploadResult processSingleForm(String[] row, Map<String, Integer> barcodeCount, List<ProductPojo> validForms) throws ApiException {

        ProductUploadResult result = createInitialResult(row);

        System.out.println("ROWS: ");

        try {
            ValidationUtil.validateProductRow(row);
            ProductPojo pojo = ProductHelper.convertRowToEntity(row);

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

    private ProductUploadResult createInitialResult(String[] row) throws ApiException{

        try
        {
            ProductUploadResult result = new ProductUploadResult();
            result.setBarcode(row[0].toLowerCase());
            result.setClientName(row[1]);
            result.setName(row[2]);
            result.setMrp(Double.parseDouble(row[3]));
            result.setImageUrl(row[4]);
            return result;
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid value for MRP");
        }

    }

    private List<ProductUploadResult> getProductUploadResults(List<ProductUploadResult> results, List<ProductPojo> validPojos) throws ApiException {

        if (validPojos.isEmpty()) return results;

        Map<String, ProductUploadResult> apiResults = productFlow.addProductsBulk(validPojos);

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

        String resultFile = generateProductUploadResults(results);

        FileData fileData = new FileData();
        fileData.setBase64file(resultFile);

        return fileData;
    }
}
