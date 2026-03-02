package com.lazybird.orderprocessing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.lazybird.orderprocessing.dto.BatchOrderResponse;
import com.lazybird.orderprocessing.dto.OrderResponse;
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
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        orderService.clearDatabase();
        orderService.purgeQueue();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/batch")
    public ResponseEntity<BatchOrderResponse> triggerBatchOrders() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createAndPublishOrders());
    }

}
