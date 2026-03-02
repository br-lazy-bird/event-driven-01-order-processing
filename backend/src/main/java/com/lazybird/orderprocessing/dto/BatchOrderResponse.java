package com.lazybird.orderprocessing.dto;

import java.util.List;
import java.util.UUID;

public record BatchOrderResponse(
                List<UUID> orderIds) {
}
