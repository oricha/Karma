package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.BlogPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPostEntity, String> {

    Optional<BlogPostEntity> findBySlug(String slug);

    List<BlogPostEntity> findByPublishedTrueOrderByPublishedAtDesc();

    List<BlogPostEntity> findTop3ByPublishedTrueAndFeaturedTrueOrderByPublishedAtDesc();

    boolean existsBySlug(String slug);
}
