package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryApiImpl implements InventoryApi{

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateSingleInventory(InventoryPojo inventoryPojo) throws ApiException {

        inventoryDao.updateInventory(inventoryPojo);
        return inventoryPojo;
    }

    public boolean reserveInventory(List<InventoryPojo> items) throws ApiException {

        boolean isFulfillable = checkOrderFulfillable(items);

        if (isFulfillable) {

            for (InventoryPojo item: items) {
                item.setQuantity(-item.getQuantity());
            }

            updateBulkInventory(items);
        }
        return isFulfillable;
    }

    public void editOrder(Map<String, Integer> existingItems, Map<String, Integer> incomingItems) throws ApiException {

        Map<String, Integer> delta = calculateDeltaInventory(existingItems, incomingItems);
        updateDeltaInventory(delta);

    }

    public void revertInventory(List<InventoryPojo> orderInventoryPojos) {

        for (InventoryPojo pojo: orderInventoryPojos) {
            int quantity = pojo.getQuantity();
            pojo.setQuantity(quantity);
        }

        updateBulkInventory(orderInventoryPojos);
    }

    public void createDummyInventoryRecord(String productId) {

        InventoryPojo dummyRecord = new InventoryPojo();
        dummyRecord.setProductId(productId);
        dummyRecord.setQuantity(0);

        inventoryDao.save(dummyRecord);
    }

    public void createDummyInventoryRecordsBulk(List<String> productIds) {

        if (productIds == null || productIds.isEmpty()) {
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

    public Map<String, InventoryPojo> fetchRecordsForOrderItems(List<InventoryPojo> items) {

        // 1. Extract barcodes from order items
        List<String> productIds = items.stream()
                .map(InventoryPojo::getProductId)
                .distinct()
                .toList();

        List<InventoryPojo> inventoryList = inventoryDao.findByProductIds(productIds);

        return inventoryList.stream()
                .collect(Collectors.toMap(
                        InventoryPojo::getProductId,
                        inv -> inv
                ));

    }

    public boolean checkOrderFulfillable(List<InventoryPojo> items) throws ApiException {

        Map<String, InventoryPojo> existingRecords = fetchRecordsForOrderItems(items);

        boolean allFulfillable = true;

        for (InventoryPojo item : items) {
            String productId = item.getProductId();
            boolean fulfillable = isItemFulfillable(item,existingRecords.get(productId));

            if (!fulfillable) allFulfillable = false;
        }

        return allFulfillable;
    }

    private boolean isItemFulfillable(InventoryPojo item, InventoryPojo existingRecord) {

        int available = existingRecord.getQuantity();
        int required = item.getQuantity();
        return available >= required;
    }

    public void updateBulkInventory(List<InventoryPojo> pojos) {

        if (pojos == null || pojos.isEmpty()) {
            return;
        }

        inventoryDao.bulkUpdate(pojos);
    }

    public void updateDeltaInventory(Map<String, Integer> delta)  {

        List<InventoryPojo> pojos = InventoryHelper.getPojosFromMap(delta);
        inventoryDao.bulkUpdate(pojos);

    }

    private Map<String, Integer> calculateDeltaInventory(Map<String, Integer> existingItems, Map<String, Integer> incomingItems) {

        Map<String, Integer> delta = new HashMap<>();

        // Add all incoming (positive)
        incomingItems.forEach((barcode, qty) ->
                delta.put(barcode, qty)
        );

        // Subtract all existing
        existingItems.forEach((barcode, qty) ->
                delta.merge(barcode, -qty, Integer::sum)
        );

        return delta;
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

    public Map<String, InventoryPojo> getInventoryForProductIds(List<String> productIds) {

        Map<String, InventoryPojo> productIdToInventory =
                inventoryDao.findByProductIds(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                InventoryPojo::getProductId,
                                Function.identity()
                        ));

        return productIdToInventory;

    }
}
