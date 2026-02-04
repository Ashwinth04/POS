package com.increff.pos.util;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.form.*;

import java.util.List;

public class NormalizationUtil {

    public static void normalizeClientForm(ClientForm clientForm) {

        String location = clientForm.getLocation().toLowerCase();
        clientForm.setEmail(clientForm.getEmail().toLowerCase());
        clientForm.setLocation(location);
    }

    public static void normalizeOrderForm(OrderForm orderForm) {

        for (OrderItemForm orderItem: orderForm.getOrderItems()) {
            orderItem.setBarcode(orderItem.getBarcode().toLowerCase());
        }
    }

    public static void normalizeProductForm(ProductForm productForm) {

        productForm.setBarcode(productForm.getBarcode().toLowerCase());
        productForm.setClientName(productForm.getClientName());
    }

    public static void normalizeUsername(String username) {
        username.trim().toLowerCase();
    }

    public static void normalizeInventoryForm(InventoryForm inventoryForm) {
        inventoryForm.setBarcode(inventoryForm.getBarcode().toLowerCase());
    }

    public static void normalizeSearchProductForm(SearchQueryForm searchProductForm) {
        searchProductForm.setType(searchProductForm.getType().toLowerCase());
        searchProductForm.setQuery(searchProductForm.getQuery().toLowerCase());
    }
}
