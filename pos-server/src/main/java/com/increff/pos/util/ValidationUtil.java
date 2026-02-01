package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

public class ValidationUtil {

    // Reflections -> runtime you can modify any classes
    // jakarta annotations -> validator factory
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

    public static void validateProductRow(String[] row) throws ApiException {

        validateName(row[0].trim());
        validateName(row[1].trim());
        validateName(row[2].trim());
        validateMrp(Double.parseDouble(row[3].trim()));
        validateUrl(row[4].trim());
    }

    public static void validateInventoryRow(String[] row) throws ApiException {

        int quantity = Integer.parseInt(row[1]);

        if (quantity <= 0) {
            throw new ApiException("Quantity must be positive");
        }

    }

    public static void validateProductRows(List<String[]> rows) throws ApiException {

        int lineNumber = 2;
        int totalRows = rows.size();
        if (totalRows > 5000) throw new ApiException("Maximum limit for the number of rows is 5000");

        boolean isHeader = true;

        for (String[] columns: rows) {

            if (columns.length != 5) {
                throw new ApiException("Line " + lineNumber + ": Expected 5 columns but found " + columns.length);
            }

            if (isHeader) {
                validateProductUploadHeader(columns);
                isHeader = false;
                continue;
            }

            String barcode = columns[0].trim();
            String clientName = columns[1].trim();
            String name = columns[2].trim();
            String mrpStr = columns[3].trim();
            String imageUrl = columns[4].trim();

            if (barcode.isBlank()) {
                throw new ApiException("Line " + lineNumber + ": Barcode cannot be empty");
            }
            if (clientName.isBlank()) {
                throw new ApiException("Line " + lineNumber + ": ClientName cannot be empty");
            }
            if (name.isBlank()) {
                throw new ApiException("Line " + lineNumber + ": Name cannot be empty");
            }

            Double mrp = null;
            if (!mrpStr.isBlank()) {
                try {
                    mrp = Double.valueOf(mrpStr);
                } catch (NumberFormatException e) {
                    throw new ApiException("Line " + lineNumber + ": Invalid MRP value: " + mrpStr);
                }
            }

            lineNumber++;

        }
    }

    public static void validateProductUploadHeader(String[] header) throws ApiException {

        String barcode = header[0].trim();
        String clientName = header[1].trim();
        String name = header[2].trim();
        String mrpStr = header[3].trim();
        String imageUrl = header[4].trim();

        if (!barcode.equals("barcode")) throw new ApiException("Headers are incorrect!");
        if (!clientName.equals("clientName")) throw new ApiException("Headers are incorrect!");
        if (!name.equals("name")) throw new ApiException("Headers are incorrect!");
        if (!mrpStr.equals("mrp")) throw new ApiException("Headers are incorrect!");
    }

    public static void validateInventoryRows(List<String[]> rows) throws ApiException {

        int lineNumber = 2;
        int totalRows = rows.size();

        if (totalRows > 5000) throw new ApiException("Maximum limit for the number of rows is 5000");

        boolean isHeader = true;

        for(String[] columns: rows) {

            if (columns.length != 2) {
                throw new ApiException("Line " + lineNumber + ": Expected 2 columns but found " + columns.length);
            }

            if (isHeader) {
                validateInventoryUploadHeader(columns);
                isHeader = false;
                continue;
            }

            String barcode = columns[0].trim();
            String quantity = columns[1].trim();

            if (barcode.isBlank()) {
                throw new ApiException("Line " + lineNumber + ": Barcode cannot be empty");
            }

            Integer qty = null;
            if (!quantity.isBlank()) {
                try {
                    qty = Integer.valueOf(quantity);
                } catch (NumberFormatException e) {
                    throw new ApiException("Line " + lineNumber + ": Invalid value for quantity: " + quantity);
                }
            }
            lineNumber++;
        }
    }

    public static void validateInventoryUploadHeader(String[] header) throws ApiException {

        String barcode = header[0].trim();
        String quantity = header[1].trim();

        if (!barcode.equals("barcode")) throw new ApiException("Headers are incorrect!");
        if (!quantity.equals("quantity")) throw new ApiException("Headers are incorrect");

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
            validateName(query);
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
} 