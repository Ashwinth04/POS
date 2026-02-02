package com.increff.pos.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TsvParser {

    public static List<String[]> parseBase64Tsv(String base64) {

        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        String content = new String(decodedBytes, StandardCharsets.UTF_8);

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {

                rows.add(line.split("\t"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse TSV", e);
        }

        return rows;
    }
}
