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

        sb.append("barcode\tstatus\tmessage\n");

        for (RowError r : results) {

            sb.append(r.getBarcode()).append("\t")
                    .append("FAILED").append("\t")
                    .append(safe(r.getMessage()))
                    .append("\n");
        }

        byte[] tsvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(tsvBytes);
    }

    public static String generateInventoryUpdateResults(List<RowError> invalidInventory) {
        if (invalidInventory.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        sb.append("barcode\tmessage\n");

        for (RowError row: invalidInventory) {
            sb.append(safe(row.getBarcode()))
                    .append("\t")
                    .append(row.getMessage())
                    .append("\n");
        }

        byte[] tsvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(tsvBytes);
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\n", " ");
    }
}
