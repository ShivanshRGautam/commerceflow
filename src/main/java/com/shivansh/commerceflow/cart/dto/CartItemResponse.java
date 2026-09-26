package com.shivansh.commerceflow.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long itemId,
        Long productId,
        String sku,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}
