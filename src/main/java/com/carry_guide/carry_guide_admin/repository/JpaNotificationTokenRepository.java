package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaNotificationTokenRepository extends JpaRepository<NotificationToken, Long> {
    List<NotificationToken> findByCustomerId(Long customerId);
    List<NotificationToken> findByPlatform(String platform);
    Optional<NotificationToken> findByToken(String token);
}
