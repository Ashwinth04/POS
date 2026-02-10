package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryApiImpl implements InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateSingleInventory(InventoryPojo inventoryPojo) throws ApiException {
        inventoryDao.updateInventory(inventoryPojo);
        return inventoryPojo;
    }

    public boolean reserveInventory(List<InventoryPojo> inventoryUpdatePojos) {
        Map<String, InventoryPojo> productIdToInventoryPojoMap = getProductIdToInventoryPojoMap(inventoryUpdatePojos);

        for (InventoryPojo inventoryUpdatePojo : inventoryUpdatePojos) {
            String productId = inventoryUpdatePojo.getProductId();
            boolean fulfillable = InventoryHelper.hasSufficientInventory(inventoryUpdatePojo,productIdToInventoryPojoMap.get(productId));
            if (!fulfillable) return false;

            inventoryUpdatePojo.setQuantity(-inventoryUpdatePojo.getQuantity());
        }

        inventoryDao.bulkUpdate(inventoryUpdatePojos);
        return true;
    }

    @Transactional(rollbackFor = ApiException.class)
    public void calculateAndUpdateDeltaInventory(Map<String, Integer> existingItems, Map<String, Integer> incomingItems) {
        Map<String, Integer> delta = calculateDeltaInventory(existingItems, incomingItems);
        updateDeltaInventory(delta);
    }

    @Transactional(rollbackFor = ApiException.class)
    public void revertInventory(List<InventoryPojo> orderInventoryPojos) {

        for (InventoryPojo pojo: orderInventoryPojos) {
            int quantity = pojo.getQuantity();
            pojo.setQuantity(quantity);
        }

        inventoryDao.bulkUpdate(orderInventoryPojos);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createDummyInventoryRecord(String productId) {

        InventoryPojo dummyRecord = new InventoryPojo();
        dummyRecord.setProductId(productId);
        dummyRecord.setQuantity(0);

        inventoryDao.save(dummyRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createDummyInventoryRecordsBulk(List<String> productIds) {

        if (Objects.isNull(productIds) || productIds.isEmpty()) {
            return;
        }

        List<InventoryPojo> inventoryPojos = new ArrayList<>();

        for (String productId : productIds) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setProductId(productId);
            pojo.setQuantity(0);
            inventoryPojos.add(pojo);
        }

        inventoryDao.saveAll(inventoryPojos);
    }

    public boolean checkOrderFulfillable(List<InventoryPojo> inventories) {
        Map<String, InventoryPojo> productIdToInventoryPojoMap = getProductIdToInventoryPojoMap(inventories);

        boolean allFulfillable = true;
        for (InventoryPojo item : inventories) {
            String productId = item.getProductId();
            boolean fulfillable = InventoryHelper.hasSufficientInventory(item,productIdToInventoryPojoMap.get(productId));

            if (!fulfillable) allFulfillable = false;
        }

        return allFulfillable;
    }

    public void updateBulkInventory(List<InventoryPojo> pojos) {
        inventoryDao.bulkUpdate(pojos);
    }

    public void updateDeltaInventory(Map<String, Integer> deltaInventory) {
        List<InventoryPojo> pojos = InventoryHelper.getPojosFromMap(deltaInventory);
        inventoryDao.bulkUpdate(pojos);
    }

    // TODO: Check if this can be here
    private Map<String, Integer> calculateDeltaInventory(Map<String, Integer> existingItems, Map<String, Integer> incomingItems) {

        Map<String, Integer> deltaInventory = new HashMap<>();

        // Add all incoming
        incomingItems.forEach((barcode, qty) ->
                deltaInventory.put(barcode, qty)
        );

        // Subtract all existing
        existingItems.forEach((barcode, qty) ->
                deltaInventory.merge(barcode, -qty, Integer::sum)
        );

        return deltaInventory;
    }

    public Map<String, Integer> aggregateItemsByProductId(List<InventoryPojo> inventoryPojos) {

        Map<String, Integer> aggregatedItems = new HashMap<>();

        for (InventoryPojo pojo : inventoryPojos) {
            String productId = pojo.getProductId();
            Integer quantity = pojo.getQuantity();

            aggregatedItems.merge(productId, quantity, Integer::sum);
        }

        return aggregatedItems;
    }

    public Map<String, InventoryPojo> getProductIdToInventoryPojoMap(List<InventoryPojo> inventoryPojos) {
        List<String> productIds = inventoryPojos.stream()
                .map(InventoryPojo::getProductId)
                .distinct()
                .toList();

        return getInventoryForProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        InventoryPojo::getProductId,
                        Function.identity()
                ));
    }

    public List<InventoryPojo> getInventoryForProductIds(List<String> productIds) {
        return inventoryDao.findByProductIds(productIds);
    }
}
