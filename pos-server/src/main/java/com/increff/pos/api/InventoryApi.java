package com.increff.pos.api;

import com.increff.pos.db.documents.InventoryPojo;
import com.increff.pos.exception.ApiException;

import java.util.List;

public interface InventoryApi {
    public void updateBulkInventory(List<InventoryPojo> inventoryPojos);
    public InventoryPojo updateSingleInventory(InventoryPojo inventoryPojo) throws ApiException;

}
