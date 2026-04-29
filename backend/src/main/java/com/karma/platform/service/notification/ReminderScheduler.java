package com.karma.platform.service.notification;

import com.karma.platform.model.ReminderType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "karma.schedulers.reminder-enabled", havingValue = "true", matchIfMissing = true)
public class ReminderScheduler {

    private final PersistentReminderService reminderService;

    public ReminderScheduler(PersistentReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "${karma.schedulers.reminder-daily-cron:0 0 0 * * *}")
    public void sendSevenDayReminders() {
        reminderService.sendReminders(ReminderType.SEVEN_DAYS);
    }

    @Scheduled(cron = "${karma.schedulers.reminder-daily-cron:0 0 0 * * *}")
    public void sendOneDayReminders() {
        reminderService.sendReminders(ReminderType.ONE_DAY);
    }

    @Scheduled(cron = "${karma.schedulers.reminder-hourly-cron:0 0 * * * *}")
    public void sendTwoHourReminders() {
        reminderService.sendReminders(ReminderType.TWO_HOURS);
    }
}
