package com.carry_guide.carry_guide_admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {

    @Value("${app.upload.folder}")
    private String uploadFolder;

    @Value("${app.upload.base-url}")
    private String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    public String save(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file.");
            }

            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 Created directory: {}", uploadPath);
            }

            log.info("📂 Directory exists: {}", uploadPath);
            // Generate unique file name
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fullUrl = baseUrl + "upload/product/" + fileName;
            log.info("🖼️ File saved successfully at: {}", filePath);
            log.info("🌐 Accessible via: {}", fullUrl);

            // Build full accessible URL
            return fullUrl;

        } catch (IOException e) {
            log.error("❌ Failed to store file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }
}
