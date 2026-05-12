package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizers/me/groups")
public class OrganizerGroupController {

    private final GroupService groupService;
    private final CurrentUser currentUser;

    public OrganizerGroupController(GroupService groupService, CurrentUser currentUser) {
        this.groupService = groupService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<GroupDtos.GroupResponse> listManagedGroups() {
        return groupService.managedGroups(currentUser.id());
    }

    @PostMapping
    public GroupDtos.GroupResponse create(@RequestBody GroupDtos.UpsertGroupRequest request) {
        return groupService.createGroup(currentUser.id(), request);
    }

    @PutMapping("/{id}")
    public GroupDtos.GroupResponse update(@PathVariable String id, @RequestBody GroupDtos.UpsertGroupRequest request) {
        return groupService.updateGroup(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public void archive(@PathVariable String id) {
        groupService.archiveGroup(currentUser.id(), id);
    }

    @GetMapping("/{id}/memberships")
    public List<GroupDtos.MembershipResponse> memberships(@PathVariable String id, @RequestParam(required = false) String status) {
        return groupService.memberships(currentUser.id(), id, status);
    }

    @PostMapping("/{id}/members/{userId}/approve")
    public GroupDtos.MembershipResponse approve(@PathVariable String id, @PathVariable String userId) {
        return groupService.approveMembership(currentUser.id(), id, userId);
    }

    @PostMapping("/{id}/members/{userId}/reject")
    public void reject(@PathVariable String id, @PathVariable String userId) {
        groupService.rejectMembership(currentUser.id(), id, userId);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public void removeMember(@PathVariable String id, @PathVariable String userId) {
        groupService.removeMember(currentUser.id(), id, userId);
    }
}
