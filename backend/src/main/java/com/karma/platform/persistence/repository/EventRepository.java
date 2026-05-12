package com.karma.platform.persistence.repository;

import com.karma.platform.model.EventStatus;
import com.karma.platform.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, String>, JpaSpecificationExecutor<EventEntity> {

    Optional<EventEntity> findBySlug(String slug);

    List<EventEntity> findByStatus(EventStatus status);

    List<EventEntity> findByGroupId(String groupId);

    List<EventEntity> findByOrganizerId(String organizerId);

    @Query(value = """
            select *
            from events e
            where e.status = 'PUBLISHED'
              and ST_DWithin(
                ST_SetSRID(ST_MakePoint(e.longitude, e.latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radiusMeters
              )
            order by ST_Distance(
                ST_SetSRID(ST_MakePoint(e.longitude, e.latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            ) asc
            """, nativeQuery = true)
    List<EventEntity> findNearbyPublished(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") int radiusMeters
    );
}
