package com.increff.pos.client;

import com.increff.pos.config.ApplicationProperties;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class InvoiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InvoiceClient(ApplicationProperties applicationProperties) {
        this.baseUrl = applicationProperties.getInvoiceServiceBaseUrl();
        this.restTemplate = new RestTemplate();
    }

    public FileData generateInvoice(OrderData orderData) throws ApiException {
        try {
            String url = baseUrl + "/api/invoice/generate-invoice/";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OrderData> requestEntity = new HttpEntity<>(orderData, headers);

            ResponseEntity<FileData> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    FileData.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new ApiException("Invoice service client error: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException("Invoice service server error");
        } catch (Exception e) {
            throw new ApiException("Invoice service call failed: " + e.getMessage());
        }
    }

    public FileData downloadInvoice(String orderId) throws ApiException {
        try {
            String url = baseUrl + "/api/invoice/download-invoice/" + orderId;

            ResponseEntity<FileData> response = restTemplate.getForEntity(url, FileData.class);

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new ApiException("Invoice service client error: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ApiException("Invoice service server error");
        } catch (Exception e) {
            throw new ApiException("Invoice service call failed: " + e.getMessage());
        }
    }
}
