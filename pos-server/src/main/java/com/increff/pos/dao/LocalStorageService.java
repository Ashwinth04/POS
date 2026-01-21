package com.increff.pos.dao;

import org.springframework.stereotype.Service;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Service
public class LocalStorageService implements StorageService {

    private static final String BASE_DIR = "data";

    @Override
    public byte[] readInvoice(String orderId) throws IOException {
        // Example: data/2026-01-20/12345.pdf
        String today = LocalDate.now().toString();

        Path filePath = Path.of("..", "data", today, orderId + ".pdf");


        System.out.println("Working directory: " + Paths.get("").toAbsolutePath());
        System.out.println("Trying to read: " + filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            System.out.println("File doesnt exist in the given path");
            throw new IOException("Invoice file not found: " + filePath);
        }

        return Files.readAllBytes(filePath);
    }
}
