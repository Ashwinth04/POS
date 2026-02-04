package com.increff.pos.util;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.RowError;
import com.increff.pos.model.form.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.increff.pos.constants.Constants.*;
import static com.increff.pos.constants.Constants.MRP;
import static com.increff.pos.util.FileUtils.getValueFromRow;

public class ValidationUtil {

    public static void validateLoginRequest(LoginRequest request) throws ApiException {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ApiException("Username cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ApiException("Password cannot be empty");
        }
    }

    public static void validateOrderId(String orderId) throws ApiException {

        if (orderId.length() != 24) throw new ApiException("Not a valid order id");
    }

    public static void validateEmail(String email) throws ApiException {
        if (!StringUtils.hasText(email)) {
            throw new ApiException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ApiException("Invalid email format");
        }
        if (email.length() < 3 || email.length() > 21) {
            throw new ApiException("Number of characters should be between 3 to 21");
        }
    }

    public static void validateName(String name) throws ApiException {
        if (!StringUtils.hasText(name)) {
            throw new ApiException("Name cannot be empty");
        }

        if (name.length() < 3 || name.length() > 21) {
            throw new ApiException("Number of characters should be between 3 to 21");
        }
    }

    private static void validateMrp(Double mrp) throws ApiException {
        if (mrp <= 0) {
            throw new ApiException("MRP cannot be less than or equal to zero");
        }
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

    public static void validateProductRow(String[] row, Map<String, Integer> headerIndexMap) throws ApiException {

        String barcode = getValueFromRow(row, headerIndexMap, BARCODE);
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be empty");
        }

        String name = getValueFromRow(row, headerIndexMap, PRODUCT_NAME);
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        }

        String clientName = getValueFromRow(row, headerIndexMap, CLIENT_NAME);
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }

        String mrpStr = getValueFromRow(row, headerIndexMap, MRP);
        if (mrpStr == null || mrpStr.trim().isEmpty()) {
            throw new ApiException("MRP cannot be empty");
        }

        if (mrpStr.toLowerCase().contains("e")) {
            throw new ApiException("MRP cannot be in scientific notation: " + mrpStr);
        }

        double mrp;
        try {
            mrp = Double.parseDouble(mrpStr.trim());
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid MRP: " + mrpStr);
        }

        if (mrp <= 0 || Double.isNaN(mrp) || Double.isInfinite(mrp) || mrp > 1_000_000) {
            throw new ApiException("Invalid MRP: " + mrpStr);
        }
    }


    public static void validatePhoneNumber(String phoneNumber) throws ApiException {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new ApiException("Phone number is required");
        }

        if (!phoneNumber.matches("^[0-9]{10}$")) {
            throw new ApiException("Phone number must be exactly 10 digits");
        }
    }


    public static void validateSearchParams(String type, String query) throws ApiException {
        if (type.equals("name")) {
            if (!StringUtils.hasText(type)) {
                throw new ApiException("Name cannot be empty");
            }
        }
        else if (type.equals("email")) {
            validateEmail(query);
        }
        else if (type.equals("phone")) {
            validatePhoneNumber(query);
        }
        else {
            throw new ApiException("Not a valid type");
        }
    }

    public static void validateDates(LocalDate startDate, LocalDate endDate) throws ApiException {

        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }

    }

    public static void validateProductHeaders(Map<String, Integer> headerIndexMap) throws ApiException {

        List<String> requiredHeaders = List.of(
                BARCODE,
                PRODUCT_NAME,
                CLIENT_NAME,
                MRP
        );

        List<String> missing = requiredHeaders.stream()
                .filter(h -> !headerIndexMap.containsKey(h))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException("Missing required columns: " + missing);
        }
    }

    public static void validateHeaders(Map<String, Integer> headerIndexMap) throws ApiException {

        List<String> requiredHeaders = List.of(
                BARCODE,
                QUANTITY
        );

        List<String> missing = requiredHeaders.stream()
                .filter(h -> !headerIndexMap.containsKey(h))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException("Missing required columns: " + missing);
        }
    }

    public static List<ProductPojo> getFinalValidProducts(List<ProductPojo> validProducts, List<RowError> invalidProducts, Map<String, ClientPojo> clientNamesToPojos, Map<String, ProductPojo> barcodesToPojos) {

        Map<String, Long> barcodeCountMap = validProducts.stream()
                .map(ProductPojo::getBarcode)
                .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        List<ProductPojo> finalValidProducts = new ArrayList<>();

        for (int i = 0; i < validProducts.size(); i++) {
            ProductPojo product = validProducts.get(i);

            String clientName = product.getClientName();
            String barcode = product.getBarcode();

            if (barcodeCountMap.get(barcode) > 1) {
                invalidProducts.add(
                        new RowError(barcode, "Duplicate barcode found in upload: " + barcode)
                );
                continue;
            }

            if (!clientNamesToPojos.containsKey(clientName)) {
                invalidProducts.add(
                        new RowError(barcode, "Client does not exist: " + clientName)
                );
                continue;
            }

            if (barcodesToPojos.containsKey(barcode)) {
                invalidProducts.add(
                        new RowError(barcode, "Product with barcode already exists: " + barcode)
                );
                continue;
            }

            finalValidProducts.add(product);
        }

        return finalValidProducts;
    }

    public static boolean isRowEmpty(String[] row) {
        return row == null || row.length == 0 ||
                Arrays.stream(row).allMatch(cell -> cell == null || cell.trim().isEmpty());
    }

    public static void validateRowLimit(List<String[]> rows) throws ApiException {

        if (rows.size() <= 1) {
            throw new ApiException("No product rows found!");
        }

        if (rows.size() > 5000) {
            throw new ApiException("Maximum row limit exceeded!");
        }
    }

    public static void validateCreateUserRequest(CreateUserRequest request) throws ApiException {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ApiException("Username missing");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ApiException("Password missing");
        }
    }

    public static void validateAuthentication(Authentication authentication) throws ApiException {

        if (authentication == null) {
            throw new ApiException("Not logged in");
        }
    }
} 