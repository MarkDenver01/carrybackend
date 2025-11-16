package com.carry_guide.carry_guide_admin.infrastructure.config;

import com.carry_guide.carry_guide_admin.model.entity.GmailToken;
import com.carry_guide.carry_guide_admin.repository.JpaGmailTokenRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class GmailConfig {

    @Autowired
    JpaGmailTokenRepository gmailTokenRepository;

    @Value("${GMAIL_CLIENT_SECRET_JSON}")
    private String clientSecretJson;

    @Bean
    public Gmail gmailClient() throws Exception {

        GmailToken token = gmailTokenRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Gmail is not linked yet. Visit /gmail/auth"));

        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new StringReader(clientSecretJson)
        );

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(secrets)
                .build()
                .setAccessToken(token.getAccessToken())
                .setRefreshToken(token.getRefreshToken());

        // Auto refresh when expired
        if (credential.getExpiresInSeconds() != null
                && credential.getExpiresInSeconds() < 60) {

            boolean refreshed = credential.refreshToken();
            if (refreshed) {
                token.setAccessToken(credential.getAccessToken());
                token.setExpiresIn(credential.getExpiresInSeconds());
                token.setTokenCreatedAt(System.currentTimeMillis());
                gmailTokenRepository.save(token);
            } else {
                throw new IllegalStateException("Failed to refresh Gmail token. Please re-authorize.");
            }
        }

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("WrapAndCarry").build();
    }
}
