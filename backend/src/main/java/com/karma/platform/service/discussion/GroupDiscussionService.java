package com.karma.platform.service.discussion;

import com.karma.platform.dto.GroupDtos;

import java.util.List;

public interface GroupDiscussionService {

    List<GroupDtos.GroupPostResponse> listPosts(String groupId, String userId);

    GroupDtos.GroupPostResponse createPost(String groupId, String userId, GroupDtos.UpsertGroupPostRequest request);

    GroupDtos.GroupPostResponse reply(String groupId, String postId, String userId, GroupDtos.UpsertGroupPostReplyRequest request);

    void deletePost(String groupId, String postId, String userId);

    GroupDtos.GroupPostResponse updatePinned(String groupId, String postId, String userId, boolean pinned);
}
