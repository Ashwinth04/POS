package com.increff.pos.controller;

import com.increff.pos.dto.InvoiceDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    private final InvoiceDto invoiceDto;

    public InvoiceController(InvoiceDto invoiceDto) {
        this.invoiceDto = invoiceDto;
    }

    @RequestMapping(value = "/generate-invoice/", method = RequestMethod.POST)
    public FileData generateInvoice(@RequestBody OrderData orderData) throws ApiException {
        System.out.println("Inside invoice endpoint");
        return invoiceDto.generateInvoice(orderData);
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
