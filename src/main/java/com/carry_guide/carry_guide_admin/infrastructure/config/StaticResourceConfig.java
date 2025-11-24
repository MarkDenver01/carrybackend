package com.carry_guide.carry_guide_admin.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    @Value("${app.upload.folder.driver}")
    private String uploadFolderDriver;

    @Value("${app.upload.folder.customer}")
    private String uploadFolderCustomer;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Convert relative path (uploads/drivers) → absolute path
        Path driverUploadPath = Paths.get(uploadFolderDriver).toAbsolutePath();

        log.info("Driver Upload Path Mapped To: {}", driverUploadPath);

        registry.addResourceHandler("/upload/driver/**")
                .addResourceLocations("file:" + driverUploadPath + "/");

        registry.addResourceHandler("/upload/customer/**")
                .addResourceLocations("file:" + Paths.get(uploadFolderCustomer).toAbsolutePath() + "/");
    }
}
