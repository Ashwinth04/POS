package com.increff.pos.invoiceUtils;

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
            String orderId = order.getOrderId();
            Path outputPath = createOutputPath(orderId);
            byte[] pdfBytes = generatePdfBytes(foContent);
            writePdfToFile(pdfBytes, outputPath);
            return encodeBase64(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    private static Path createOutputPath(String orderId) throws IOException {
        LocalDate date = extractDateFromOrderId(orderId);
        String folderName = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
        Path dir = projectRoot.resolve("data").resolve(folderName);
        Files.createDirectories(dir);
        return dir.resolve(orderId + ".pdf");
    }

    private static LocalDate extractDateFromOrderId(String orderId) {
        String rawDate = orderId.split("-")[1];
        return LocalDate.parse(rawDate, DateTimeFormatter.BASIC_ISO_DATE);
    }

    private static byte[] generatePdfBytes(String foContent) throws Exception {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOutputStream);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        Source src = new StreamSource(new StringReader(foContent));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        return pdfOutputStream.toByteArray();
    }

    private static void writePdfToFile(byte[] pdfBytes, Path outputPath) throws IOException {
        Files.write(outputPath, pdfBytes);
    }

    private static String encodeBase64(byte[] pdfBytes) {
        return Base64.getEncoder().encodeToString(pdfBytes);
    }
}
