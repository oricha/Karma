package com.karma.platform.service;

import com.karma.platform.dto.*;
import com.karma.platform.model.RsvpStatus;
import com.karma.platform.persistence.entity.*;
import com.karma.platform.persistence.repository.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiMapper {

    private final CategoryRepository categoryRepository;
    private final ThemeRepository themeRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final EventThemeRepository eventThemeRepository;
    private final RsvpRepository rsvpRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ApiMapper(
            CategoryRepository categoryRepository,
            ThemeRepository themeRepository,
            OrganizerProfileRepository organizerProfileRepository,
            GroupRepository groupRepository,
            EventRepository eventRepository,
            EventThemeRepository eventThemeRepository,
            RsvpRepository rsvpRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.themeRepository = themeRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.groupRepository = groupRepository;
        this.eventRepository = eventRepository;
        this.eventThemeRepository = eventThemeRepository;
        this.rsvpRepository = rsvpRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public UserDtos.UserResponse toUser(UserEntity user) {
        return new UserDtos.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getPhone(),
                user.getRole(),
                user.getLocale(),
                user.isEmailVerified()
        );
    }

    public UserDtos.UserPreferenceResponse toPreference(UserPreferenceEntity preference, List<String> themeIds) {
        return new UserDtos.UserPreferenceResponse(
                preference.getNewsletterFrequency(),
                preference.isReviewReminders(),
                preference.getPreferredLocation(),
                preference.getLatitude(),
                preference.getLongitude(),
                preference.getLocationRadiusKm(),
                themeIds
        );
    }

    public CatalogDtos.CategoryResponse toCategory(CategoryEntity category) {
        return new CatalogDtos.CategoryResponse(
                category.getId(),
                category.getNameEs(),
                category.getNameEn(),
                category.getSlug(),
                category.getDescriptionEs(),
                category.getDescriptionEn(),
                category.getImageUrl(),
                category.getEventCount()
        );
    }

    public CatalogDtos.ThemeResponse toTheme(ThemeEntity theme) {
        return new CatalogDtos.ThemeResponse(
                theme.getId(),
                theme.getCategoryId(),
                theme.getNameEs(),
                theme.getNameEn(),
                theme.getSlug()
        );
    }

    public GroupDtos.OrganizerResponse toOrganizer(OrganizerProfileEntity organizerProfile) {
        return new GroupDtos.OrganizerResponse(
                organizerProfile.getId(),
                organizerProfile.getUserId(),
                organizerProfile.getName(),
                organizerProfile.getSlug(),
                organizerProfile.getBio(),
                organizerProfile.getWebsite(),
                organizerProfile.getLogoUrl(),
                organizerProfile.isVerified()
        );
    }

    public GroupDtos.GroupResponse toGroup(GroupEntity group) {
        return toGroup(group, null);
    }

    public GroupDtos.GroupResponse toGroup(GroupEntity group, String notificationPreference) {
        OrganizerProfileEntity organizer = organizerProfileRepository.findById(group.getOrganizerId()).orElse(null);
        CategoryEntity category = categoryRepository.findById(group.getCategoryId()).orElse(null);
        int upcomingEvents = (int) eventRepository.findByGroupId(group.getId()).stream()
                .filter(event -> event.getStatus() == com.karma.platform.model.EventStatus.PUBLISHED)
                .count();
        return new GroupDtos.GroupResponse(
                group.getId(),
                group.getOrganizerId(),
                organizer == null ? null : toOrganizer(organizer),
                group.getName(),
                group.getSlug(),
                group.getDescription(),
                group.getCategoryId(),
                category == null ? null : toCategory(category),
                group.getBannerUrl(),
                group.getCity(),
                group.getCountry(),
                group.isPrivate(),
                group.getStatus().name(),
                group.getMemberCount(),
                upcomingEvents,
                notificationPreference
        );
    }

    public EventDtos.EventResponse toEvent(EventEntity event) {
        return toEvent(event, reviewRepository.averageRatingByEventId(event.getId()), reviewRepository.countByEventId(event.getId()));
    }

    public EventDtos.EventResponse toEvent(EventEntity event, Double averageRating, long reviewCount) {
        OrganizerProfileEntity organizer = organizerProfileRepository.findById(event.getOrganizerId()).orElse(null);
        GroupEntity group = event.getGroupId() == null ? null : groupRepository.findById(event.getGroupId()).orElse(null);
        CategoryEntity category = categoryRepository.findById(event.getCategoryId()).orElse(null);
        List<CatalogDtos.ThemeResponse> themeResponses = eventThemeRepository.findByEventId(event.getId()).stream()
                .map(item -> themeRepository.findById(item.getThemeId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::toTheme)
                .toList();
        return new EventDtos.EventResponse(
                event.getId(),
                event.getOrganizerId(),
                organizer == null ? null : toOrganizer(organizer),
                event.getGroupId(),
                group == null ? null : toGroup(group),
                event.getTitle(),
                event.getSlug(),
                event.getDescription(),
                event.getCoverImageUrl(),
                event.getStartDate().toString(),
                event.getEndDate() == null ? null : event.getEndDate().toString(),
                event.getVenueName(),
                event.getAddress(),
                event.getCity(),
                event.getCountry(),
                event.isOnline(),
                event.isHybrid(),
                event.getOnlineUrl(),
                event.getStatus().name(),
                event.isFeatured(),
                event.getMaxAttendees(),
                Math.toIntExact(rsvpRepository.countByEventIdAndStatus(event.getId(), RsvpStatus.YES)),
                event.isFree(),
                event.getPrice(),
                event.getCurrency(),
                event.getLanguage(),
                themeResponses,
                category == null ? null : toCategory(category),
                averageRating == null ? null : Math.round(averageRating * 10.0) / 10.0,
                Math.toIntExact(reviewCount)
        );
    }

    public EventDtos.ReviewResponse toReview(ReviewEntity review) {
        UserEntity user = userRepository.findById(review.getUserId()).orElse(null);
        EventDtos.ReviewAuthorResponse author = user == null
                ? null
                : new EventDtos.ReviewAuthorResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getAvatarUrl());
        return new EventDtos.ReviewResponse(
                review.getId(),
                review.getEventId(),
                review.getUserId(),
                author,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt() == null ? null : review.getCreatedAt().toString(),
                review.getUpdatedAt() == null ? null : review.getUpdatedAt().toString()
        );
    }

    public EventDtos.RsvpResponse toRsvp(RsvpEntity rsvp) {
        return new EventDtos.RsvpResponse(
                rsvp.getId(),
                rsvp.getEventId(),
                rsvp.getUserId(),
                rsvp.getStatus().name(),
                rsvp.getWaitlistPosition(),
                rsvp.isCheckedIn()
        );
    }

    public OrderDtos.OrderResponse toOrder(OrderEntity order) {
        EventEntity event = eventRepository.findById(order.getEventId()).orElse(null);
        return new OrderDtos.OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getEventId(),
                event == null ? null : toEvent(event),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getPurchasedAt().toString()
        );
    }

    public BlogDtos.BlogPostResponse toBlogPost(BlogPostEntity blogPost) {
        return new BlogDtos.BlogPostResponse(
                blogPost.getId(),
                blogPost.getTitleEs(),
                blogPost.getTitleEn(),
                blogPost.getSlug(),
                blogPost.getExcerptEs(),
                blogPost.getExcerptEn(),
                blogPost.getCoverImageUrl(),
                blogPost.getPublishedAt().toString()
        );
    }
}
