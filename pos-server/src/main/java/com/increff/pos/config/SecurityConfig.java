package com.increff.pos.config;

import com.increff.pos.constants.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                )
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/auth/login").permitAll()

                        // Any logged-in user
                        .requestMatchers("/auth/me").authenticated()

                        // Supervisor only
                        .requestMatchers("/auth/create-operator").hasRole(UserRole.SUPERVISOR.role())

                        // Orders accessible by both
                        .requestMatchers("/api/orders/**").hasAnyRole(UserRole.SUPERVISOR.role(), UserRole.OPERATOR.role())

                        .requestMatchers("/api/products/get-all-paginated").hasAnyRole(UserRole.SUPERVISOR.role(),UserRole.OPERATOR.role())
                        .requestMatchers("/api/clients/get-all-paginated").hasAnyRole(UserRole.SUPERVISOR.role(),UserRole.OPERATOR.role())
                        .requestMatchers("/api/clients/search").hasAnyRole(UserRole.SUPERVISOR.role(),UserRole.OPERATOR.role())
                        // Everything else supervisor only
                        .anyRequest().hasRole(UserRole.SUPERVISOR.role())
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()   // 👈 IMPORTANT
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

