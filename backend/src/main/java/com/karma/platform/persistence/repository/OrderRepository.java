package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByUserIdOrderByPurchasedAtDesc(String userId);

    List<OrderEntity> findByEventIdIn(List<String> eventIds);
}
