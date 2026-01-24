package com.increff.pos.helper;

import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;

public class InventoryHelper {

    public static InventoryPojo convertToEntity(String barcode, InventoryForm inventoryForm) {

        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setBarcode(barcode);
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        return inventoryPojo;
    }

    public static InventoryPojo convertRowToEntity(String[] row) {

        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setBarcode(row[0]);
        inventoryPojo.setQuantity(Integer.parseInt(row[1]));
        return inventoryPojo;
    }

    public static InventoryData convertToDto(InventoryPojo inventoryPojo) {

        InventoryData inventoryData = new InventoryData();
        inventoryData.setBarcode(inventoryPojo.getBarcode());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        return inventoryData;
    }

}
