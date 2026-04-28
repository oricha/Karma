package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThemeRepository extends JpaRepository<ThemeEntity, String> {

    List<ThemeEntity> findByCategoryIdOrderBySortOrderAsc(String categoryId);
}
