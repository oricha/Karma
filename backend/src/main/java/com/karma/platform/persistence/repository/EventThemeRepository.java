package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EventThemeEntity;
import com.karma.platform.persistence.entity.EventThemeEntity.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventThemeRepository extends JpaRepository<EventThemeEntity, Key> {

    List<EventThemeEntity> findByEventId(String eventId);

    void deleteByEventId(String eventId);
}
