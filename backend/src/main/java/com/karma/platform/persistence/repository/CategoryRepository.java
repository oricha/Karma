package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {

    Optional<CategoryEntity> findBySlug(String slug);
}
