package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {

    List<ReviewEntity> findByEventId(String eventId);

    Optional<ReviewEntity> findByUserIdAndEventId(String userId, String eventId);

    @Query("select avg(r.rating) from ReviewEntity r where r.eventId = :eventId")
    Double averageRatingByEventId(String eventId);
}
