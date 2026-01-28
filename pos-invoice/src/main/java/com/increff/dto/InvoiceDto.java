package com.increff.dto;

import com.increff.api.InvoiceApiImpl;
import com.increff.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceDto {

    private final InvoiceApiImpl invoiceApi;

    public InvoiceDto(InvoiceApiImpl invoiceApi) {
        this.invoiceApi = invoiceApi;
    }

    public FileData generateInvoice(String orderId) throws ApiException {
        ValidationUtil.validateOrderId(orderId);
        return invoiceApi.generateInvoice(orderId);
    }

    public FileData downloadInvoice(String orderId) throws ApiException, IOException {
        ValidationUtil.validateOrderId(orderId);
        return invoiceApi.downloadInvoice(orderId);
    }
}
