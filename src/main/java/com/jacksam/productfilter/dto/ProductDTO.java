package com.jacksam.productfilter.dto;

import com.jacksam.productfilter.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        boolean active,
        String imageUrl,
        Long categoryId,
        String categoryName,
        Long ownerId,
        Long departmentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> tags
) {
    public static ProductDTO from(Product p) {
        return new ProductDTO(
                p.getId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getQuantity(), p.isActive(),
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getOwnerId(), p.getDepartmentId(),
                p.getCreatedAt(), p.getUpdatedAt(),
                List.of()
        );
    }
}
