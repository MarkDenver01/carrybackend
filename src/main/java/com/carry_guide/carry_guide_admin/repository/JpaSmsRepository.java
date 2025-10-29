package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSmsRepository extends JpaRepository<SmsMessage, Long> {
    List<SmsMessage> findTop10ByStatusOrderByCreatedAtAsc(String status);
}
