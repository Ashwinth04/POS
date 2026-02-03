package com.increff.pos.service;

import com.increff.pos.client.InvoiceClient;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceClient invoiceClient;

    public FileData generateInvoice(OrderData orderData) throws ApiException {
        return invoiceClient.generateInvoice(orderData);
    }

    public FileData downloadInvoice(String orderId) throws ApiException {
        return invoiceClient.downloadInvoice(orderId);
    }
}
