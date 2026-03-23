package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.dto.UserDtos;
import com.karma.platform.model.SavedEvent;
import com.karma.platform.model.User;
import com.karma.platform.model.UserPreference;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public UserService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public UserDtos.UserResponse currentUser(String userId) {
        return apiMapper.toUser(requireUser(userId));
    }

    public UserDtos.UserResponse update(String userId, UserDtos.UpdateUserRequest request) {
        User current = requireUser(userId);
        User updated = new User(
                current.id(),
                request.email(),
                current.passwordHash(),
                request.firstName(),
                request.lastName(),
                current.avatarUrl(),
                request.bio(),
                request.phone(),
                current.role(),
                request.locale() == null ? current.locale() : request.locale(),
                current.emailVerified()
        );
        dataStore.saveUser(updated);
        return apiMapper.toUser(updated);
    }

    public UserDtos.UserPreferenceResponse getPreferences(String userId) {
        return apiMapper.toPreference(dataStore.getPreference(userId));
    }

    public UserDtos.UserPreferenceResponse updatePreferences(String userId, UserDtos.UpdatePreferenceRequest request) {
        UserPreference updated = new UserPreference(
                userId,
                request.newsletterFrequency(),
                request.reviewReminders(),
                request.preferredLocation(),
                request.latitude(),
                request.longitude(),
                request.locationRadiusKm(),
                request.themeIds()
        );
        dataStore.savePreference(updated);
        return apiMapper.toPreference(updated);
    }

    public UserDtos.UserPreferenceResponse updateThemes(String userId, UserDtos.UpdateThemePreferencesRequest request) {
        UserPreference current = dataStore.getPreference(userId);
        UserPreference updated = new UserPreference(
                userId,
                current.newsletterFrequency(),
                current.reviewReminders(),
                current.preferredLocation(),
                current.latitude(),
                current.longitude(),
                current.locationRadiusKm(),
                request.themeIds()
        );
        dataStore.savePreference(updated);
        return apiMapper.toPreference(updated);
    }

    public List<EventDtos.EventResponse> savedEvents(String userId) {
        return dataStore.savedEventsByUser(userId).stream()
                .map(savedEvent -> dataStore.eventById(savedEvent.eventId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(apiMapper::toEvent)
                .toList();
    }

    public void saveEvent(String userId, String eventId) {
        if (dataStore.savedEvent(userId, eventId).isEmpty()) {
            dataStore.saveSavedEvent(new SavedEvent(dataStore.id(), userId, eventId, LocalDateTime.now()));
        }
    }

    public void unsaveEvent(String userId, String eventId) {
        dataStore.savedEvent(userId, eventId).ifPresent(savedEvent -> dataStore.deleteSavedEvent(savedEvent.id()));
    }

    public List<OrderDtos.OrderResponse> orders(String userId) {
        return dataStore.ordersByUser(userId).stream().map(apiMapper::toOrder).toList();
    }

    public List<GroupDtos.GroupResponse> myGroups(String userId) {
        return dataStore.membershipsByUser(userId).stream()
                .map(membership -> {
                    var group = dataStore.groupById(membership.groupId()).orElse(null);
                    return group == null ? null : apiMapper.toGroup(group, membership.notificationPreference().name());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public List<EventDtos.EventResponse> myEvents(String userId) {
        return dataStore.events().stream()
                .filter(event -> dataStore.userRsvp(event.id(), userId).isPresent())
                .map(apiMapper::toEvent)
                .toList();
    }

    private User requireUser(String userId) {
        return dataStore.findUserById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
