package com.increff.pos.helper;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.db.InventoryPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.form.InventoryForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryHelper {

    public static InventoryPojo convertToEntity(String barcode, InventoryForm inventoryForm) {

        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setBarcode(barcode);
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        return inventoryPojo;
    }

    public static InventoryPojo convertRowToEntity(String[] row) {

        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setBarcode(row[0].trim());
        inventoryPojo.setQuantity(Integer.parseInt(row[1].trim()));
        return inventoryPojo;
    }

    public static InventoryData convertToDto(InventoryPojo inventoryPojo) {

        InventoryData inventoryData = new InventoryData();
        inventoryData.setBarcode(inventoryPojo.getBarcode());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        return inventoryData;
    }

    public static InventoryPojo normalizeInventoryPojo(InventoryPojo inventoryPojo) {
        String barcode = inventoryPojo.getBarcode().toLowerCase();
        inventoryPojo.setBarcode(barcode);

        return inventoryPojo;
    }

    public static List<InventoryPojo> getPojosFromMap(Map<String, Integer> delta) {
        List<InventoryPojo> pojos = new ArrayList<>();

        for (String barcode: delta.keySet()) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setBarcode(barcode);
            pojo.setQuantity(-delta.get(barcode));
            pojos.add(pojo);
        }

        return pojos;
    }

    public static List<InventoryPojo> getPojosFromOrderItems(List<OrderItem> orderItems) {

        List<InventoryPojo> pojos = new ArrayList<>();

        for (OrderItem item: orderItems) {
            InventoryPojo pojo = new InventoryPojo();
            pojo.setQuantity(-item.getOrderedQuantity()); // negative because inventory needs to be reduced
            pojo.setBarcode(item.getBarcode());
            pojos.add(pojo);
        }

        return pojos;
    }

}
