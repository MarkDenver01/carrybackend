package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.presentation.handler.BaseController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/public")
public class FileUploadController extends BaseController {

    @Value("${app.upload.folder}")
    private String uploadFolder;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return badRequest("No file uploaded.");
            }

            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique file name
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Build full URL
            String imageUrl = baseUrl + "/uploads/products/" + fileName;


            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("fileName", fileName);

            // ✅ Use BaseController's ok() response
            return ok(response, "File uploaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            return internalServerError("File upload failed: " + e.getMessage());
        }
    }
}
