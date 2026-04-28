package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    void deleteByUserId(String userId);

    void deleteByExpiresAtBefore(LocalDateTime threshold);
}
