package com.increff.pos.dto;

import com.increff.pos.api.InvoiceApiImpl;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InvoiceDto {

    private final InvoiceApiImpl invoiceApi;

    public InvoiceDto(InvoiceApiImpl invoiceApi) {
        this.invoiceApi = invoiceApi;
    }

    public FileData generateInvoice(OrderData orderData) throws ApiException {
        return invoiceApi.generateInvoice(orderData);
    }

    public FileData downloadInvoice(String orderId) throws ApiException, IOException {
        ValidationUtil.validateOrderId(orderId);
        return invoiceApi.downloadInvoice(orderId);
    }
}
