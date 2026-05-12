package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, String> {
}
