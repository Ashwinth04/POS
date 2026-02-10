package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.documents.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.constants.ProductSearchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        existingRecord.setClientName(productPojo.getClientName());
        existingRecord.setName(productPojo.getName());
        existingRecord.setMrp(productPojo.getMrp());
        existingRecord.setImageUrl(productPojo.getImageUrl());
        return productDao.save(productPojo);
    }

    @Transactional(readOnly = true)
    public Page<ProductPojo> getAllProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return productDao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<ProductPojo> addProductsBulk(List<ProductPojo> pojos) {
        return productDao.saveAll(pojos);
    }

    @Transactional(readOnly = true)
    public void checkBarcodeExists(String barcode) throws ApiException {
        ProductPojo result = productDao.findByBarcode(barcode);
        if (Objects.nonNull(result)) { throw new ApiException("Barcode already exists"); }
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> findExistingProducts(List<String> barcodes) {
        return productDao.findByBarcodes(barcodes);
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> getProductPojosForBarcodes(List<String> barcodes) {

        if (Objects.isNull(barcodes) || barcodes.isEmpty()) {
            return Collections.emptyList();
        }
        return productDao.findByBarcodes(barcodes);
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> getProductPojosForProductIds(List<String> productIds) {

        if (Objects.isNull(productIds) || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productDao.findAllById(productIds);
    }

    @Transactional(readOnly = true)
    public ProductPojo getCheckByBarcode(String barcode) throws ApiException {

        ProductPojo record = productDao.findByBarcode(barcode);

        if (Objects.isNull(record)) {
            throw new ApiException("Product with this given barcode doesn't exist");
        }
        return record;
    }

    @Transactional(readOnly = true)
    public Page<ProductPojo> searchProducts(ProductSearchType type, String query, int page, int size) throws ApiException {
        Pageable pageable = PageRequest.of(page, size);

        return switch (type) {
            case BARCODE ->
                    productDao.searchByBarcode(query, pageable);

            case NAME ->
                    productDao.searchByName(query, pageable);
        };
    }
}