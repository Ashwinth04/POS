package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderItem;
import com.increff.pos.model.data.OrderItemForm;
import com.increff.pos.model.form.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ValidationUtil {

    // User validations
    public static void validateUserForm(UserForm form) throws ApiException {
        validateEmail(form.getEmail());
        validateName(form.getName());
    }

    public static void validateClientForm(ClientForm clientForm) throws ApiException {
        validateName(clientForm.getName());
        validateName(clientForm.getLocation());
        validateEmail(clientForm.getEmail());
        validatePhoneNumber(clientForm.getPhoneNumber());
    }

    public static void validateProductForm(ProductForm productForm) throws ApiException {
        validateName(productForm.getName());
        validateMrp(productForm.getMrp());
        validateUrl(productForm.getImageUrl());
    }

    public static void validateOrderForm(OrderForm orderForm) throws ApiException {

        if (orderForm == null) {
            throw new ApiException("OrderForm cannot be null");
        }

        validateOrderItems(orderForm.getOrderItems());
    }

    private static void validateOrderItems(List<OrderItemForm> items) throws ApiException {
        if (items == null || items.isEmpty()) {
            throw new ApiException("orderItems cannot be empty");
        }

        for (int i = 0; i < items.size(); i++) {
            validateOrderItem(items.get(i), i);
        }
    }

    private static void validateOrderItem(OrderItemForm item, int index) throws ApiException {
        if (item == null) {
            throw new ApiException("orderItems[" + index + "] cannot be null");
        }

        if (item.getProductId() == null) {
            throw new ApiException("orderItems[" + index + "].productId cannot be null");
        }

        if (item.getOrderedQuantity() == null || item.getOrderedQuantity() <= 0) {
            throw new ApiException("orderItems[" + index + "].orderedQuantity must be > 0");
        }

        if (item.getSellingPrice() == null || item.getSellingPrice() <= 0) {
            throw new ApiException("orderItems[" + index + "].sellingPrice must be > 0");
        }
    }

    public static void validateInventoryForm(InventoryForm inventoryForm) throws ApiException {
        validateQuantity(inventoryForm.getQuantity());
    }

    private static void validateEmail(String email) throws ApiException {
        if (!StringUtils.hasText(email)) {
            throw new ApiException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ApiException("Invalid email format");
        }
    }

    private static void validatePhoneNumber(String phoneNumber) throws ApiException {
        if (phoneNumber == null || phoneNumber.length() != 10 || !phoneNumber.matches("\\d{10}")) {
            throw new ApiException("Not a valid phone number");
        }
    }

    public static void validateName(String name) throws ApiException {
        if (!StringUtils.hasText(name)) {
            throw new ApiException("Name cannot be empty");
        }
    }

    public static void validateLocation(String location) throws ApiException {
        if (!StringUtils.hasText(location)) {
            throw new ApiException("Location cannot be empty");
        }
    }

    private static void validateMrp(Double mrp) throws ApiException {
        if (mrp == 0) {
            throw new ApiException("MRP cannot be zero");
        }
    }

    private static void validateQuantity(int quantity) throws ApiException {
        if (quantity == 0) {
            throw new ApiException("Quantity cannot be zero");
        }
    }

    private static void validateProduct(int productId) throws ApiException {
        // Add logic to check if productId exists in the DB
    }

    private static void validateUrl(String url) throws ApiException {
        try {
            URL newUrl = new URL(url);
            URLConnection conn = newUrl.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            throw new ApiException("Not a valid URL");
        } catch (IOException e) {
            throw new ApiException("Connection couldn't be established to the URL");
        }
    }

    // Pagination validations
    public static void validatePageForm(PageForm form) throws ApiException {
        if (form.getPage() < 0) {
            throw new ApiException("Page number cannot be negative");
        }
        if (form.getSize() <= 0) {
            throw new ApiException("Page size must be positive");
        }
        if (form.getSize() > 100) {
            throw new ApiException("Page size cannot be greater than 100");
        }
    }
} 