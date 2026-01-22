package com.increff.pos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupervisorConfig {

    @Value("${app.supervisor.username}")
    private String username;

    @Value("${app.supervisor.password}")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
