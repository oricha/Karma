package com.karma.platform.persistence.repository;

import com.karma.platform.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {

    List<OrderItemEntity> findByEventOrderId(String eventOrderId);
}
