package com.carry_guide.carry_guide_admin.infrastructure.config;

import com.carry_guide.carry_guide_admin.model.entity.GmailToken;
import com.carry_guide.carry_guide_admin.repository.JpaGmailTokenRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import java.io.StringReader;

@Configuration
public class GmailConfig {

    @Autowired
    private JpaGmailTokenRepository tokenRepo;

    @Value("${GMAIL_CLIENT_SECRET_JSON}")
    private String clientSecretJson;

    @Bean
    @Lazy   // ⬅️ IMPORTANT: Do NOT initialize at startup
    public Gmail gmailClient() throws Exception {

        // Try to load token from DB
        GmailToken token = tokenRepo.findById(1L).orElse(null);

        // If no token, do NOT fail the whole application
        if (token == null) {
            return null; // Gmail is not linked yet
        }

        // Load client secret JSON
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new StringReader(clientSecretJson)
        );

        // Build credential using stored tokens
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientSecrets)
                .build()
                .setAccessToken(token.getAccessToken())
                .setRefreshToken(token.getRefreshToken());

        // Auto-refresh token if expired or about to expire
        if (credential.getExpiresInSeconds() != null &&
                credential.getExpiresInSeconds() <= 60) {

            boolean refreshed = credential.refreshToken();
            if (refreshed) {
                token.setAccessToken(credential.getAccessToken());
                token.setExpiresIn(credential.getExpiresInSeconds());
                token.setTokenCreatedAt(System.currentTimeMillis());
                tokenRepo.save(token);
            }
        }

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("WrapAndCarry").build();
    }
}
