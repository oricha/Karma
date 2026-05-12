package com.karma.platform.persistence.repository;

import com.karma.platform.model.ReminderType;
import com.karma.platform.persistence.entity.EventReminderLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventReminderLogRepository extends JpaRepository<EventReminderLogEntity, String> {

    Optional<EventReminderLogEntity> findByEventIdAndUserIdAndReminderType(String eventId, String userId, ReminderType reminderType);
}
