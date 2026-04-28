package com.karma.platform.persistence.repository;

import com.karma.platform.model.EventStatus;
import com.karma.platform.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, String> {

    Optional<EventEntity> findBySlug(String slug);

    List<EventEntity> findByStatus(EventStatus status);

    List<EventEntity> findByGroupId(String groupId);

    List<EventEntity> findByOrganizerId(String organizerId);
}
