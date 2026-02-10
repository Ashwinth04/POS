package com.increff.pos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ApplicationProperties {

    @Value("${app.supervisor.email}")
    private String supervisorEmail;

    @Value("${app.supervisor.password}")
    private String supervisorPassword;

    @Value("${invoice.service.base-url}")
    private String invoiceUrl;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
}
