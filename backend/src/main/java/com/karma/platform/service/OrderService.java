package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.model.OrderStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final ApiMapper apiMapper;

    public OrderService(EventRepository eventRepository, OrderRepository orderRepository, ApiMapper apiMapper) {
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.apiMapper = apiMapper;
    }

    @Transactional
    public OrderDtos.CheckoutResponse checkout(String userId, String eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setEventId(event.getId());
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(event.getPrice() == null ? 0 : event.getPrice());
        order.setCurrency(event.getCurrency() == null ? "EUR" : event.getCurrency());
        order.setPurchasedAt(LocalDateTime.now());
        orderRepository.save(order);
        return new OrderDtos.CheckoutResponse("https://checkout.stripe.test/session/" + order.getId(), apiMapper.toOrder(order));
    }

    public OrderDtos.OrderResponse get(String id) {
        return orderRepository.findById(id)
                .map(apiMapper::toOrder)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.order-not-found", "Order not found"));
    }
}
