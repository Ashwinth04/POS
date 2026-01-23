package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.db.UserPojo;
import com.increff.pos.dto.ProductDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryUpdateResult;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.ProductUploadResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductApiImpl implements ProductApi {
    private static final Logger logger = LoggerFactory.getLogger(ProductApiImpl.class);

    private final ProductDao productDao;

    private final InventoryDao inventoryDao;

    private final ClientDao clientDao;

    public ProductApiImpl(ProductDao productDao, InventoryDao inventoryDao, ClientDao clientDao) {
        this.productDao = productDao;
        this.inventoryDao = inventoryDao;
        this.clientDao = clientDao;
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo addProduct(ProductPojo productPojo) throws ApiException {

        logger.info("Adding a product with barcode: {}", productPojo.getBarcode());

        checkClientExists(productPojo);
        checkBarcodeExists(productPojo.getBarcode());

        ProductPojo saved = productDao.save(productPojo);

        logger.info("Product created with barcode: {}", saved.getBarcode());

        createDummyInventoryRecord(productPojo);

        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, ProductUploadResult> addProductsBulk(List<ProductPojo> pojos) {

        Map<String, ProductUploadResult> resultMap = initializeResultMap(pojos);

        if (pojos == null || pojos.isEmpty()) {
            return resultMap;
        }

        List<String> existingClientNames = fetchExistingClientNames(pojos);

        List<ProductPojo> validForInsert = filterValidClients(pojos, existingClientNames, resultMap);

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

    private List<String> fetchExistingClientNames(List<ProductPojo> pojos) {
        List<String> requestedClientNames = pojos.stream()
                .map(ProductPojo::getClientName)
                .toList();

        return clientDao.findExistingClientNames(requestedClientNames);
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

            System.out.println(resultMap.keySet());


            for (ProductPojo savedPojo : saved) {
                createDummyInventoryRecord(savedPojo);
                resultMap.remove(savedPojo.getBarcode());
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

    public InventoryPojo getDummyInventoryRecord(ProductPojo productPojo) throws ApiException {
        InventoryPojo dummyInventory = new InventoryPojo();
        dummyInventory.setBarcode(productPojo.getBarcode());
        dummyInventory.setQuantity(0);
        return dummyInventory;
    }

    public InventoryPojo createDummyInventoryRecord(ProductPojo productPojo) throws ApiException {
        InventoryPojo dummyInventory = getDummyInventoryRecord(productPojo);
        logger.info("Adding new inventory for product with barcode: {}", dummyInventory.getBarcode());
        InventoryPojo saved = inventoryDao.save(dummyInventory);
        logger.info("Inventory added for product with id: {}", saved.getBarcode());
        return saved;
    }

    public Page<ProductPojo> getAllProducts(int page, int size) {
        logger.info("Fetching products page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productDao.findAll(pageRequest);
    }

    public void checkClientExists(ProductPojo productPojo) throws ApiException {

        String clientName = productPojo.getClientName();

        ClientPojo client = clientDao.findByName(clientName);

        if (client == null) { throw new ApiException("Client with the given name does not exist"); }
    }

    public void checkBarcodeExists(String barcode) throws ApiException {

        ProductPojo result = productDao.findByBarcode(barcode);

        if (result != null) { throw new ApiException("Barcode already exists"); }
    }
}
