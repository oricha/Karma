package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.OrganizerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfileEntity, String> {

    Optional<OrganizerProfileEntity> findByUserId(String userId);

    Optional<OrganizerProfileEntity> findBySlug(String slug);
}
