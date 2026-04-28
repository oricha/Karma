package com.karma.platform.repository;

import com.karma.platform.entity.GroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, UUID> {

    Page<GroupPost> findByGroupIdOrderByIsPinnedDescCreatedAtDesc(UUID groupId, Pageable pageable);

    List<GroupPost> findByGroupId(UUID groupId);

    @Query("SELECT gp FROM GroupPost gp WHERE gp.group.id = :groupId AND gp.isPinned = true ORDER BY gp.createdAt DESC LIMIT 3")
    List<GroupPost> findFirst3ByGroupIdAndIsPinned(UUID groupId);

    List<GroupPost> findByAuthorId(UUID authorId);

    long countByGroupId(UUID groupId);
}
