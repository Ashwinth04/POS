package com.increff.pos.util;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.ProductForm;

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
        productForm.setClientName(productForm.getClientName().toLowerCase());
    }
}
