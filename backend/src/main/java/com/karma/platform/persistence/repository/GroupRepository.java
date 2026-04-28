package com.karma.platform.persistence.repository;

import com.karma.platform.model.GroupStatus;
import com.karma.platform.persistence.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<GroupEntity, String> {

    Optional<GroupEntity> findBySlug(String slug);

    List<GroupEntity> findByOrganizerIdAndStatus(String organizerId, GroupStatus status);
}
