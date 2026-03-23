package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.model.Group;
import com.karma.platform.model.GroupMembership;
import com.karma.platform.model.GroupNotificationPreference;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class GroupService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public GroupService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public List<GroupDtos.GroupResponse> list() {
        return dataStore.groups().stream()
                .sorted(Comparator.comparingInt(Group::memberCount).reversed())
                .map(apiMapper::toGroup)
                .toList();
    }

    public List<GroupDtos.GroupResponse> nearby(Double lat, Double lng, Integer radiusKm) {
        return list();
    }

    public GroupDtos.GroupDetailResponse detail(String slug) {
        Group group = dataStore.groupBySlug(slug).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
        return new GroupDtos.GroupDetailResponse(
                apiMapper.toGroup(group),
                dataStore.eventsByGroup(group.id()).stream().map(apiMapper::toEvent).toList(),
                dataStore.membershipsByGroup(group.id()).stream()
                        .map(membership -> dataStore.findUserById(membership.userId()).orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .map(apiMapper::toUser)
                        .toList()
        );
    }

    public void join(String groupId, String userId) {
        dataStore.membership(groupId, userId).orElseGet(() -> dataStore.saveMembership(new GroupMembership(dataStore.id(), groupId, userId, "MEMBER", "ACTIVE", GroupNotificationPreference.IMMEDIATE)));
    }

    public void leave(String groupId, String userId) {
        dataStore.membership(groupId, userId).ifPresent(membership -> dataStore.deleteMembership(membership.id()));
    }

    public void updateNotification(String groupId, String userId, GroupNotificationPreference preference) {
        GroupMembership current = dataStore.membership(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Membership not found"));
        dataStore.saveMembership(new GroupMembership(current.id(), current.groupId(), current.userId(), current.role(), current.status(), preference));
    }
}
