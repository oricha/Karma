package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.common.geocoding.DomainGeocodingService;
import com.karma.platform.common.i18n.LocalizationService;
import com.karma.platform.common.storage.FileCategory;
import com.karma.platform.common.storage.FileStorageService;
import com.karma.platform.common.storage.FileUploadValidator;
import com.karma.platform.common.storage.StoredFile;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.dto.UserDtos;
import com.karma.platform.persistence.entity.EmailVerificationTokenEntity;
import com.karma.platform.persistence.entity.GroupEntity;
import com.karma.platform.persistence.entity.SavedEventEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.entity.UserPreferenceEntity;
import com.karma.platform.persistence.entity.UserThemePreferenceEntity;
import com.karma.platform.persistence.repository.EmailVerificationTokenRepository;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.GroupMembershipRepository;
import com.karma.platform.persistence.repository.GroupRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.repository.RsvpRepository;
import com.karma.platform.persistence.repository.SavedEventRepository;
import com.karma.platform.persistence.repository.UserPreferenceRepository;
import com.karma.platform.persistence.repository.UserRepository;
import com.karma.platform.persistence.repository.UserThemePreferenceRepository;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final FileUploadValidator fileUploadValidator;
    private final DomainGeocodingService domainGeocodingService;
    private final LocalizationService localizationService;
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
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            FileStorageService fileStorageService,
            FileUploadValidator fileUploadValidator,
            DomainGeocodingService domainGeocodingService,
            LocalizationService localizationService,
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
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.fileUploadValidator = fileUploadValidator;
        this.domainGeocodingService = domainGeocodingService;
        this.localizationService = localizationService;
        this.apiMapper = apiMapper;
    }

    public UserDtos.UserResponse currentUser(String userId) {
        return apiMapper.toUser(requireUser(userId));
    }

    @Transactional
    public UserDtos.UserResponse update(String userId, UserDtos.UpdateUserRequest request) {
        UserEntity current = requireUser(userId);
        if (!current.getEmail().equalsIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.email-change-dedicated-endpoint",
                    localizationService.message("error.email-change-dedicated-endpoint"));
        }
        current.setFirstName(request.firstName());
        current.setLastName(request.lastName());
        current.setPhone(request.phone());
        current.setBio(request.bio());
        if (StringUtils.hasText(request.locale())) {
            current.setLocale(request.locale());
        }
        return apiMapper.toUser(userRepository.save(current));
    }

    @Transactional
    public UserDtos.UserResponse uploadAvatar(String userId, MultipartFile file) {
        UserEntity current = requireUser(userId);
        Locale locale = LocaleContextHolder.getLocale();
        fileUploadValidator.validate(file, FileCategory.IMAGE, locale);
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + fileExtension(file);
        try {
            StoredFile storedFile = fileStorageService.upload(key, file.getBytes(), file.getContentType());
            current.setAvatarUrl(storedFile.url());
            return apiMapper.toUser(userRepository.save(current));
        } catch (java.io.IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "storage.file-name-required", "Unable to read uploaded file");
        }
    }

    @Transactional
    public UserDtos.ActionResponse changePassword(String userId, UserDtos.ChangePasswordRequest request) {
        UserEntity current = requireUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), current.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.current-password-invalid",
                    localizationService.message("error.current-password-invalid"));
        }
        current.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(current);
        return new UserDtos.ActionResponse(localizationService.message("user.password-updated"));
    }

    @Transactional
    public UserDtos.EmailChangeResponse changeEmail(String userId, UserDtos.ChangeEmailRequest request) {
        UserEntity current = requireUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), current.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.current-password-invalid",
                    localizationService.message("error.current-password-invalid"));
        }
        if (emailInUseByAnotherUser(request.email(), userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "error.email-exists", localizationService.message("error.email-exists"));
        }
        current.setEmail(request.email());
        current.setEmailVerified(false);
        userRepository.save(current);
        String verificationToken = createEmailVerificationToken(userId);
        return new UserDtos.EmailChangeResponse(
                localizationService.message("user.email-change-pending-verification"),
                verificationToken,
                apiMapper.toUser(current)
        );
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

        double resolvedLatitude = request.latitude();
        double resolvedLongitude = request.longitude();
        String resolvedLocation = request.preferredLocation();

        if (StringUtils.hasText(request.preferredLocation())) {
            try {
                var geocoding = domainGeocodingService.geocodePreferredLocation(request.preferredLocation());
                if (geocoding.isPresent()) {
                    resolvedLatitude = geocoding.get().latitude();
                    resolvedLongitude = geocoding.get().longitude();
                    resolvedLocation = geocoding.get().formattedAddress();
                }
            } catch (ApiException exception) {
                if (exception.getStatus() != HttpStatus.SERVICE_UNAVAILABLE) {
                    throw exception;
                }
            }
        }

        preference.setPreferredLocation(resolvedLocation);
        preference.setLatitude(resolvedLatitude);
        preference.setLongitude(resolvedLongitude);
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

    private boolean emailInUseByAnotherUser(String email, String userId) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(user -> !user.getId().equals(userId))
                .isPresent();
    }

    private String createEmailVerificationToken(String userId) {
        emailVerificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setToken(tokenValue);
        token.setUserId(userId);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusDays(2));
        emailVerificationTokenRepository.save(token);
        return tokenValue;
    }

    private String fileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.'));
        }
        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }
}
