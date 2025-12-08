package com.carry_guide.carry_guide_admin.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FcmNotificationService {
    @Autowired
    FirebaseMessaging firebaseMessaging;

    public void sendToToken(String token, String title, String body, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            firebaseMessaging.send(builder.build());

        } catch (Exception e) {
            System.err.println("❌ FCM send error: " + e.getMessage());
        }
    }
}
