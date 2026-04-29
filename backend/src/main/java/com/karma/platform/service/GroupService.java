package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.common.geocoding.DomainGeocodingService;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.model.GroupNotificationPreference;
import com.karma.platform.model.GroupStatus;
import com.karma.platform.persistence.entity.GroupEntity;
import com.karma.platform.persistence.entity.GroupMembershipEntity;
import com.karma.platform.persistence.entity.OrganizerProfileEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.GroupMembershipRepository;
import com.karma.platform.persistence.repository.GroupPostRepository;
import com.karma.platform.persistence.repository.GroupRepository;
import com.karma.platform.persistence.repository.OrganizerProfileRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final GroupPostRepository groupPostRepository;
    private final DomainGeocodingService domainGeocodingService;
    private final ApiMapper apiMapper;

    public GroupService(
            GroupRepository groupRepository,
            GroupMembershipRepository groupMembershipRepository,
            OrganizerProfileRepository organizerProfileRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            GroupPostRepository groupPostRepository,
            DomainGeocodingService domainGeocodingService,
            ApiMapper apiMapper
    ) {
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.groupPostRepository = groupPostRepository;
        this.domainGeocodingService = domainGeocodingService;
        this.apiMapper = apiMapper;
    }

    public List<GroupDtos.GroupResponse> list() {
        return groupRepository.findAll().stream()
                .sorted(Comparator.comparingInt(GroupEntity::getMemberCount).reversed())
                .map(apiMapper::toGroup)
                .toList();
    }

    public List<GroupDtos.GroupResponse> nearby(Double lat, Double lng, Integer radiusKm) {
        return list();
    }

    public GroupDtos.GroupDetailResponse detail(String slug) {
        GroupEntity group = groupRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.group-not-found", "Group not found"));
        var posts = groupPostRepository.findByGroupIdOrderByPinnedDescCreatedAtDesc(group.getId());
        return new GroupDtos.GroupDetailResponse(
                apiMapper.toGroup(group),
                eventRepository.findByGroupId(group.getId()).stream().map(apiMapper::toEvent).toList(),
                groupMembershipRepository.findByGroupIdAndStatus(group.getId(), "ACTIVE").stream()
                        .map(membership -> userRepository.findById(membership.getUserId()).orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .map(apiMapper::toUser)
                        .toList(),
                posts.size(),
                posts.isEmpty() || posts.getFirst().getCreatedAt() == null ? null : posts.getFirst().getCreatedAt().toString()
        );
    }

    public List<GroupDtos.GroupResponse> managedGroups(String userId) {
        String organizerId = requireOrganizer(userId).getId();
        return groupRepository.findByOrganizerId(organizerId).stream()
                .sorted(Comparator.comparing(GroupEntity::getName))
                .map(apiMapper::toGroup)
                .toList();
    }

    @Transactional
    public GroupDtos.GroupResponse createGroup(String userId, GroupDtos.UpsertGroupRequest request) {
        OrganizerProfileEntity organizer = requireOrganizer(userId);
        GroupEntity group = new GroupEntity();
        group.setId(UUID.randomUUID().toString());
        group.setOrganizerId(organizer.getId());
        applyGroupData(group, request);
        return apiMapper.toGroup(groupRepository.save(group));
    }

    @Transactional
    public GroupDtos.GroupResponse updateGroup(String userId, String groupId, GroupDtos.UpsertGroupRequest request) {
        GroupEntity group = requireManagedGroup(userId, groupId);
        applyGroupData(group, request);
        return apiMapper.toGroup(groupRepository.save(group));
    }

    @Transactional
    public void archiveGroup(String userId, String groupId) {
        GroupEntity group = requireManagedGroup(userId, groupId);
        group.setStatus(GroupStatus.ARCHIVED);
        groupRepository.save(group);
    }

    public List<GroupDtos.MembershipResponse> memberships(String userId, String groupId, String status) {
        requireManagedGroup(userId, groupId);
        List<GroupMembershipEntity> memberships = StringUtils.hasText(status)
                ? groupMembershipRepository.findByGroupIdAndStatus(groupId, status.toUpperCase(Locale.ROOT))
                : groupMembershipRepository.findByGroupId(groupId);
        return memberships.stream()
                .sorted(Comparator.comparing(GroupMembershipEntity::getJoinedAt))
                .map(this::toMembershipResponse)
                .toList();
    }

    @Transactional
    public GroupDtos.MembershipResponse approveMembership(String userId, String groupId, String memberUserId) {
        requireManagedGroup(userId, groupId);
        GroupMembershipEntity membership = requireMembership(groupId, memberUserId);
        if (!"PENDING".equals(membership.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.membership-not-pending", "Membership is not pending");
        }
        membership.setStatus("ACTIVE");
        membership.setApprovedAt(LocalDateTime.now());
        groupMembershipRepository.save(membership);
        incrementMemberCount(groupId);
        return toMembershipResponse(membership);
    }

    @Transactional
    public void rejectMembership(String userId, String groupId, String memberUserId) {
        requireManagedGroup(userId, groupId);
        GroupMembershipEntity membership = requireMembership(groupId, memberUserId);
        groupMembershipRepository.delete(membership);
    }

    @Transactional
    public void removeMember(String userId, String groupId, String memberUserId) {
        requireManagedGroup(userId, groupId);
        GroupMembershipEntity membership = requireMembership(groupId, memberUserId);
        boolean active = "ACTIVE".equals(membership.getStatus());
        groupMembershipRepository.delete(membership);
        if (active) {
            decrementMemberCount(groupId);
        }
    }

    @Transactional
    public void join(String groupId, String userId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.group-not-found", "Group not found"));
        if (groupMembershipRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            return;
        }
        GroupMembershipEntity membership = new GroupMembershipEntity();
        membership.setId(UUID.randomUUID().toString());
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRole("MEMBER");
        membership.setStatus(group.isPrivate() ? "PENDING" : "ACTIVE");
        membership.setNotificationPreference(GroupNotificationPreference.IMMEDIATE);
        membership.setJoinedAt(LocalDateTime.now());
        membership.setApprovedAt(group.isPrivate() ? null : LocalDateTime.now());
        groupMembershipRepository.save(membership);
        if (!group.isPrivate()) {
            incrementMemberCount(groupId);
        }
    }

    @Transactional
    public void leave(String groupId, String userId) {
        groupMembershipRepository.findByGroupIdAndUserId(groupId, userId).ifPresent(membership -> {
            boolean active = "ACTIVE".equals(membership.getStatus());
            groupMembershipRepository.delete(membership);
            if (active) {
                decrementMemberCount(groupId);
            }
        });
    }

    @Transactional
    public void updateNotification(String groupId, String userId, GroupNotificationPreference preference) {
        GroupMembershipEntity current = groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.membership-not-found", "Membership not found"));
        current.setNotificationPreference(preference);
        groupMembershipRepository.save(current);
    }

    private void applyGroupData(GroupEntity group, GroupDtos.UpsertGroupRequest request) {
        if (!StringUtils.hasText(request.name()) || !StringUtils.hasText(request.categoryId())
                || !StringUtils.hasText(request.city()) || !StringUtils.hasText(request.country())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        group.setName(request.name());
        group.setSlug(slugify(request.name()));
        group.setDescription(request.description());
        group.setCategoryId(request.categoryId());
        group.setBannerUrl(request.bannerUrl());
        group.setCity(request.city());
        group.setCountry(request.country());
        group.setPrivate(request.isPrivate());
        group.setStatus(group.getStatus() == null ? GroupStatus.ACTIVE : group.getStatus());
        if (group.getMemberCount() < 0) {
            group.setMemberCount(0);
        }
        try {
            var geocoded = domainGeocodingService.geocodeGroupLocation(request.city(), request.country());
            if (geocoded.isPresent()) {
                group.setLatitude(geocoded.get().latitude());
                group.setLongitude(geocoded.get().longitude());
            }
        } catch (ApiException exception) {
            if (exception.getStatus() != HttpStatus.SERVICE_UNAVAILABLE) {
                throw exception;
            }
        }
        if (group.getLatitude() == 0 && group.getLongitude() == 0) {
            group.setLatitude(0);
            group.setLongitude(0);
        }
    }

    private GroupEntity requireManagedGroup(String userId, String groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.group-not-found", "Group not found"));
        OrganizerProfileEntity organizer = requireOrganizer(userId);
        if (!group.getOrganizerId().equals(organizer.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.organizer-access-denied", "Organizer access denied");
        }
        return group;
    }

    private OrganizerProfileEntity requireOrganizer(String userId) {
        return organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.organizer-profile-not-found", "Organizer profile not found"));
    }

    private GroupMembershipEntity requireMembership(String groupId, String userId) {
        return groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.membership-not-found", "Membership not found"));
    }

    private GroupDtos.MembershipResponse toMembershipResponse(GroupMembershipEntity membership) {
        UserEntity user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
        return new GroupDtos.MembershipResponse(
                membership.getId(),
                membership.getGroupId(),
                membership.getUserId(),
                apiMapper.toUser(user),
                membership.getRole(),
                membership.getStatus(),
                membership.getNotificationPreference().name(),
                membership.getJoinedAt() == null ? null : membership.getJoinedAt().toString(),
                membership.getApprovedAt() == null ? null : membership.getApprovedAt().toString()
        );
    }

    private void incrementMemberCount(String groupId) {
        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(group.getMemberCount() + 1);
            groupRepository.save(group);
        });
    }

    private void decrementMemberCount(String groupId) {
        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            groupRepository.save(group);
        });
    }

    private String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
