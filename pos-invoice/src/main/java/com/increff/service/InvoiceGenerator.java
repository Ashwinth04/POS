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

public class InvoiceGenerator {

    public void generate(OrderData order) {
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

            Files.createDirectories(dir); // IMPORTANT

            // 4. Full file path
            Path outputPath = dir.resolve(orderId + ".pdf");

            System.out.println("Output path: " + outputPath);

            // 5. Generate PDF
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            try (OutputStream out = Files.newOutputStream(outputPath)) {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();

                Source src = new StreamSource(new StringReader(foContent));
                Result res = new SAXResult(fop.getDefaultHandler());

                transformer.transform(src, res);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }
}
