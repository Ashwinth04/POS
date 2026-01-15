package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.UserForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Product Management", description = "Perform CRUD operations on Products")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @Operation(summary = "Create a new product")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public ProductData create(@RequestBody ProductForm productForm) throws ApiException {
        return productDto.create(productForm);
    }

    @Operation(summary = "Upload a list of products")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public byte[] upload(@RequestPart("file") MultipartFile file)throws ApiException { // USe base64 string
//        System.out.println("Request received");
        List<ProductForm> forms = productDto.parseTsv(file);
//        System.out.println("TSV file parsed");
        List<ProductUploadResult> results = productDto.upload(forms); // make a single call to Dto
//        System.out.println("TSV file uploaded");
        return productDto.convertResultsToTsv(results);
    }

    @Operation(summary = "Update inventory for a product")
    @RequestMapping(path = "/update/{productId}", method = RequestMethod.PUT)
    public InventoryData updateInventory(@PathVariable String productId, @RequestBody InventoryForm inventoryForm) throws ApiException {
        return productDto.updateInventory(productId, inventoryForm);
    }

    @Operation(summary = "Update inventory for multiple products at once")
    @RequestMapping(path = "/bulkUpdate", method = RequestMethod.POST)
    public byte[] uploadBulkInventory(@RequestPart("file") MultipartFile file) throws ApiException {
        return null;
    }

    @Operation(summary = "Get all products with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<ProductData> getAll(@RequestBody PageForm form) throws ApiException {
        return productDto.getAll(form);
    }
}
