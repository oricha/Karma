package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.model.Event;
import com.karma.platform.model.Order;
import com.karma.platform.model.OrderStatus;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public OrderService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public OrderDtos.CheckoutResponse checkout(String userId, String eventId) {
        Event event = dataStore.eventById(eventId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        Order order = dataStore.saveOrder(new Order(dataStore.id(), userId, event.id(), OrderStatus.PAID, event.price() == null ? 0 : event.price(), event.currency() == null ? "EUR" : event.currency(), LocalDateTime.now()));
        return new OrderDtos.CheckoutResponse("https://checkout.stripe.test/session/" + order.id(), apiMapper.toOrder(order));
    }

    public OrderDtos.OrderResponse get(String id) {
        return dataStore.orderById(id).map(apiMapper::toOrder).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
