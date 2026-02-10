package com.increff.pos.util;

import com.increff.pos.model.data.FileData;
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

    public static String getValueFromRow(String[] row, Map<String, Integer> headerIndexMap, String header) {
        Integer index = headerIndexMap.get(header);
        if (index == null || index >= row.length) {
            return null;
        }
        return row[index].trim();
    }

    public static FileData convertProductResultsToBase64(List<RowError> results) {

        String resultFile = generateProductUploadResults(results);

        FileData fileData = new FileData();
        fileData.setStatus(results.isEmpty() ? "SUCCESS" : "UNSUCCESSFUL");
        fileData.setBase64file(resultFile);

        return fileData;
    }
}
