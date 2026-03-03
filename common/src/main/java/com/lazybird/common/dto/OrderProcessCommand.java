package com.lazybird.common.dto;

import java.util.UUID;

public record OrderProcessCommand(
        UUID orderId) {
}
