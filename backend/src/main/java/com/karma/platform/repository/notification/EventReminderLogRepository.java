package com.karma.platform.repository.notification;

import com.karma.platform.entity.notification.EventReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventReminderLogRepository extends JpaRepository<EventReminderLog, UUID> {

    List<EventReminderLog> findByEventIdAndUserId(UUID eventId, UUID userId);

    @Query("SELECT erl FROM EventReminderLog erl WHERE erl.event.id = :eventId AND erl.user.id = :userId AND erl.reminderType = :reminderType")
    Optional<EventReminderLog> findExistingReminder(UUID eventId, UUID userId, EventReminderLog.ReminderType reminderType);

    List<EventReminderLog> findByEventId(UUID eventId);

    List<EventReminderLog> findByStatus(String status);
}
