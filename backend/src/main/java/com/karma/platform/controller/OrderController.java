package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUser currentUser;

    public OrderController(OrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @PostMapping("/checkout")
    public OrderDtos.CheckoutResponse checkout(@RequestBody OrderDtos.CheckoutRequest request) {
        return orderService.checkout(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    public OrderDtos.OrderResponse get(@PathVariable String id) {
        return orderService.get(id);
    }
}
