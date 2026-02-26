package com.lazybird.orderprocessing.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.lazybird.common.model.Order;
import com.lazybird.orderprocessing.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
public class OrderController {

    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/api/reset")
    public ResponseEntity<Void> reset() {
        orderService.clearDatabase();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/orders/batch")
    public ResponseEntity<List<String>> postBatchOrders() {
        
        List<Order> orders = orderService.createOrders();
        List<String> orderIds = orders.stream()
                  .map(Order::getId)
                  .map(UUID::toString)
                  .collect(java.util.stream.Collectors.toList());
        orderService.publishOrders(orderIds);

        return ResponseEntity.ok(orderIds);
    }
    
}
