package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, String> {

    void deleteByExpiryDateBefore(LocalDateTime threshold);
}
