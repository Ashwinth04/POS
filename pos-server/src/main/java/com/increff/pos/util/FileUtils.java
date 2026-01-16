package com.increff.pos.util;

import com.increff.pos.model.data.ProductUploadResult;
import com.increff.pos.model.form.ProductForm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileUtils {
    public static List<ProductForm> parseBase64File(String base64) {

        System.out.println("Parse tsv called");
        // Handle cases like: data:text/csv;base64,xxxx
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }

        System.out.println("Splitted file");

        byte[] fileBytes;
        try {
            fileBytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Base64 input", e);
        }

        System.out.println("Converted to bytes");

        List<ProductForm> forms = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(fileBytes)))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] columns = line.split("\t", -1);

                ProductForm form = new ProductForm();
                form.setBarcode(columns[0].trim());
                form.setClientId(columns[1].trim());
                form.setName(columns[2].trim());
                form.setMrp(columns[3].isBlank() ? null : Double.valueOf(columns[3].trim()));
                form.setImageUrl(columns[4].trim());

                forms.add(form);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse TSV from Base64", e);
        }

        System.out.println("TSV FILE PARSED SUCCESSFULLY\n\n\n\n" + forms);

        return forms;
    }

    public static String getBase64String(List<ProductUploadResult> results) {

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("barcode\tclientId\tname\tmrp\timageUrl\tproductId\tstatus\tmessage\n");

        for (ProductUploadResult r : results) {
            sb.append(safe(r.getBarcode())).append("\t")
                    .append(safe(r.getClientId())).append("\t")
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

    private static String safe(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\n", " ");
    }
}
