package com.jacksam.productfilter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String name,
        String description,
        @Positive BigDecimal price,
        Integer quantity,
        Long categoryId,
        String imageUrl
) {}
