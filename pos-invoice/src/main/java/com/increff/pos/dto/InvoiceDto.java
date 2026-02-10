package com.increff.pos.dto;

import com.increff.pos.api.InvoiceApiImpl;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.invoiceUtils.InvoiceGenerator;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceDto {

    @Autowired
    private InvoiceApiImpl invoiceApi;

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

    public FileData downloadInvoice(String orderId) throws ApiException, IOException {
        ValidationUtil.validateOrderId(orderId);
        return invoiceApi.downloadInvoice(orderId);
    }
}
