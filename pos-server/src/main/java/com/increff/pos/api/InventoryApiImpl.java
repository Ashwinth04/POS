package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryApiImpl implements InventoryApi{

    private final InventoryDao inventoryDao;

    public InventoryApiImpl(InventoryDao inventoryDao) {
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

    public void createDummyInventoryRecord(String barcode) throws ApiException {

        InventoryPojo dummyRecord = new InventoryPojo();
        dummyRecord.setBarcode(barcode);
        dummyRecord.setQuantity(0);

        inventoryDao.save(dummyRecord);
    }

    public boolean checkOrderFulfillable(List<OrderItem> items, Map<String, OrderStatus> statuses) throws ApiException {

        boolean allFulfillable = true;

        for (OrderItem item : items) {
            boolean fulfillable = isItemFulfillable(item);
            updateItemAndStatus(item, fulfillable, statuses);

            if (!fulfillable) allFulfillable = false;
        }

        return allFulfillable;
    }

    private boolean isItemFulfillable(OrderItem item) throws ApiException {

        int available = inventoryDao.getQuantity(item.getBarcode());
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

        for (OrderItem item : orderItems) {
            applyInventoryUpdate(item);
        }
    }

    private void applyInventoryUpdate(OrderItem item) throws ApiException {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setBarcode(item.getBarcode());
        pojo.setQuantity(-item.getOrderedQuantity());

        inventoryDao.updateInventory(pojo);
    }

}
