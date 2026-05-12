package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, String> {

    void deleteByExpiryDateBefore(LocalDateTime threshold);
}
