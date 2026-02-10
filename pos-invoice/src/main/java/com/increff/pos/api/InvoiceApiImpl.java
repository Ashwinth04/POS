package com.increff.pos.api;

import com.increff.pos.model.data.FileData;
import com.increff.pos.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceApiImpl {

    @Autowired
    private StorageService storageService;

    public FileData downloadInvoice(String orderId) throws IOException {

        String base64String = storageService.readInvoice(orderId);
        FileData fileData = new FileData();
        fileData.setBase64file(base64String);

        return fileData;
    }
}
