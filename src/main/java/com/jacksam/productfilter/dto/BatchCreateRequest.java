package com.jacksam.productfilter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchCreateRequest(
        @NotEmpty @Valid List<BatchProductItem> products
) {
    public record BatchProductItem(
            String name,
            String description,
            java.math.BigDecimal price,
            Integer quantity,
            Long categoryId,
            String imageUrl
    ) {}
}
