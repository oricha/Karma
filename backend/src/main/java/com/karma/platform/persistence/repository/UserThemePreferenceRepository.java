package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.UserThemePreferenceEntity;
import com.karma.platform.persistence.entity.UserThemePreferenceEntity.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserThemePreferenceRepository extends JpaRepository<UserThemePreferenceEntity, Key> {

    List<UserThemePreferenceEntity> findByUserId(String userId);

    void deleteByUserId(String userId);
}
