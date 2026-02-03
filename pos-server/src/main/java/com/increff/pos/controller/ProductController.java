package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.form.FileForm;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.model.form.ProductForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
    public ProductData createProduct(@RequestBody ProductForm productForm) throws ApiException {
        return productDto.createProduct(productForm);
    }

    @Operation(summary = "Edit product details")
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public ProductData editProductDetails(@RequestBody ProductForm productForm) throws ApiException {
        return productDto.editProduct(productForm);
    }

    @Operation(summary = "Upload a list of products")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public FileData createProductsBulk(@RequestBody FileForm fileForm) throws ApiException { // Use base64 string
        return productDto.createProducts(fileForm);
    }

    @Operation(summary = "Get all products with pagination")
    @RequestMapping(path = "/get-all-paginated", method = RequestMethod.POST)
    public Page<ProductData> getAllProducts(@RequestBody PageForm form) throws ApiException {
        return productDto.getAllProducts(form);
    }

    @RequestMapping(path = "/search", method = RequestMethod.POST)
    public Page<ProductData> searchProducts(@RequestParam String type, @RequestParam String query, @RequestBody PageForm pageForm) throws ApiException{
        return productDto.searchProducts(type, query, pageForm);
    }
}