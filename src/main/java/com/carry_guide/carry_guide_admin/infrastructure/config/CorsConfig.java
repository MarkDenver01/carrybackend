package com.carry_guide.carry_guide_admin.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * {@code CorsConfig} defines the CORS (Cross-Origin Resource Sharing) configuration
 * for the Spring Boot application.
 * <p>
 * It sets up allowed origins, HTTP methods, headers, and credentials sharing
 * to enable secure communication between the backend and different client applications
 * such as React frontend, Android app, or other backends.
 * </p>
 *
 * <p><b>Configuration Properties:</b></p>
 * <ul>
 *     <li><b>base.url.react</b> – Base URL for the React frontend</li>
 *     <li><b>base.url.android</b> – Base URL for the Android app</li>
 *     <li><b>base.url.backend</b> – Base URL for backend services</li>
 * </ul>
 *
 * <p><b>Allowed:</b></p>
 * <ul>
 *     <li>Origins: React, Android, Backend (defined in application properties)</li>
 *     <li>Methods: GET, POST, PUT, DELETE, OPTIONS</li>
 *     <li>Headers: All</li>
 *     <li>Credentials: Enabled (required for cookies/session)</li>
 * </ul>
 *
 * <p><b>Example usage in application.properties:</b></p>
 * <pre>
 * base.url.react=http://localhost:3000
 * base.url.android=http://10.0.2.2:8080
 * base.url.backend=http://localhost:8080
 * </pre>
 *
 * @author Nathaniel
 */
@Configuration
public class CorsConfig {

    @Value("${base.url.react}")
    private String reactBaseUrl;

    @Value("${base.url.android}")
    private String androidBaseUrl;

    @Value("${base.url.backend}")
    private String backendBaseUrl;

    /**
     * Defines the {@link CorsConfigurationSource} bean for the application.
     * This sets up the CORS rules (origins, methods, headers, and credentials).
     *
     * @return a configured {@link CorsConfigurationSource} object
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow your frontend (static domains)
        config.setAllowedOriginPatterns(List.of(
                reactBaseUrl,
                androidBaseUrl,
                backendBaseUrl,
                "https://capstone.wrapandcarry.com"
        ));
        config.setAllowedOrigins(List.of(
                "https://capstone.wrapandcarry.com",
                "https://www.wrapandcarry.com"
        ));

        // Required HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Required headers
        config.setAllowedHeaders(List.of(
                "*",
                "Last-Event-ID",
                "Cache-Control",
                "Content-Type"
        ));

        // Must be true for cookies + SSE
        config.setAllowCredentials(true);

        // Required for SSE
        config.setExposedHeaders(List.of(
                "Cache-Control",
                "Content-Type",
                "X-Accel-Buffering",
                "Connection",
                "Transfer-Encoding",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Access-Control-Allow-Headers"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}

