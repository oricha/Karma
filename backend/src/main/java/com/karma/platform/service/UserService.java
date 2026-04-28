package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.dto.UserDtos;
import com.karma.platform.persistence.entity.*;
import com.karma.platform.persistence.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserThemePreferenceRepository userThemePreferenceRepository;
    private final SavedEventRepository savedEventRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupRepository groupRepository;
    private final RsvpRepository rsvpRepository;
    private final ApiMapper apiMapper;

    public UserService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            UserThemePreferenceRepository userThemePreferenceRepository,
            SavedEventRepository savedEventRepository,
            EventRepository eventRepository,
            OrderRepository orderRepository,
            GroupMembershipRepository groupMembershipRepository,
            GroupRepository groupRepository,
            RsvpRepository rsvpRepository,
            ApiMapper apiMapper
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userThemePreferenceRepository = userThemePreferenceRepository;
        this.savedEventRepository = savedEventRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.groupRepository = groupRepository;
        this.rsvpRepository = rsvpRepository;
        this.apiMapper = apiMapper;
    }

    public UserDtos.UserResponse currentUser(String userId) {
        return apiMapper.toUser(requireUser(userId));
    }

    @Transactional
    public UserDtos.UserResponse update(String userId, UserDtos.UpdateUserRequest request) {
        UserEntity current = requireUser(userId);
        current.setEmail(request.email());
        current.setFirstName(request.firstName());
        current.setLastName(request.lastName());
        current.setPhone(request.phone());
        current.setBio(request.bio());
        if (request.locale() != null) {
            current.setLocale(request.locale());
        }
        return apiMapper.toUser(userRepository.save(current));
    }

    public UserDtos.UserPreferenceResponse getPreferences(String userId) {
        UserPreferenceEntity preference = requirePreference(userId);
        return apiMapper.toPreference(preference, themeIds(userId));
    }

    @Transactional
    public UserDtos.UserPreferenceResponse updatePreferences(String userId, UserDtos.UpdatePreferenceRequest request) {
        UserPreferenceEntity preference = requirePreference(userId);
        preference.setNewsletterFrequency(request.newsletterFrequency());
        preference.setReviewReminders(request.reviewReminders());
        preference.setPreferredLocation(request.preferredLocation());
        preference.setLatitude(request.latitude());
        preference.setLongitude(request.longitude());
        preference.setLocationRadiusKm(request.locationRadiusKm());
        userPreferenceRepository.save(preference);
        replaceThemePreferences(userId, request.themeIds());
        return apiMapper.toPreference(preference, themeIds(userId));
    }

    @Transactional
    public UserDtos.UserPreferenceResponse updateThemes(String userId, UserDtos.UpdateThemePreferencesRequest request) {
        UserPreferenceEntity preference = requirePreference(userId);
        replaceThemePreferences(userId, request.themeIds());
        return apiMapper.toPreference(preference, themeIds(userId));
    }

    public List<EventDtos.EventResponse> savedEvents(String userId) {
        return savedEventRepository.findByUserIdOrderBySavedAtDesc(userId).stream()
                .map(savedEvent -> eventRepository.findById(savedEvent.getEventId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(apiMapper::toEvent)
                .toList();
    }

    @Transactional
    public void saveEvent(String userId, String eventId) {
        if (savedEventRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            SavedEventEntity savedEvent = new SavedEventEntity();
            savedEvent.setId(UUID.randomUUID().toString());
            savedEvent.setUserId(userId);
            savedEvent.setEventId(eventId);
            savedEvent.setSavedAt(LocalDateTime.now());
            savedEventRepository.save(savedEvent);
        }
    }

    @Transactional
    public void unsaveEvent(String userId, String eventId) {
        savedEventRepository.findByUserIdAndEventId(userId, eventId).ifPresent(savedEventRepository::delete);
    }

    public List<OrderDtos.OrderResponse> orders(String userId) {
        return orderRepository.findByUserIdOrderByPurchasedAtDesc(userId).stream().map(apiMapper::toOrder).toList();
    }

    public List<GroupDtos.GroupResponse> myGroups(String userId) {
        return groupMembershipRepository.findByUserId(userId).stream()
                .map(membership -> {
                    GroupEntity group = groupRepository.findById(membership.getGroupId()).orElse(null);
                    return group == null ? null : apiMapper.toGroup(group, membership.getNotificationPreference().name());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public List<EventDtos.EventResponse> myEvents(String userId) {
        return rsvpRepository.findByUserId(userId).stream()
                .map(rsvp -> eventRepository.findById(rsvp.getEventId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(apiMapper::toEvent)
                .toList();
    }

    private UserEntity requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
    }

    private UserPreferenceEntity requirePreference(String userId) {
        return userPreferenceRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
    }

    private List<String> themeIds(String userId) {
        return userThemePreferenceRepository.findByUserId(userId).stream()
                .map(UserThemePreferenceEntity::getThemeId)
                .toList();
    }

    private void replaceThemePreferences(String userId, List<String> themeIds) {
        userThemePreferenceRepository.deleteByUserId(userId);
        if (themeIds == null) {
            return;
        }
        themeIds.forEach(themeId -> {
            UserThemePreferenceEntity preference = new UserThemePreferenceEntity();
            preference.setUserId(userId);
            preference.setThemeId(themeId);
            userThemePreferenceRepository.save(preference);
        });
    }
}
