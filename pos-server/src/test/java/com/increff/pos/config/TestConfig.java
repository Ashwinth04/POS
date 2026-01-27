package com.increff.pos.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

@SpringBootApplication
@ComponentScan("com.increff.pos")
@TestPropertySource(properties = {
    "spring.data.mongodb.host=localhost",
    "spring.data.mongodb.port=27017",
    "spring.data.mongodb.database=testdb",
    "spring.mongodb.embedded.version=6.0.1"
})
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
public class TestConfig {
} 