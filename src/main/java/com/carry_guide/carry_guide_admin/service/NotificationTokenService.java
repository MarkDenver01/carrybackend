package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.NotificationToken;
import com.carry_guide.carry_guide_admin.repository.JpaNotificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationTokenService {

    @Autowired
    JpaNotificationTokenRepository notificationTokenRepository;

    public void saveToken(Long customerId, String token, String platform) {
        NotificationToken entity = new NotificationToken(customerId, token, platform);
        notificationTokenRepository.save(entity);
    }

    public List<NotificationToken> getAndroidTokensForCustomer(Long customerId) {
        return notificationTokenRepository.findByCustomerId(customerId);
    }

    public List<NotificationToken> getAdminWebTokens() {
        return notificationTokenRepository.findByPlatform("WEB");
    }
}
