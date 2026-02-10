package com.increff.pos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "")
public class ApplicationProperties {

    private String invoiceServiceBaseUrl;

    public String getInvoiceServiceBaseUrl() {
        return invoiceServiceBaseUrl;
    }

    public void setInvoiceServiceBaseUrl(String invoiceServiceBaseUrl) {
        this.invoiceServiceBaseUrl = invoiceServiceBaseUrl;
    }
}