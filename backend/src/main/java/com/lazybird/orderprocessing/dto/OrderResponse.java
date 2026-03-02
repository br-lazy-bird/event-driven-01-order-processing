package com.lazybird.orderprocessing.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String product,
        Integer quantity,
        String status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
