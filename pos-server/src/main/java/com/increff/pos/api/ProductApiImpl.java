package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductApiImpl implements ProductApi {

    @Autowired
    private ProductDao productDao;

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {

        checkBarcodeExists(productPojo.getBarcode());

        return productDao.save(productPojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo editProduct(ProductPojo productPojo) throws ApiException {

        ProductPojo existingRecord = getCheckByBarcode(productPojo.getBarcode());

        productPojo.setId(existingRecord.getId());

        return productDao.save(productPojo);
    }

    public Page<ProductPojo> getAllProducts(int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return productDao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<ProductPojo> addProductsBulk(List<ProductPojo> pojos) throws ApiException {

        try {
            return productDao.saveAll(pojos);
        } catch (Exception e) {
            throw new ApiException("Failed to insert valid products");
        }
    }

    public void checkBarcodeExists(String barcode) throws ApiException {

        ProductPojo result = productDao.findByBarcode(barcode);

        if (Objects.nonNull(result)) { throw new ApiException("Barcode already exists"); }
    }

    public Map<String, ProductPojo> findExistingProducts(List<String> barcodes) {

        List<ProductPojo> existingBarcodes = productDao.findByBarcodes(barcodes);

        return existingBarcodes.stream()
                .collect(Collectors.toMap(
                        ProductPojo::getBarcode,
                        Function.identity()
                ));
    }

    public Map<String, ProductPojo> mapBarcodesToProductPojos(List<String> barcodes) {

        if (barcodes == null || barcodes.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductPojo> products = productDao.findByBarcodes(barcodes);

        Map<String, ProductPojo> barcodeToProductId = new HashMap<>();

        for (ProductPojo product : products) {
            barcodeToProductId.put(
                    product.getBarcode(),
                    product
            );
        }

        return barcodeToProductId;
    }

    public Map<String, ProductPojo> mapProductIdsToProductPojos(List<String> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductPojo> products = productDao.findAllById(productIds);

        Map<String, ProductPojo> productIdToProductPojo = new HashMap<>();

        for (ProductPojo product : products) {
            productIdToProductPojo.put(
                    product.getId(),
                    product
            );
        }

        return productIdToProductPojo;

    }

    public ProductPojo getCheckByBarcode(String barcode) throws ApiException {

        ProductPojo record = productDao.findByBarcode(barcode);

        if (Objects.isNull(record)) {
            throw new ApiException("Product with this given barcode doesn't exist");
        }

        return record;
    }

    public Page<ProductPojo> searchProducts(String type, String query, int page, int size) throws ApiException {
        Pageable pageable = PageRequest.of(page, size);

        return switch (type.toLowerCase()) {
            case "barcode" ->
                    productDao.searchByBarcode(query, pageable);

            case "name" ->
                    productDao.searchByName(query, pageable);

            default ->
                    throw new ApiException("Invalid search type: " + type);
        };
    }

}
