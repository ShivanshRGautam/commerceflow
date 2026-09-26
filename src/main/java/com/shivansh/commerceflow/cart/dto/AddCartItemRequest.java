package com.shivansh.commerceflow.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {
}
