package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryApiImpl implements InventoryApi{

    private final ProductDao productDao;

    private final InventoryDao inventoryDao;

    public InventoryApiImpl(ProductDao productDao, InventoryDao inventoryDao) {
        this.productDao = productDao;
        this.inventoryDao = inventoryDao;
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateInventory(InventoryPojo inventoryPojo) throws ApiException {

        inventoryDao.updateInventory(inventoryPojo);
        return inventoryPojo;
    }

    public Map<String, String> bulkInventoryUpdate(List<InventoryPojo> inventoryPojos) {

        Map<String, String> resultMap = new HashMap<>();

        if (inventoryPojos == null || inventoryPojos.isEmpty()) {
            return resultMap;
        }

        List<String> incomingBarcodes = inventoryPojos.stream()
                .map(InventoryPojo::getBarcode)
                .toList();

        List<String> existingBarcodes = productDao.findExistingBarcodes(incomingBarcodes);

        List<InventoryPojo> valid = new ArrayList<>();

        for (InventoryPojo pojo : inventoryPojos) {
            if (existingBarcodes.contains(pojo.getBarcode())) {
                valid.add(pojo);
            } else {
                resultMap.put(
                        pojo.getBarcode(),
                        "Product with the given barcode doesn't exist"
                );
            }
        }

        if (!valid.isEmpty()) {
            inventoryDao.bulkUpdate(valid);
        }

        return resultMap;
    }

}
