package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.GroupPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupPostRepository extends JpaRepository<GroupPostEntity, String> {

    List<GroupPostEntity> findByGroupIdOrderByPinnedDescCreatedAtDesc(String groupId);

    long countByGroupIdAndPinnedTrue(String groupId);

    boolean existsByImageUrlContaining(String fragment);
}
