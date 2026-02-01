package com.increff.pos.api;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.service.InvoiceGenerator;
import com.increff.pos.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceApiImpl {

    @Autowired
    private StorageService storageService;

    public FileData generateInvoice(OrderData orderData) throws ApiException {

        try {
            String base64String = InvoiceGenerator.generate(orderData);

            FileData fileData = new FileData();
            fileData.setStatus("SUCCESS");
            fileData.setBase64file(base64String);

            return fileData;
        } catch (Exception e) {
            throw new ApiException("Error occurred during file generation");
        }

    }

    public FileData downloadInvoice(String orderId) throws IOException {

        String base64String = storageService.readInvoice(orderId);
        FileData fileData = new FileData();
        fileData.setBase64file(base64String);

        return fileData;
    }
}
