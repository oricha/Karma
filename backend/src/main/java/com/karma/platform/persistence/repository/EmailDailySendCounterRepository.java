package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EmailDailySendCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface EmailDailySendCounterRepository extends JpaRepository<EmailDailySendCounterEntity, LocalDate> {
}
