package com.karma.platform.repository;

import com.karma.platform.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    Optional<BlogPost> findBySlug(String slug);

    Page<BlogPost> findByPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    List<BlogPost> findByFeaturedTrueAndPublishedTrue();

    List<BlogPost> findByPublishedTrue();

    Page<BlogPost> findByPublishedTrue(Pageable pageable);

    boolean existsBySlug(String slug);

    long countByPublishedTrue();
}
