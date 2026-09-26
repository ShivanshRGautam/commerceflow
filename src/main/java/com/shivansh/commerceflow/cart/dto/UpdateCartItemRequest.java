package com.shivansh.commerceflow.cart.dto;

import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(

        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {
}
