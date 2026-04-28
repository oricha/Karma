package com.karma.platform.persistence.repository;

import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.RsvpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RsvpRepository extends JpaRepository<RsvpEntity, String> {

    List<RsvpEntity> findByEventId(String eventId);

    List<RsvpEntity> findByUserId(String userId);

    Optional<RsvpEntity> findByEventIdAndUserId(String eventId, String userId);

    boolean existsByEventIdAndUserIdAndStatus(String eventId, String userId, RsvpStatus status);

    long countByEventIdAndStatus(String eventId, RsvpStatus status);
}
