package com.increff.pos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
//TODO::Change the name to Application properties and add the application properties in this class only and rename the fields
public class SupervisorConfig {

    @Value("${app.supervisor.email}")
    private String email;

    @Value("${app.supervisor.password}")
    private String password;
}
