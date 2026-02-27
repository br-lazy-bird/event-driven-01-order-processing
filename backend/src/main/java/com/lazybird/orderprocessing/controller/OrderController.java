package com.lazybird.orderprocessing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.lazybird.common.model.Order;
import com.lazybird.orderprocessing.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        orderService.clearDatabase();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/batch")
    public ResponseEntity<List<String>> triggerBatchOrders() {
        return ResponseEntity.ok(orderService.createAndPublishOrders());
    }
    
}
