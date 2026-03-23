package com.karma.platform.service;

import com.karma.platform.dto.*;
import com.karma.platform.model.*;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiMapper {

    private final PlatformDataStore dataStore;

    public ApiMapper(PlatformDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public UserDtos.UserResponse toUser(User user) {
        return new UserDtos.UserResponse(
                user.id(),
                user.email(),
                user.firstName(),
                user.lastName(),
                user.avatarUrl(),
                user.bio(),
                user.phone(),
                user.role(),
                user.locale(),
                user.emailVerified()
        );
    }

    public UserDtos.UserPreferenceResponse toPreference(UserPreference preference) {
        return new UserDtos.UserPreferenceResponse(
                preference.newsletterFrequency(),
                preference.reviewReminders(),
                preference.preferredLocation(),
                preference.latitude(),
                preference.longitude(),
                preference.locationRadiusKm(),
                preference.themeIds()
        );
    }

    public CatalogDtos.CategoryResponse toCategory(Category category) {
        return new CatalogDtos.CategoryResponse(
                category.id(),
                category.nameEs(),
                category.nameEn(),
                category.slug(),
                category.descriptionEs(),
                category.descriptionEn(),
                category.imageUrl(),
                category.eventCount()
        );
    }

    public CatalogDtos.ThemeResponse toTheme(Theme theme) {
        return new CatalogDtos.ThemeResponse(
                theme.id(),
                theme.categoryId(),
                theme.nameEs(),
                theme.nameEn(),
                theme.slug()
        );
    }

    public GroupDtos.OrganizerResponse toOrganizer(OrganizerProfile organizerProfile) {
        return new GroupDtos.OrganizerResponse(
                organizerProfile.id(),
                organizerProfile.userId(),
                organizerProfile.name(),
                organizerProfile.slug(),
                organizerProfile.bio(),
                organizerProfile.website(),
                organizerProfile.logoUrl(),
                organizerProfile.verified()
        );
    }

    public GroupDtos.GroupResponse toGroup(Group group) {
        return toGroup(group, null);
    }

    public GroupDtos.GroupResponse toGroup(Group group, String notificationPreference) {
        OrganizerProfile organizer = dataStore.organizerById(group.organizerId());
        Category category = dataStore.categoryBySlug(dataStore.categories().stream()
                .filter(item -> item.id().equals(group.categoryId()))
                .map(Category::slug)
                .findFirst()
                .orElse("")).orElse(null);
        int upcomingEvents = (int) dataStore.eventsByGroup(group.id()).stream().filter(event -> event.status() == EventStatus.PUBLISHED).count();
        return new GroupDtos.GroupResponse(
                group.id(),
                group.organizerId(),
                organizer == null ? null : toOrganizer(organizer),
                group.name(),
                group.slug(),
                group.description(),
                group.categoryId(),
                category == null ? null : toCategory(category),
                group.bannerUrl(),
                group.city(),
                group.country(),
                group.isPrivate(),
                group.status().name(),
                group.memberCount(),
                upcomingEvents,
                notificationPreference
        );
    }

    public EventDtos.EventResponse toEvent(Event event) {
        OrganizerProfile organizer = dataStore.organizerById(event.organizerId());
        Group group = event.groupId() == null ? null : dataStore.groupById(event.groupId()).orElse(null);
        Category category = dataStore.categories().stream().filter(item -> item.id().equals(event.categoryId())).findFirst().orElse(null);
        List<CatalogDtos.ThemeResponse> themeResponses = event.themeIds().stream()
                .map(id -> dataStore.themes().stream().filter(theme -> theme.id().equals(id)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::toTheme)
                .toList();
        return new EventDtos.EventResponse(
                event.id(),
                event.organizerId(),
                organizer == null ? null : toOrganizer(organizer),
                event.groupId(),
                group == null ? null : toGroup(group),
                event.title(),
                event.slug(),
                event.description(),
                event.coverImageUrl(),
                event.startDate().toString(),
                event.endDate() == null ? null : event.endDate().toString(),
                event.venueName(),
                event.address(),
                event.city(),
                event.country(),
                event.isOnline(),
                event.isHybrid(),
                event.onlineUrl(),
                event.status().name(),
                event.featured(),
                event.maxAttendees(),
                dataStore.attendeeCount(event.id()),
                event.isFree(),
                event.price(),
                event.currency(),
                event.language(),
                themeResponses,
                category == null ? null : toCategory(category),
                4.8,
                12
        );
    }

    public EventDtos.RsvpResponse toRsvp(Rsvp rsvp) {
        return new EventDtos.RsvpResponse(
                rsvp.id(),
                rsvp.eventId(),
                rsvp.userId(),
                rsvp.status().name(),
                rsvp.waitlistPosition(),
                rsvp.checkedIn()
        );
    }

    public OrderDtos.OrderResponse toOrder(Order order) {
        Event event = dataStore.eventById(order.eventId()).orElse(null);
        return new OrderDtos.OrderResponse(
                order.id(),
                order.userId(),
                order.eventId(),
                event == null ? null : toEvent(event),
                order.status().name(),
                order.totalAmount(),
                order.currency(),
                order.purchasedAt().toString()
        );
    }

    public BlogDtos.BlogPostResponse toBlogPost(BlogPost blogPost) {
        return new BlogDtos.BlogPostResponse(
                blogPost.id(),
                blogPost.titleEs(),
                blogPost.titleEn(),
                blogPost.slug(),
                blogPost.excerptEs(),
                blogPost.excerptEn(),
                blogPost.coverImageUrl(),
                blogPost.publishedAt().toString()
        );
    }
}
