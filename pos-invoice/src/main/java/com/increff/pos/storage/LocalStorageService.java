package com.increff.pos.storage;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class LocalStorageService implements StorageService {

    private static final String BASE_DIR = "data";

    @Override
    public String readInvoice(String orderId) throws IOException {

        String orderDate = orderId.split("-")[1];

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate date = LocalDate.parse(orderDate, inputFormatter);
        String formattedDate = date.format(outputFormatter);

        Path filePath = Path.of("..", "data", formattedDate, orderId + ".pdf");

        if (!Files.exists(filePath)) {
            throw new IOException("Invoice file not found: " + filePath);
        }

        byte[] fileBytes = Files.readAllBytes(filePath);

        // Convert to Base64
        return Base64.getEncoder().encodeToString(fileBytes);
    }

}

