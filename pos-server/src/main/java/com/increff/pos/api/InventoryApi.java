package com.increff.pos.api;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface InventoryApi {
    public Map<String, String> bulkInventoryUpdate(List<InventoryPojo> inventoryPojos);
    public InventoryPojo updateInventory(InventoryPojo inventoryPojo) throws ApiException;

}
