package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.model.OrderStatus;
import com.karma.platform.persistence.entity.EventEntity;
import com.karma.platform.persistence.entity.OrderItemEntity;
import com.karma.platform.persistence.entity.OrderEntity;
import com.karma.platform.persistence.repository.EventRepository;
import com.karma.platform.persistence.repository.OrderItemRepository;
import com.karma.platform.persistence.repository.OrderRepository;
import com.karma.platform.persistence.entity.UserEntity;
import com.karma.platform.persistence.repository.TicketTypeRepository;
import com.karma.platform.persistence.repository.UserRepository;
import com.karma.platform.service.notification.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ApiMapper apiMapper;

    public OrderService(
            EventRepository eventRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            TicketTypeRepository ticketTypeRepository,
            PaymentService paymentService,
            UserRepository userRepository,
            EmailService emailService,
            ApiMapper apiMapper
    ) {
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.apiMapper = apiMapper;
    }

    @Transactional
    public OrderDtos.CheckoutResponse checkout(String userId, OrderDtos.CheckoutRequest request) {
        EventEntity event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.event-not-found", "Event not found"));
        if (event.isFree()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
        }
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setEventId(event.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(0);
        order.setCurrency(event.getCurrency() == null ? "EUR" : event.getCurrency());
        order.setPurchasedAt(LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItemEntity> items = createOrderItems(order, event, request.items());
        double totalAmount = items.stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();
        order.setTotalAmount(totalAmount);
        orderItemRepository.saveAll(items);

        PaymentService.CheckoutSessionResult session = paymentService.createCheckoutSession(order, event, items);
        order.setStripeSessionId(session.sessionId());
        order.setStripePaymentIntentId(session.paymentIntentId());
        order.setCheckoutUrl(session.checkoutUrl());
        orderRepository.save(order);
        return new OrderDtos.CheckoutResponse(session.sessionId(), session.checkoutUrl(), apiMapper.toOrder(order));
    }

    public OrderDtos.OrderResponse get(String id) {
        return orderRepository.findById(id)
                .map(apiMapper::toOrder)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.order-not-found", "Order not found"));
    }

    @Transactional
    public OrderDtos.OrderResponse confirmPayment(String stripeSessionId, String paymentIntentId) {
        OrderEntity order = orderRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.order-not-found", "Order not found"));
        order.setStatus(OrderStatus.PAID);
        order.setStripePaymentIntentId(paymentIntentId);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);
        orderItemRepository.findByEventOrderId(order.getId()).forEach(item -> {
            if (item.getTicketTypeId() != null) {
                ticketTypeRepository.findById(item.getTicketTypeId()).ifPresent(ticketType -> {
                    ticketType.setSoldCount(ticketType.getSoldCount() + item.getQuantity());
                    ticketTypeRepository.save(ticketType);
                });
            }
        });
        EventEntity event = eventRepository.findById(order.getEventId()).orElse(null);
        UserEntity user = userRepository.findById(order.getUserId()).orElse(null);
        if (user != null && event != null) {
            emailService.sendOrderConfirmationEmail(user, order, event);
        }
        return apiMapper.toOrder(order);
    }

    private List<OrderItemEntity> createOrderItems(OrderEntity order, EventEntity event, List<OrderDtos.CheckoutItemRequest> requests) {
        List<OrderItemEntity> items = new ArrayList<>();
        if (CollectionUtils.isEmpty(requests)) {
            OrderItemEntity item = new OrderItemEntity();
            item.setId(UUID.randomUUID().toString());
            item.setEventOrderId(order.getId());
            item.setTicketName(event.getTitle());
            item.setUnitPrice(event.getPrice() == null ? 0.0 : event.getPrice());
            item.setCurrency(event.getCurrency() == null ? "EUR" : event.getCurrency());
            item.setQuantity(1);
            items.add(item);
            return items;
        }

        for (OrderDtos.CheckoutItemRequest request : requests) {
            if (request == null || request.quantity() <= 0 || request.ticketTypeId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
            }
            var ticketType = ticketTypeRepository.findByEventIdAndId(event.getId(), request.ticketTypeId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.ticket-type-not-found", "Ticket type not found"));
            int available = ticketType.getQuantity() - ticketType.getSoldCount();
            if (request.quantity() > available) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "error.validation", "Validation error");
            }
            OrderItemEntity item = new OrderItemEntity();
            item.setId(UUID.randomUUID().toString());
            item.setEventOrderId(order.getId());
            item.setTicketTypeId(ticketType.getId());
            item.setTicketName(ticketType.getName());
            item.setUnitPrice(ticketType.getPrice());
            item.setCurrency(ticketType.getCurrency());
            item.setQuantity(request.quantity());
            items.add(item);
        }
        return items;
    }
}
