package com.increff.pos.util;

import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.form.*;

import java.util.Objects;

public class NormalizationUtil {

    public static void normalizeClientForm(ClientForm clientForm) {
        String name = clientForm.getName();
        String location = clientForm.getLocation();
        String email = clientForm.getEmail();
        String phoneNumber = clientForm.getPhoneNumber();

        if (Objects.nonNull(email)) clientForm.setEmail(email.trim().toLowerCase());
        if (Objects.nonNull(location)) clientForm.setLocation(location.trim().toLowerCase());
        if (Objects.nonNull(phoneNumber)) clientForm.setPhoneNumber(phoneNumber.trim().toLowerCase());
        if (Objects.nonNull(name)) clientForm.setName(name.trim());
    }

    public static void normalizeOrderForm(OrderForm orderForm) {
        if (Objects.isNull(orderForm) || Objects.isNull(orderForm.getOrderItems())) return;

        for (OrderItemForm orderItem: orderForm.getOrderItems()) {
            String barcode = orderItem.getBarcode();
            if (Objects.nonNull(barcode)) orderItem.setBarcode(barcode.trim().toLowerCase());
        }
    }

    public static void normalizeProductForm(ProductForm productForm) {
        String name = productForm.getName();
        String clientName = productForm.getClientName();
        String imageUrl = productForm.getImageUrl();
        String barcode = productForm.getBarcode();

        if (Objects.nonNull(name)) productForm.setName(name.trim().toLowerCase());
        if (Objects.nonNull(clientName)) productForm.setClientName(clientName.trim());
        if (Objects.nonNull(imageUrl)) productForm.setImageUrl(imageUrl.trim().toLowerCase());
        if (Objects.nonNull(barcode)) productForm.setBarcode(barcode.trim().toLowerCase());
    }

    public static void normalizeInventoryForm(InventoryForm inventoryForm) {
        String barcode = inventoryForm.getBarcode();
        if (Objects.nonNull(barcode)) inventoryForm.setBarcode(barcode.trim().toLowerCase());
    }

    public static void normalizeSearchProductForm(ProductSearchForm searchProductForm) {
        String query = searchProductForm.getQuery();
        if (Objects.nonNull(query)) searchProductForm.setQuery(query.trim().toLowerCase());
    }

    public static void normalizeSearchClientForm(ClientSearchForm clientSearchForm) {
        String query = clientSearchForm.getQuery();
        if (Objects.nonNull(query)) clientSearchForm.setQuery(query.trim().toLowerCase());
    }

    public static void normalizeLoginRequest(LoginRequest request) {
        if (Objects.isNull(request)) return;
        String email = request.getEmail();
        if (Objects.nonNull(email)) request.setEmail(email.trim().toLowerCase());
    }

    public static void normalizeCreateOperator(CreateUserRequest request) {
        String email = request.getEmail();
        if (Objects.nonNull(email)) request.setEmail(email.trim().toLowerCase());
    }

    public static String normalizeName(String name) {
        if (Objects.nonNull(name)) return name.trim();

        return "";
    }
}
