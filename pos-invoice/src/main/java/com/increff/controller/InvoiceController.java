package com.increff.controller;

import com.increff.dto.InvoiceDto;
import com.increff.exception.ApiException;
import com.increff.pos.model.data.FileData;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    private final InvoiceDto invoiceDto;

    public InvoiceController(InvoiceDto invoiceDto) {
        this.invoiceDto = invoiceDto;
    }

    @RequestMapping(value = "/generate-invoice/{orderId}", method = RequestMethod.GET)
    public FileData generateInvoice(@PathVariable String orderId) throws ApiException {
        return invoiceDto.generateInvoice(orderId);
    }

    @RequestMapping(value = "/download-invoice/{orderId}", method = RequestMethod.GET)
    public FileData downloadInvoice(@PathVariable String orderId) throws ApiException, IOException {
        return invoiceDto.downloadInvoice(orderId);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        return "SUCESS";
    }
}
