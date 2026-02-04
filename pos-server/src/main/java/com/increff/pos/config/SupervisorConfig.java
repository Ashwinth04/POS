package com.increff.pos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SupervisorConfig {

    @Value("${app.supervisor.email}")
    private String email;

    @Value("${app.supervisor.password}")
    private String password;
}
