package com.increff.pos.dto;

import com.increff.pos.api.ProductApiImpl;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.ClientHelper;
import com.increff.pos.helper.ProductHelper;
import com.increff.pos.helper.UserHelper;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ProductDto {

    @Autowired
    private ProductApiImpl productApi;

    public List<ProductForm> parseTsv(MultipartFile file) {

        //Parse the tsv and convert it a list of ProductForm objects
        List<ProductForm> forms = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean isHeader = true; //first line in the file is a header

            //iterate through all the lines
            //Every line corresponds to a product except the first line
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // skip header
                    continue;
                }

                //trim removes whitespaces at the beginning and the end
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split("\t", -1); // -1 keeps empty columns

                ProductForm form = new ProductForm();
                form.setBarcode(columns[0].trim());
                form.setClientId(columns[1].trim());
                form.setName(columns[2].trim());
                form.setMrp(columns[3].isBlank() ? null : Double.valueOf(columns[3].trim()));
                form.setImageUrl(columns[4].trim());

                forms.add(form);
            }

        } catch (IOException e) {
            System.out.println("Failed to parse TSV file");
            throw new RuntimeException("Failed to read TSV file", e);
        }

        return forms;
    }

    public byte[] convertResultsToTsv(List<ProductUploadResult> results) {

        //Products are added incrementally, so StringBuilder is necessary
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("barcode\tclientId\tname\tmrp\timageUrl\tproductId\tstatus\tmessage\n");

        // Iterate through the products and create the tsv file incrementally
        for (ProductUploadResult r : results) {
            sb.append(safe(r.getBarcode())).append("\t")
                    .append(safe(r.getClientId())).append("\t")
                    .append(safe(r.getName())).append("\t")
                    .append(r.getMrp() == null ? "" : r.getMrp()).append("\t")
                    .append(safe(r.getImageUrl())).append("\t")
                    .append(safe(r.getProductId())).append("\t")
                    .append(safe(r.getStatus())).append("\t")
                    .append(safe(r.getMessage()))
                    .append("\n");
        }

        // Convert it to a string and then to a byte array. This makes it suitable for sending a file to the frontend
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\n", " ");
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


            // Statuses are required to track whether a product upload is success or fail. This will be later used in the output TSV file which will be sent to the frontend
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

        if (!validForms.isEmpty()) {

            //Get API layer's response
            //API layer returns a map where the key represents the status and the value represents the object
            Map<String, ProductUploadResult> apiResults =
                    productApi.bulkAdd(validForms);

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
