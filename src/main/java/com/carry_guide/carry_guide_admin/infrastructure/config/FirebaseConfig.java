package com.carry_guide.carry_guide_admin.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        String projectId = System.getenv("FIREBASE_PROJECT_ID");
        String privateKeyId = System.getenv("FIREBASE_PRIVATE_KEY_ID");
        String privateKey = System.getenv("FIREBASE_PRIVATE_KEY").replace("\\n", "\n");
        String clientEmail = System.getenv("FIREBASE_CLIENT_EMAIL");
        String clientId = System.getenv("FIREBASE_CLIENT_ID");

        String json = """
        {
          "type": "service_account",
          "project_id": "%s",
          "private_key_id": "%s",
          "private_key": "%s",
          "client_email": "%s",
          "client_id": "%s",
          "token_uri": "https://oauth2.googleapis.com/token"
        }
        """.formatted(projectId, privateKeyId, privateKey, clientEmail, clientId);

        InputStream serviceAccount =
                new ByteArrayInputStream(json.getBytes());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return FirebaseMessaging.getInstance();
    }
}
