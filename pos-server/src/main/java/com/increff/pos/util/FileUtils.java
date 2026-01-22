package com.increff.pos.util;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.form.ProductForm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class FileUtils {
    public static List<ProductForm> parseBase64File(String base64) throws ApiException {

        if (base64 == null || base64.isBlank()) {
            throw new ApiException("File content is empty");
        }

        // Handle cases like: data:text/csv;base64,xxxx
        if (base64.contains(",")) {
            base64 = base64.split(",", 2)[1];
        }

        byte[] fileBytes;
        try {
            fileBytes = convertFileToBytes(base64);
        } catch (Exception e) {
            throw new ApiException("Invalid base64 file " + e);
        }

        List<ProductForm> forms = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) continue;

                // Skip header safely
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] columns = line.split("\t", -1);

                if (columns.length != 5) {
                    throw new ApiException("Line " + lineNumber + ": Expected 5 columns but found " + columns.length);
                }

                String barcode = columns[0].trim();
                String clientName = columns[1].trim();
                String name = columns[2].trim();
                String mrpStr = columns[3].trim();
                String imageUrl = columns[4].trim();

                // Required field validations
                if (barcode.isBlank()) {
                    throw new ApiException("Line " + lineNumber + ": Barcode cannot be empty");
                }
                if (clientName.isBlank()) {
                    throw new ApiException("Line " + lineNumber + ": ClientName cannot be empty");
                }
                if (name.isBlank()) {
                    throw new ApiException("Line " + lineNumber + ": Name cannot be empty");
                }

                // Safe numeric parsing
                Double mrp = null;
                if (!mrpStr.isBlank()) {
                    try {
                        mrp = Double.valueOf(mrpStr);
                    } catch (NumberFormatException e) {
                        throw new ApiException("Line " + lineNumber + ": Invalid MRP value: " + mrpStr);
                    }
                }

                ProductForm form = new ProductForm();
                form.setBarcode(barcode);
                form.setClientName(clientName);
                form.setName(name);
                form.setMrp(mrp);
                form.setImageUrl(imageUrl);

                forms.add(form);
            }

        } catch (ApiException e) {
            throw e; // preserve meaningful validation errors
        } catch (Exception e) {
            throw new ApiException("Failed to parse TSV file" + e);
        }

        return forms;
    }

    public static String getBase64String(List<ProductUploadResult> results) {

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("barcode\tclientName\tname\tmrp\timageUrl\tproductId\tstatus\tmessage\n");

        for (ProductUploadResult r : results) {
            if (r.getStatus() != "FAILED") continue;

            sb.append(safe(r.getBarcode())).append("\t")
                    .append(safe(r.getClientName())).append("\t")
                    .append(safe(r.getName())).append("\t")
                    .append(r.getMrp() == null ? "" : r.getMrp()).append("\t")
                    .append(safe(r.getImageUrl())).append("\t")
                    .append(safe(r.getProductId())).append("\t")
                    .append(safe(r.getStatus())).append("\t")
                    .append(safe(r.getMessage()))
                    .append("\n");
        }

        // Convert TSV string → bytes → Base64 string
        byte[] tsvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(tsvBytes);
    }

    public static String getBase64InventoryUpdate(Map<String, String> inventoryUpdateResults) {
        if (inventoryUpdateResults.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        sb.append("barcode\tmessage\n");

        for (String key: inventoryUpdateResults.keySet()) {
            sb.append(safe(key))
                    .append("\t")
                    .append(inventoryUpdateResults.get(key))
                    .append("\n");
        }

        byte[] tsvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(tsvBytes);
    }

    public static List<InventoryUpdateForm> getInventoryFormsFromFile(String base64File) throws ApiException {

        byte[] fileBytes = convertFileToBytes(base64File);

        List<InventoryUpdateForm> inventoryForms = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes)))) {

            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] columns = line.split("\t", -1);

                if (columns.length != 2) {

                    throw new ApiException("Line " + lineNumber + ": Expected 2 columns but found " + columns.length + " " + columns[0]);
                }

                InventoryUpdateForm form = new InventoryUpdateForm();

                // Barcode (safe)
                form.setBarcode(columns.length > 0 ? columns[0].trim() : "");

                // Quantity (safe parsing)
                String qtyStr = columns.length > 1 ? columns[1].trim() : "";

                if (form.getBarcode().isBlank()) {
                    throw new ApiException("Line " + lineNumber + ": Barcode cannot be empty");
                }
                if (qtyStr.isBlank()) {
                    throw new ApiException("Line " + lineNumber + ": Quantity cannot be empty");
                }


                // Safe numeric parsing
                Double qty = null;
                if (!qtyStr.isBlank()) {
                    try {
                        qty = Double.valueOf(qtyStr);
                    } catch (NumberFormatException e) {
                        throw new ApiException("Line " + lineNumber + ": Invalid Quantity value: " + qtyStr);
                    }
                }

                try {

                    form.setQuantity(Integer.parseInt(qtyStr));
                } catch (NumberFormatException e) {
                    form.setQuantity(0); // Let validation catch this as invalid
                }

                inventoryForms.add(form);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse TSV from Base64", e);
        }

        return inventoryForms;
    }


    public static byte[] convertFileToBytes(String base64File) {

        if (base64File.contains(",")) {
            base64File = base64File.split(",")[1];
        }

        byte[] fileBytes;

        try {
            fileBytes = Base64.getDecoder().decode(base64File);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Base64 Input", e);
        }

        return fileBytes;

    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\n", " ");
    }
}
