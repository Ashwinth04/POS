package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductApiImpl implements ProductApi {
    private static final Logger logger = LoggerFactory.getLogger(ProductApiImpl.class);

    private final ProductDao productDao;

    public ProductApiImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {
        logger.info("Adding a product with barcode: {}", productPojo.getBarcode());

        checkBarcodeExists(productPojo.getBarcode());

        ProductPojo saved = productDao.save(productPojo);

        logger.info("Product created with barcode: {}", saved.getBarcode());

        return saved;
    }

    public ProductPojo editProduct(ProductPojo productPojo) throws ApiException {

        ProductPojo existingRecord = productDao.findByBarcode(productPojo.getBarcode());

        if (existingRecord == null) {
            throw new ApiException("Product with this given barcode doesn't exist");
        }

        productPojo.setId(existingRecord.getId());

        return productDao.save(productPojo);
    }

    public Page<ProductPojo> getAllProducts(int page, int size) {
        logger.info("Fetching products page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productDao.findAll(pageRequest);
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, ProductUploadResult> addProductsBulk(List<ProductPojo> pojos, List<String> existingClientNames) {

        Map<String, ProductUploadResult> resultMap = initializeResultMap(pojos);

        if (pojos == null || pojos.isEmpty()) {
            return resultMap;
        }

        List<ProductPojo> validForInsert = filterValidClients(pojos, existingClientNames, resultMap); // no change needed

        persistValidProducts(validForInsert, resultMap);

        return resultMap;
    }

    private Map<String, ProductUploadResult> initializeResultMap(List<ProductPojo> pojos) {

        Map<String, ProductUploadResult> resultMap = new HashMap<>();

        if (pojos == null) return resultMap;

        for (ProductPojo p : pojos) {


            ProductUploadResult r = new ProductUploadResult();
            r.setBarcode(p.getBarcode());
            r.setClientName(p.getClientName());
            r.setName(p.getName());
            r.setMrp(p.getMrp());
            r.setImageUrl(p.getImageUrl());
            r.setStatus("PENDING");

            resultMap.put(p.getBarcode(), r);
        }

        return resultMap;
    }

    private List<ProductPojo> filterValidClients(List<ProductPojo> pojos, List<String> existingClientNames, Map<String, ProductUploadResult> resultMap) {

        List<ProductPojo> valid = new ArrayList<>();

        for (ProductPojo p : pojos) {
            ProductUploadResult r = resultMap.get(p.getBarcode());

            String barcode = p.getBarcode();
            ProductPojo existing = productDao.findByBarcode(barcode);

            if (!existingClientNames.contains(p.getClientName())) {
                r.setStatus("FAILED");
                r.setMessage("Client with the given name does not exist");
            } else if (existing != null) {
                r.setStatus("FAILED");
                r.setMessage("Product with the given barcode already exists");
            }
            else {
                valid.add(p);
            }
        }

        return valid;
    }

    private void persistValidProducts(List<ProductPojo> validForInsert, Map<String, ProductUploadResult> resultMap) {

        try {
            List<ProductPojo> saved = productDao.saveAll(validForInsert);

            for (ProductPojo savedPojo : saved) {
                ProductUploadResult r = resultMap.get(savedPojo.getBarcode());
                r.setStatus("SUCCESS");
                r.setMessage("SUCCESS");
            }

        } catch (Exception e) {
            markDatabaseFailure(validForInsert, resultMap, e);
        }

    }

    private void markDatabaseFailure(List<ProductPojo> validForInsert, Map<String, ProductUploadResult> resultMap, Exception e) {

        for (ProductPojo p : validForInsert) {
            ProductUploadResult r = resultMap.get(p.getBarcode());
            r.setStatus("FAILED");
            r.setMessage("Database error: " + e.getMessage());
        }
    }

    public void checkBarcodeExists(String barcode) throws ApiException {

        ProductPojo result = productDao.findByBarcode(barcode);

        if (result != null) { throw new ApiException("Barcode already exists"); }
    }
}
