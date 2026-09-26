package com.shivansh.commerceflow.order.dto;

import com.shivansh.commerceflow.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status
) {}
