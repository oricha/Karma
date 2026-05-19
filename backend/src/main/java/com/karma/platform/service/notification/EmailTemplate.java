package com.karma.platform.service.notification;

public enum EmailTemplate {
    WELCOME("welcome"),
    EMAIL_VERIFICATION("email-verification"),
    PASSWORD_RESET("password-reset"),
    RSVP_CONFIRMATION("rsvp-confirmation"),
    WAITLIST_PROMOTION("waitlist-promotion"),
    ORDER_CONFIRMATION("order-confirmation"),
    EVENT_CANCELLATION("event-cancellation"),
    REVIEW_REQUEST("review-request"),
    NEW_GROUP_EVENT("new-group-event"),
    EVENT_REMINDER_7DAY("event-reminder-7day"),
    EVENT_REMINDER_1DAY("event-reminder-1day"),
    EVENT_REMINDER_2HOUR("event-reminder-2hour"),
    WEEKLY_DIGEST("weekly-digest"),
    PLATFORM_NEWS("platform-news");

    private final String templateName;

    EmailTemplate(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTemplatePathEs() {
        return "emails/" + templateName + "-es";
    }

    public String getTemplatePathEn() {
        return "emails/" + templateName + "-en";
    }

    public String getTemplatePathCa() {
        return "emails/" + templateName + "-ca";
    }
}
