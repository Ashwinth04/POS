package com.increff.pos.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class InvoiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${invoice.service.url}")
    private String invoiceServiceUrl;

    public FileData generateInvoice(OrderData orderData) throws ApiException {

        String url = invoiceServiceUrl + "/api/invoice/generate-invoice/";

        try {
            return restTemplate.postForObject(url, orderData, FileData.class);
        }
        catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(responseBody);
                String message = node.get("message").asText();

                throw new ApiException(message);
            } catch (JsonProcessingException parseException) {
                throw new ApiException("Invoice service error: " + responseBody);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Invoice service unavailable");
        }
    }

    public FileData downloadInvoice(String orderId) throws ApiException {
        String url = invoiceServiceUrl + "/api/invoice/download-invoice/" + orderId;
        return restTemplate.getForObject(url, FileData.class);
    }
}
