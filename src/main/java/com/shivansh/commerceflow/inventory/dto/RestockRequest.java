package com.shivansh.commerceflow.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RestockRequest(

        @NotNull(message = "Restock quantity is required")
        @Positive(message = "Restock quantity must be positive")
        Integer quantity

) {
}
