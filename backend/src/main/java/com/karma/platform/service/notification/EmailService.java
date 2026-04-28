package com.karma.platform.service.notification;

import com.karma.platform.entity.User;
import com.karma.platform.entity.Event;
import com.karma.platform.entity.Order;
import java.util.List;

public interface EmailService {

    void sendWelcomeEmail(User user);

    void sendEmailVerificationEmail(User user, String verificationCode);

    void sendPasswordResetEmail(User user, String resetToken);

    void sendRsvpConfirmationEmail(User user, Event event);

    void sendWaitlistPromotionEmail(User user, Event event);

    void sendOrderConfirmationEmail(User user, Order order);

    void sendEventCancellationEmail(User user, Event event);

    void sendReviewRequestEmail(User user, Event event);

    void sendNewEventNotificationEmail(User user, Event event, String groupName);

    void sendWeeklyDigestEmail(User user, DigestContent digest);

    void sendEventReminderEmail(User user, Event event, ReminderType reminderType);

    void sendPlatformNewsEmail(User user, List<BlogPostSummary> featuredPosts);

    public enum ReminderType {
        SEVEN_DAY,
        ONE_DAY,
        TWO_HOUR
    }

    public class DigestContent {
        public List<EventSummary> groupEvents;
        public List<EventSummary> recommendedEvents;
        public List<EventSummary> popularRegionalEvents;
    }

    public class EventSummary {
        public String eventId;
        public String title;
        public String description;
        public String startTime;
        public String location;
    }

    public class BlogPostSummary {
        public String postId;
        public String title;
        public String excerpt;
        public String slug;
        public String coverImageUrl;
    }
}
