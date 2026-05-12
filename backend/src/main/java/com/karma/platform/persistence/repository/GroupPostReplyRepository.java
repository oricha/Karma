package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.GroupPostReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupPostReplyRepository extends JpaRepository<GroupPostReplyEntity, String> {

    List<GroupPostReplyEntity> findByPostIdOrderByCreatedAtAsc(String postId);
}
