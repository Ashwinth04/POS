package com.increff.service;

import org.apache.fop.apps.*;
import javax.xml.transform.sax.SAXResult;
import com.increff.pos.model.data.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class InvoiceGenerator {

    public static String generate(OrderData order) {
        try {
            String foContent = FoBuilder.build(order);

            // 1. Extract date from orderId (ORD-20260120-XXXX)
            String orderId = order.getId();
            String rawDate = orderId.split("-")[1]; // 20260120

            LocalDate date = LocalDate.parse(rawDate, DateTimeFormatter.BASIC_ISO_DATE);
            String folderName = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // 2026-01-20

            Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
            Path dir = projectRoot.resolve("data").resolve(folderName);
            Files.createDirectories(dir);

            // 2. Full file path
            Path outputPath = dir.resolve(orderId + ".pdf");

            System.out.println("Output path: " + outputPath);

            // 3. Generate PDF into memory (byte array)
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOutputStream);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            Source src = new StreamSource(new StringReader(foContent));
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(src, res);

            // 4. Get generated PDF bytes
            byte[] pdfBytes = pdfOutputStream.toByteArray();

            // 5. Write same bytes to file
            Files.write(outputPath, pdfBytes);

            // 6. Convert to Base64 and return
            return Base64.getEncoder().encodeToString(pdfBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }
}
