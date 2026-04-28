package com.karma.platform.repository;

import com.karma.platform.entity.GroupPostReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupPostReplyRepository extends JpaRepository<GroupPostReply, UUID> {

    List<GroupPostReply> findByPostIdOrderByCreatedAtAsc(UUID postId);

    Page<GroupPostReply> findByPostIdOrderByCreatedAtAsc(UUID postId, Pageable pageable);

    List<GroupPostReply> findByAuthorId(UUID authorId);

    long countByPostId(UUID postId);
}
