package com.shivansh.commerceflow.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        int totalQuantity,
        BigDecimal totalAmount
) {
}
