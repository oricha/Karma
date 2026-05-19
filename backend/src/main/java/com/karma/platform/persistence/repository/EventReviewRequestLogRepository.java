package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EventReviewRequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventReviewRequestLogRepository extends JpaRepository<EventReviewRequestLogEntity, String> {

    Optional<EventReviewRequestLogEntity> findByEventIdAndUserId(String eventId, String userId);
}
