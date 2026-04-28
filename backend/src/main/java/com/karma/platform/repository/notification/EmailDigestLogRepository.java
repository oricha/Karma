package com.karma.platform.repository.notification;

import com.karma.platform.entity.notification.EmailDigestLog;
import com.karma.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface EmailDigestLogRepository extends JpaRepository<EmailDigestLog, UUID> {

    List<EmailDigestLog> findByUserId(UUID userId);

    @Query("SELECT edl FROM EmailDigestLog edl WHERE edl.user.id = :userId AND edl.sentAt >= :since ORDER BY edl.sentAt DESC LIMIT 1")
    Optional<EmailDigestLog> findLastDigestSent(UUID userId, LocalDateTime since);

    List<EmailDigestLog> findByUserAndStatus(User user, String status);

    @Query("SELECT edl FROM EmailDigestLog edl WHERE edl.sentAt >= :startOfHour AND edl.status = 'SENT'")
    List<EmailDigestLog> findSentInHour(LocalDateTime startOfHour);
}
