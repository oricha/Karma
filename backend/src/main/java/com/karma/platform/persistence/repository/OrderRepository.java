package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByUserIdOrderByPurchasedAtDesc(String userId);

    List<OrderEntity> findByEventIdIn(List<String> eventIds);

    Optional<OrderEntity> findByStripeSessionId(String stripeSessionId);
}
