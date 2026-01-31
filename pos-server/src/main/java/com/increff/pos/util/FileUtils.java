package com.increff.pos.util;

import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.data.RowError;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class FileUtils {

    public static String generateProductUploadResults(List<RowError> results) {

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("barcode\tstatus\tmessage\n");

        for (RowError r : results) {

            sb.append(r.getBarcode()).append("\t")
                    .append("FAILED").append("\t")
                    .append(safe(r.getMessage()))
                    .append("\n");
        }

        // Convert TSV string → bytes → Base64 string
        byte[] tsvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(tsvBytes);
    }

    public static String generateInventoryUpdateResults(Map<String, String> inventoryUpdateResults) {
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

    private static String safe(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\n", " ");
    }
}
