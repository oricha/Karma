package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EmailDeferredSendEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EmailDeferredSendRepository extends JpaRepository<EmailDeferredSendEntity, String> {

    List<EmailDeferredSendEntity> findBySentAtIsNullAndScheduledForLessThanEqualOrderByScheduledForAsc(
            Instant scheduledFor
    );
}
