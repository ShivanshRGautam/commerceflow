package com.shivansh.commerceflow.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryCreateRequest(

        @NotNull(message = "Initial quantity is required")
        @PositiveOrZero(message = "Initial quantity cannot be negative")
        Integer initialQuantity

) {
}
