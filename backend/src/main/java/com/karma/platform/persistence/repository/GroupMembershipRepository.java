package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.GroupMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembershipEntity, String> {

    List<GroupMembershipEntity> findByUserId(String userId);

    List<GroupMembershipEntity> findByGroupId(String groupId);

    Optional<GroupMembershipEntity> findByGroupIdAndUserId(String groupId, String userId);
}
