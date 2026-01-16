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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductApiImpl implements ProductApi {
    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    @Autowired
    private ProductDao productDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ClientDao clientDao;

    @Transactional(rollbackFor = ApiException.class)
    public ProductPojo add(ProductPojo productPojo) throws ApiException {
        logger.info("Adding a product with name: {}", productPojo.getName());

        checkClientExists(productPojo);
        checkBarcodeExists(productPojo.getBarcode());

        ProductPojo saved = productDao.save(productPojo);
        logger.info("Product created with name: {}", saved.getName());
        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String, ProductUploadResult> bulkAdd(List<ProductPojo> pojos) {

        //Take all the valid Product pojos and uploads them in bulk. Also, check if the client ids exist in the DB

        Map<String, ProductUploadResult> resultMap = new HashMap<>();

        if (pojos == null || pojos.isEmpty()) {
            return resultMap;
        }

        //Initialize result map
        for (ProductPojo p : pojos) {
            ProductUploadResult r = new ProductUploadResult();
            r.setBarcode(p.getBarcode());
            r.setClientId(p.getClientId());
            r.setName(p.getName());
            r.setMrp(p.getMrp());
            r.setImageUrl(p.getImageUrl());

            r.setStatus("PENDING");
            resultMap.put(p.getBarcode(), r);
        }

        // Fetch all existing clients in ONE call
        Set<String> requestedClientIds = pojos.stream()
                .map(ProductPojo::getClientId)
                .collect(Collectors.toSet());

        Set<String> existingClientIds = clientDao.findExistingClientIds(requestedClientIds);

        // Step 3: Filter valid vs invalid clients
        List<ProductPojo> validForInsert = new ArrayList<>();

        // Iterate through the pojos and check if the client ids exist. If it doesn't, mark it as failed.
        for (ProductPojo p : pojos) {
            ProductUploadResult r = resultMap.get(p.getBarcode());

            if (!existingClientIds.contains(p.getClientId())) {
                r.setStatus("FAILED");
                r.setMessage("Client with the given id does not exist");
            } else {
                validForInsert.add(p);
            }
        }

        // Step 4: Bulk insert valid ones
        try {
            // Save only the valid pojos (Valid input + clientId exists in DB)
            List<ProductPojo> saved = productDao.saveAll(validForInsert);

            // Mark status
            for (ProductPojo savedPojo : saved) {
                addInventory(savedPojo);
                ProductUploadResult r = resultMap.get(savedPojo.getBarcode());
                r.setStatus("SUCCESS");
                r.setProductId(savedPojo.getId());
                r.setMessage("Inserted successfully");
            }

        } catch (Exception e) {
            // if bulk insert explodes (constraint, DB down, etc) - just an extra layer of exception handling
            for (ProductPojo p : validForInsert) {
                ProductUploadResult r = resultMap.get(p.getBarcode());
                r.setStatus("FAILED");
                r.setMessage("Database error: " + e.getMessage());
            }
        }

        return resultMap;
    }

    public InventoryPojo getDummyInventoryRecord(ProductPojo productPojo) throws ApiException {
        InventoryPojo dummyInventory = new InventoryPojo();
        dummyInventory.setProductId(productPojo.getId());
        dummyInventory.setQuantity(0);
        return dummyInventory;
    }

    public InventoryPojo addInventory(ProductPojo productPojo) throws ApiException {
        InventoryPojo dummyInventory = getDummyInventoryRecord(productPojo);
        logger.info("Adding new inventory for product with id: {}", dummyInventory.getProductId());
        InventoryPojo saved = inventoryDao.save(dummyInventory);
        logger.info("Inventory added for product with id: {}", saved.getProductId());
        return saved;
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateInventory(InventoryPojo inventoryPojo) throws ApiException {
        inventoryDao.updateInventory(inventoryPojo);
        return inventoryPojo;
    }

    public Page<ProductPojo> getAll(int page, int size) {
        logger.info("Fetching productds page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productDao.findAll(pageRequest);
    }

    public void checkClientExists(ProductPojo productPojo) throws ApiException {

        String clientId = productPojo.getClientId();

        ClientPojo client = clientDao.findById(clientId)
                .orElseThrow(() -> new ApiException("Client with the given id does not exist"));
    }

    public void checkBarcodeExists(String barcode) throws ApiException {
        ProductPojo result = productDao.findByBarcode(barcode);

        if (result != null) { throw new ApiException("Barcode already exists"); }
    }
}
