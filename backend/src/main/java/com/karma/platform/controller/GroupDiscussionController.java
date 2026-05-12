package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.service.discussion.GroupDiscussionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/posts")
public class GroupDiscussionController {

    private final GroupDiscussionService groupDiscussionService;
    private final CurrentUser currentUser;

    public GroupDiscussionController(GroupDiscussionService groupDiscussionService, CurrentUser currentUser) {
        this.groupDiscussionService = groupDiscussionService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<GroupDtos.GroupPostResponse> list(@PathVariable String groupId) {
        return groupDiscussionService.listPosts(groupId, currentUser.id());
    }

    @PostMapping
    public GroupDtos.GroupPostResponse create(@PathVariable String groupId, @RequestBody GroupDtos.UpsertGroupPostRequest request) {
        return groupDiscussionService.createPost(groupId, currentUser.id(), request);
    }

    @PostMapping("/{postId}/replies")
    public GroupDtos.GroupPostResponse reply(
            @PathVariable String groupId,
            @PathVariable String postId,
            @RequestBody GroupDtos.UpsertGroupPostReplyRequest request
    ) {
        return groupDiscussionService.reply(groupId, postId, currentUser.id(), request);
    }

    @DeleteMapping("/{postId}")
    public void delete(@PathVariable String groupId, @PathVariable String postId) {
        groupDiscussionService.deletePost(groupId, postId, currentUser.id());
    }

    @PutMapping("/{postId}/pin")
    public GroupDtos.GroupPostResponse pin(@PathVariable String groupId, @PathVariable String postId, @RequestParam boolean pinned) {
        return groupDiscussionService.updatePinned(groupId, postId, currentUser.id(), pinned);
    }
}
