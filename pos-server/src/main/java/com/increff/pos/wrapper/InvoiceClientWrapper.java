package com.increff.pos.wrapper;

import com.increff.pos.client.InvoiceClient;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceClientWrapper {

    @Autowired
    private InvoiceClient invoiceClient;

    public FileData generateInvoice(OrderData orderData) throws ApiException {
        return invoiceClient.generateInvoice(orderData);
    }

    public FileData downloadInvoice(String orderId) throws ApiException {
        return invoiceClient.downloadInvoice(orderId);
    }
}
