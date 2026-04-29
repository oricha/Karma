package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EmailDigestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailDigestLogRepository extends JpaRepository<EmailDigestLogEntity, String> {

    List<EmailDigestLogEntity> findByUserId(String userId);

    List<EmailDigestLogEntity> findByStatusAndLastDigestSentAtBefore(com.karma.platform.model.ReminderLogStatus status, LocalDateTime threshold);

    Optional<EmailDigestLogEntity> findTopByUserIdOrderBySentAtDesc(String userId);

    long countByStatusAndSentAtBetween(com.karma.platform.model.ReminderLogStatus status, LocalDateTime start, LocalDateTime end);
}
