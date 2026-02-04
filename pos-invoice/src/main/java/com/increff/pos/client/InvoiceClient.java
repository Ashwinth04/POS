package com.increff.pos.client;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.FileData;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class InvoiceClient {

    private final WebClient webClient;

    public InvoiceClient(
            @Value("${invoice.service.base-url}") String baseUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }


    public FileData generateInvoice(OrderData orderData) throws ApiException {
        try {
            return webClient.post()
                    .uri("/api/invoice/generate-invoice/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(orderData)
                    .retrieve()
                    .bodyToMono(FileData.class)
                    .block();

        } catch (WebClientResponseException e) {
            throw handleException(e);
        }
    }

    public FileData downloadInvoice(String orderId) throws ApiException {
        try {
            return webClient.get()
                    .uri("/api/invoice/download-invoice/{orderId}", orderId)
                    .retrieve()
                    .bodyToMono(FileData.class)
                    .block();

        } catch (WebClientResponseException e) {
            throw handleException(e);
        }
    }

    private ApiException handleException(WebClientResponseException e) {
        HttpStatusCode status = e.getStatusCode();

        if (status.is4xxClientError()) {
            return new ApiException("Invoice service client error: " + e.getResponseBodyAsString());
        }

        if (status.is5xxServerError()) {
            return new ApiException("Invoice service server error");
        }

        return new ApiException("Invoice service call failed");
    }
}
