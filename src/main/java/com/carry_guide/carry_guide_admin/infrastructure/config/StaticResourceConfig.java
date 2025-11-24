package com.carry_guide.carry_guide_admin.infrastructure.config;

import com.carry_guide.carry_guide_admin.service.FileUploadService;
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

    @Value("${app.upload.folder}")
    private String uploadFolder;

    @Value("${app.upload.folder.driver}")
    private String uploadFolderDriver;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.debug("register - addResourceHandlers");
        registry.addResourceHandler("/upload/product/**")
                .addResourceLocations("file:" + uploadFolder + "/");

        // Driver images (photo, front ID, back ID)
        registry.addResourceHandler("/upload/drivers/**")
                .addResourceLocations("file:" + uploadFolderDriver + "/");
    }
}
