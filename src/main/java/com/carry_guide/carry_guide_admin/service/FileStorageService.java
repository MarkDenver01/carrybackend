package com.carry_guide.carry_guide_admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.folder.driver}")
    private String uploadDriverFolder; // ex: C:/uploads/driver

    public String saveDriverFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path folderPath = Paths.get(uploadDriverFolder);

            if (!Files.exists(folderPath))
                Files.createDirectories(folderPath);

            Path filePath = folderPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            return "/upload/driver/" + fileName; // URL returned
        } catch (Exception e) {
            throw new RuntimeException("Error saving file: " + e.getMessage());
        }
    }
}
