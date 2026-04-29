package com.karma.platform.service.discussion;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.persistence.entity.GroupEntity;
import com.karma.platform.persistence.entity.GroupMembershipEntity;
import com.karma.platform.persistence.entity.GroupPostEntity;
import com.karma.platform.persistence.entity.GroupPostReplyEntity;
import com.karma.platform.persistence.entity.OrganizerProfileEntity;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.GroupMembershipRepository;
import com.karma.platform.persistence.repository.GroupPostReplyRepository;
import com.karma.platform.persistence.repository.GroupPostRepository;
import com.karma.platform.persistence.repository.GroupRepository;
import com.karma.platform.persistence.repository.OrganizerProfileRepository;
import com.karma.platform.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class PersistentGroupDiscussionService implements GroupDiscussionService {

    private static final int MAX_PINNED_POSTS = 3;

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupPostRepository groupPostRepository;
    private final GroupPostReplyRepository groupPostReplyRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserRepository userRepository;
    private final SimpleContentSanitizer sanitizer;

    public PersistentGroupDiscussionService(
            GroupRepository groupRepository,
            GroupMembershipRepository groupMembershipRepository,
            GroupPostRepository groupPostRepository,
            GroupPostReplyRepository groupPostReplyRepository,
            OrganizerProfileRepository organizerProfileRepository,
            UserRepository userRepository,
            SimpleContentSanitizer sanitizer
    ) {
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.groupPostRepository = groupPostRepository;
        this.groupPostReplyRepository = groupPostReplyRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.userRepository = userRepository;
        this.sanitizer = sanitizer;
    }

    @Override
    public List<GroupDtos.GroupPostResponse> listPosts(String groupId, String userId) {
        requireActiveMember(groupId, userId);
        return groupPostRepository.findByGroupIdOrderByPinnedDescCreatedAtDesc(groupId).stream()
                .map(this::toPostResponse)
                .toList();
    }

    @Override
    @Transactional
    public GroupDtos.GroupPostResponse createPost(String groupId, String userId, GroupDtos.UpsertGroupPostRequest request) {
        requireActiveMember(groupId, userId);
        String content = sanitizeRequiredContent(request.content());
        GroupPostEntity post = new GroupPostEntity();
        post.setId(UUID.randomUUID().toString());
        post.setGroupId(groupId);
        post.setAuthorId(userId);
        post.setContent(content);
        post.setImageUrl(request.imageUrl());
        post.setPinned(false);
        return toPostResponse(groupPostRepository.save(post));
    }

    @Override
    @Transactional
    public GroupDtos.GroupPostResponse reply(String groupId, String postId, String userId, GroupDtos.UpsertGroupPostReplyRequest request) {
        requireActiveMember(groupId, userId);
        GroupPostEntity post = requirePost(groupId, postId);
        GroupPostReplyEntity reply = new GroupPostReplyEntity();
        reply.setId(UUID.randomUUID().toString());
        reply.setPostId(post.getId());
        reply.setAuthorId(userId);
        reply.setContent(sanitizeRequiredContent(request.content()));
        groupPostReplyRepository.save(reply);
        return toPostResponse(post);
    }

    @Override
    @Transactional
    public void deletePost(String groupId, String postId, String userId) {
        GroupPostEntity post = requirePost(groupId, postId);
        if (!post.getAuthorId().equals(userId) && !isOrganizer(groupId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.group-post-delete-forbidden", "You cannot delete this post");
        }
        groupPostReplyRepository.findByPostIdOrderByCreatedAtAsc(postId).forEach(groupPostReplyRepository::delete);
        groupPostRepository.delete(post);
    }

    @Override
    @Transactional
    public GroupDtos.GroupPostResponse updatePinned(String groupId, String postId, String userId, boolean pinned) {
        if (!isOrganizer(groupId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.group-post-pin-forbidden", "Only the organizer can pin posts");
        }
        GroupPostEntity post = requirePost(groupId, postId);
        if (pinned && !post.isPinned() && groupPostRepository.countByGroupIdAndPinnedTrue(groupId) >= MAX_PINNED_POSTS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.group-post-pin-limit", "Maximum pinned posts reached");
        }
        post.setPinned(pinned);
        return toPostResponse(groupPostRepository.save(post));
    }

    private GroupMembershipEntity requireActiveMember(String groupId, String userId) {
        return groupMembershipRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(membership -> "ACTIVE".equals(membership.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.group-discussion-membership-required", "Active membership is required"));
    }

    private GroupPostEntity requirePost(String groupId, String postId) {
        GroupPostEntity post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.group-post-not-found", "Post not found"));
        if (!post.getGroupId().equals(groupId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "error.group-post-not-found", "Post not found");
        }
        return post;
    }

    private boolean isOrganizer(String groupId, String userId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.group-not-found", "Group not found"));
        OrganizerProfileEntity organizer = organizerProfileRepository.findByUserId(userId).orElse(null);
        return organizer != null && organizer.getId().equals(group.getOrganizerId());
    }

    private String sanitizeRequiredContent(String content) {
        String sanitized = sanitizer.sanitize(content);
        if (!StringUtils.hasText(sanitized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        return sanitized;
    }

    private GroupDtos.GroupPostResponse toPostResponse(GroupPostEntity post) {
        UserEntity author = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
        List<GroupDtos.GroupPostReplyResponse> replies = groupPostReplyRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).stream()
                .map(this::toReplyResponse)
                .toList();
        return new GroupDtos.GroupPostResponse(
                post.getId(),
                post.getGroupId(),
                new GroupDtos.GroupPostAuthorResponse(author.getId(), author.getFirstName(), author.getLastName(), author.getAvatarUrl()),
                post.getContent(),
                post.getImageUrl(),
                post.isPinned(),
                replies.size(),
                post.getCreatedAt() == null ? null : post.getCreatedAt().toString(),
                post.getUpdatedAt() == null ? null : post.getUpdatedAt().toString(),
                replies
        );
    }

    private GroupDtos.GroupPostReplyResponse toReplyResponse(GroupPostReplyEntity reply) {
        UserEntity author = userRepository.findById(reply.getAuthorId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found", "User not found"));
        return new GroupDtos.GroupPostReplyResponse(
                reply.getId(),
                reply.getPostId(),
                new GroupDtos.GroupPostAuthorResponse(author.getId(), author.getFirstName(), author.getLastName(), author.getAvatarUrl()),
                reply.getContent(),
                reply.getCreatedAt() == null ? null : reply.getCreatedAt().toString()
        );
    }
}
