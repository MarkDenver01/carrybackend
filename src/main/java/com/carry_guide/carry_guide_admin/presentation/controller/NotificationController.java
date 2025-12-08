package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.service.NotificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    NotificationTokenService notificationTokenService;

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, String> body) {

        String token = body.get("token");
        String platform = body.getOrDefault("platform", "WEB");

        Long customerId = null;
        if (body.containsKey("customerId") && body.get("customerId") != null) {
            customerId = Long.parseLong(body.get("customerId"));
        } else if (body.containsKey("driverId") && body.get("driverId") != null) {
            customerId = Long.parseLong(body.get("driverId"));
        }

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Missing token");
        }

        notificationTokenService.saveToken(customerId, token, platform);

        return ResponseEntity.ok("✅ Token saved / updated");
    }
}
