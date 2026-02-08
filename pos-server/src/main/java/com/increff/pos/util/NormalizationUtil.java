package com.increff.pos.util;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.form.*;

import java.util.List;

public class NormalizationUtil {

    public static void normalizeClientForm(ClientForm clientForm) {

        String location = clientForm.getLocation().trim().toLowerCase();
        clientForm.setEmail(clientForm.getEmail().trim().toLowerCase());
        clientForm.setLocation(location);
    }

    public static void normalizeOrderForm(OrderForm orderForm) {

        for (OrderItemForm orderItem: orderForm.getOrderItems()) {
            orderItem.setBarcode(orderItem.getBarcode().trim().toLowerCase());
        }
    }

    public static void normalizeProductForm(ProductForm productForm) {
        String normalizedBarcode = productForm.getBarcode().trim().toLowerCase();
        productForm.setBarcode(normalizedBarcode);
        productForm.setClientName(productForm.getClientName().trim());
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public static void normalizeInventoryForm(InventoryForm inventoryForm) {
        inventoryForm.setBarcode(inventoryForm.getBarcode().trim().toLowerCase());
    }

    public static void normalizeSearchProductForm(ProductSearchForm searchProductForm) {
        searchProductForm.setQuery(searchProductForm.getQuery().trim().toLowerCase());
    }
}
