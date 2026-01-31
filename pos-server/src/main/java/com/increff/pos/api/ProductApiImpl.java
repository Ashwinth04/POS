package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import com.increff.pos.model.data.ProductUploadResult;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    public void addProductsBulk(List<ProductPojo> pojos) throws ApiException {

        persistValidProducts(pojos);
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

    private void persistValidProducts(List<ProductPojo> validForInsert) throws ApiException {

        try {
            List<ProductPojo> saved = productDao.saveAll(validForInsert);
        } catch (Exception e) {
            throw new ApiException("Failed to insert valid products");
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

    public boolean validateAllOrderItems(OrderPojo orderPojo, Map<String, OrderStatus> statuses) {

        boolean validationFailure = false;

        List<String> barcodes = orderPojo.getOrderItems().stream()
                .map(OrderItem::getBarcode)
                .distinct()
                .toList();

        List<ProductPojo> products = productDao.findByBarcodes(barcodes);

        Map<String, ProductPojo> productMap = products.stream()
                .collect(Collectors.toMap(ProductPojo::getBarcode, p -> p));

        for (OrderItem item : orderPojo.getOrderItems()) {
            try {
                validateItem(item, productMap);
                addValidItemStatus(item, statuses);
            } catch (ApiException e) {
                addInvalidItemStatus(item, e.getMessage(), statuses);
                validationFailure = true;
            }
        }

        return validationFailure;
    }

    public void validateItem(OrderItem item, Map<String, ProductPojo> productMap) throws ApiException {

        String barcode = item.getBarcode();
        ProductPojo product = productMap.get(barcode);

        if (product == null) {
            throw new ApiException("Invalid barcode: " + barcode);
        }

        if (item.getSellingPrice() > product.getMrp() || item.getSellingPrice() <= 0) {
            throw new ApiException("Selling price exceeds MRP for barcode: " + barcode);
        }
    }


    public void addValidItemStatus(OrderItem item, Map<String, OrderStatus> statuses) {

        OrderStatus status = new OrderStatus();
        status.setOrderItemId(item.getOrderItemId());
        status.setStatus("VALID");
        status.setMessage("OK");
        statuses.put(item.getOrderItemId(), status);
    }

    public void addInvalidItemStatus(OrderItem item, String errorMessage, Map<String, OrderStatus> statuses) {

        OrderStatus status = new OrderStatus();
        status.setOrderItemId(item.getOrderItemId());
        status.setStatus("INVALID");
        status.setMessage(errorMessage);
        statuses.put(item.getOrderItemId(), status);
    }

    public List<String> findExistingProducts(List<String> barcodes) {
        return productDao.findExistingBarcodes(barcodes);
    }
}
