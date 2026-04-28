package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.TicketTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketTypeEntity, String> {

    List<TicketTypeEntity> findByEventId(String eventId);

    Optional<TicketTypeEntity> findByEventIdAndId(String eventId, String id);
}
