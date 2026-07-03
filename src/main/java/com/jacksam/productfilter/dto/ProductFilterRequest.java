package com.jacksam.productfilter.dto;

import java.math.BigDecimal;

public record ProductFilterRequest(
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long categoryId,
        Boolean includeSubCategories,
        Boolean inStock,
        Boolean active,
        String sortBy,
        String order,
        int page,
        int size
) {
    public ProductFilterRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (order == null || (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")))
            order = "asc";
        if (sortBy == null || sortBy.isBlank())
            sortBy = "name";
    }
}
