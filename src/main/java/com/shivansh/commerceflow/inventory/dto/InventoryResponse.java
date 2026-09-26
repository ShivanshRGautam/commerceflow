package com.shivansh.commerceflow.inventory.dto;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        int availableQuantity,
        Long version,
        LocalDateTime updatedAt
) {
}
