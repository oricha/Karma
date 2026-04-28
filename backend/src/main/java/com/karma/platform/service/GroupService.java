package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.model.GroupNotificationPreference;
import com.karma.platform.persistence.entity.GroupEntity;
import com.karma.platform.persistence.entity.GroupMembershipEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.GroupMembershipRepository;
import com.karma.platform.persistence.repository.GroupRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ApiMapper apiMapper;

    public GroupService(
            GroupRepository groupRepository,
            GroupMembershipRepository groupMembershipRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            ApiMapper apiMapper
    ) {
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
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
        return new GroupDtos.GroupDetailResponse(
                apiMapper.toGroup(group),
                eventRepository.findByGroupId(group.getId()).stream().map(apiMapper::toEvent).toList(),
                groupMembershipRepository.findByGroupId(group.getId()).stream()
                        .map(membership -> userRepository.findById(membership.getUserId()).orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .map(apiMapper::toUser)
                        .toList()
        );
    }

    @Transactional
    public void join(String groupId, String userId) {
        if (groupMembershipRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            return;
        }
        GroupMembershipEntity membership = new GroupMembershipEntity();
        membership.setId(UUID.randomUUID().toString());
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRole("MEMBER");
        membership.setStatus("ACTIVE");
        membership.setNotificationPreference(GroupNotificationPreference.IMMEDIATE);
        membership.setJoinedAt(LocalDateTime.now());
        membership.setApprovedAt(LocalDateTime.now());
        groupMembershipRepository.save(membership);
        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(group.getMemberCount() + 1);
            groupRepository.save(group);
        });
    }

    @Transactional
    public void leave(String groupId, String userId) {
        groupMembershipRepository.findByGroupIdAndUserId(groupId, userId).ifPresent(membership -> {
            groupMembershipRepository.delete(membership);
            groupRepository.findById(groupId).ifPresent(group -> {
                group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
                groupRepository.save(group);
            });
        });
    }

    @Transactional
    public void updateNotification(String groupId, String userId, GroupNotificationPreference preference) {
        GroupMembershipEntity current = groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.membership-not-found", "Membership not found"));
        current.setNotificationPreference(preference);
        groupMembershipRepository.save(current);
    }
}
