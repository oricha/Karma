package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.dto.UserDtos;
import com.karma.platform.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final CurrentUser currentUser;

    public GroupController(GroupService groupService, CurrentUser currentUser) {
        this.groupService = groupService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<GroupDtos.GroupResponse> list() {
        return groupService.list();
    }

    @GetMapping("/nearby")
    public List<GroupDtos.GroupResponse> nearby(@RequestParam(required = false) Double lat, @RequestParam(required = false) Double lng, @RequestParam(required = false) Integer radius) {
        return groupService.nearby(lat, lng, radius);
    }

    @GetMapping("/{slug}")
    public GroupDtos.GroupDetailResponse detail(@PathVariable String slug) {
        return groupService.detail(slug);
    }

    @PostMapping("/{id}/join")
    public void join(@PathVariable String id) {
        groupService.join(id, currentUser.id());
    }

    @DeleteMapping("/{id}/leave")
    public void leave(@PathVariable String id) {
        groupService.leave(id, currentUser.id());
    }

    @PutMapping("/{id}/notifications")
    public void updateNotification(@PathVariable String id, @RequestBody UserDtos.UpdateGroupNotificationRequest request) {
        groupService.updateNotification(id, currentUser.id(), request.preference());
    }
}
