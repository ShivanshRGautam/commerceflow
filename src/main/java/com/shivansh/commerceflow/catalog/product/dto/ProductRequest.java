package com.shivansh.commerceflow.catalog.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU cannot exceed 50 characters")
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name cannot exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.01",
                message = "Price must be at least 0.01"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Price can have at most 10 integer digits and 2 decimal places"
        )
        BigDecimal price,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl,

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId

) {
}
