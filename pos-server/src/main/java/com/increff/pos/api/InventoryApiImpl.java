package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.helper.InventoryHelper;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryApiImpl implements InventoryApi{

    private final InventoryDao inventoryDao;

    public InventoryApiImpl(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryPojo updateSingleInventory(InventoryPojo inventoryPojo) throws ApiException {

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

        List<String> existingBarcodes = inventoryDao.findExistingBarcodes(incomingBarcodes);

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

    public boolean reserveInventory(List<OrderItem> items, Map<String, OrderStatus> statuses) throws ApiException {

        boolean isFulfillable = checkOrderFulfillable(items, statuses);

        if (isFulfillable) updateInventory(items);
        return isFulfillable;
    }

    public void editOrder(Map<String, Integer> existingItems, Map<String, Integer> incomingItems) throws ApiException {

            Map<String, Integer> delta = calculateDeltaInventory(existingItems, incomingItems);

            updateDeltaInventory(delta);
    }

    public void revertInventory(List<OrderItem> orderItems) throws ApiException {

        for (OrderItem item: orderItems) {
            int quantity = item.getOrderedQuantity();
            item.setOrderedQuantity(-quantity);
        }

        updateInventory(orderItems);
    }

    public void createDummyInventoryRecord(String barcode) throws ApiException {

        InventoryPojo dummyRecord = new InventoryPojo();
        dummyRecord.setBarcode(barcode);
        dummyRecord.setQuantity(0);

        inventoryDao.save(dummyRecord);
    }

    public Map<String, InventoryPojo> fetchRecordsForOrderItems(List<OrderItem> orderItems) {

        // 1. Extract barcodes from order items
        List<String> barcodes = orderItems.stream()
                .map(OrderItem::getBarcode)
                .distinct()
                .toList();

        List<InventoryPojo> inventoryList = inventoryDao.findByBarcodes(barcodes);

        return inventoryList.stream()
                .collect(Collectors.toMap(
                        InventoryPojo::getBarcode,
                        inv -> inv
                ));

    }

    public boolean checkOrderFulfillable(List<OrderItem> items, Map<String, OrderStatus> statuses) throws ApiException {

        Map<String, InventoryPojo> existingRecords = fetchRecordsForOrderItems(items);

        boolean allFulfillable = true;

        for (OrderItem item : items) {
            String barcode = item.getBarcode();
            boolean fulfillable = isItemFulfillable(item,existingRecords.get(barcode));
            updateItemAndStatus(item, fulfillable, statuses);

            if (!fulfillable) allFulfillable = false;
        }

        return allFulfillable;
    }

    private boolean isItemFulfillable(OrderItem item, InventoryPojo existingRecord) throws ApiException {

        int available = existingRecord.getQuantity();
        int required = item.getOrderedQuantity();
        return available >= required;
    }

    private void updateItemAndStatus(OrderItem item, boolean fulfillable, Map<String, OrderStatus> statuses) {

        OrderStatus status = new OrderStatus();
        status.setOrderItemId(item.getOrderItemId());

        if (fulfillable) {
            item.setOrderItemStatus("FULFILLABLE");
            status.setStatus("FULFILLABLE");
            status.setMessage("OK");
        } else {
            item.setOrderItemStatus("UNFULFILLABLE");
            status.setStatus("UNFULFILLABLE");
            status.setMessage("Insufficient inventory");
        }

        statuses.put(item.getOrderItemId(), status);
    }

    public void updateInventory(List<OrderItem> orderItems) throws ApiException{

        List<InventoryPojo> pojos = InventoryHelper.getPojosFromOrderItems(orderItems);

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

}
