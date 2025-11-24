package com.carry_guide.carry_guide_admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${app.upload.folder.driver}")
    private String driverFolder;

    public String saveFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return null;
            }

            // Create directory if not exists
            Files.createDirectories(Paths.get(driverFolder));

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = driverFolder + "/" + fileName;

            file.transferTo(new File(filePath));

            // Return static URL path (example)
            return "/upload/driver/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }
}
