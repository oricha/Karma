package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EmailDigestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailDigestLogRepository extends JpaRepository<EmailDigestLogEntity, String> {

    List<EmailDigestLogEntity> findByUserId(String userId);

    List<EmailDigestLogEntity> findByStatusAndLastDigestSentAtBefore(com.karma.platform.model.ReminderLogStatus status, LocalDateTime threshold);
}
