package com.navrotskyi.trippyapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection, common for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow all requests to any endpoint
                        .requestMatchers("/**").permitAll()
                        // Any other request that doesn't match '/**' needs to be authenticated
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
