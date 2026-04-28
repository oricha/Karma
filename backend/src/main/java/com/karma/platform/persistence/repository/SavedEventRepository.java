package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.SavedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedEventRepository extends JpaRepository<SavedEventEntity, String> {

    List<SavedEventEntity> findByUserIdOrderBySavedAtDesc(String userId);

    Optional<SavedEventEntity> findByUserIdAndEventId(String userId, String eventId);
}
