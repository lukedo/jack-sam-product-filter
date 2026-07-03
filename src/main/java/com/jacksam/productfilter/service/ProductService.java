package com.jacksam.productfilter.service;

import com.jacksam.productfilter.dto.CreateProductRequest;
import com.jacksam.productfilter.dto.ProductDTO;
import com.jacksam.productfilter.dto.ProductFilterRequest;
import com.jacksam.productfilter.entity.Category;
import com.jacksam.productfilter.entity.Product;
import com.jacksam.productfilter.entity.ProductAccessMetrics;
import com.jacksam.productfilter.entity.UserAccess;
import com.jacksam.productfilter.enums.AccessLevel;
import com.jacksam.productfilter.enums.AuditAction;
import com.jacksam.productfilter.repository.CategoryRepository;
import com.jacksam.productfilter.repository.ProductAccessMetricsRepository;
import com.jacksam.productfilter.repository.ProductRepository;
import com.jacksam.productfilter.repository.UserAccessRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserAccessRepository userAccessRepository;
    private final ProductAccessMetricsRepository metricsRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UserAccessRepository userAccessRepository,
                          ProductAccessMetricsRepository metricsRepository,
                          AuditService auditService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userAccessRepository = userAccessRepository;
        this.metricsRepository = metricsRepository;
        this.auditService = auditService;
    }

    @Cacheable(value = "userProducts", key = "#userId")
    public List<Long> getAccessibleProductIds(Long userId) {
        List<Long> owned = productRepository.findIdsByOwnerId(userId);
        List<Long> granted = userAccessRepository.findProductIdsByUserIdAndLevels(
                userId, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN));
        owned.addAll(granted);
        return owned.stream().distinct().toList();
    }

    public Page<ProductDTO> getAccessibleProducts(Long userId, ProductFilterRequest filter) {
        List<Long> accessibleIds = getAccessibleProductIds(userId);

        Sort sort = Sort.by(
                filter.order().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.sortBy());
        PageRequest pageable = PageRequest.of(filter.page(), filter.size(), sort);

        Page<Product> products = productRepository.findByFilters(
                filter.search(), filter.minPrice(), filter.maxPrice(),
                filter.categoryId(), filter.includeSubCategories(),
                filter.inStock(), filter.active(), pageable);

        List<Product> filtered = products.getContent().stream()
                .filter(p -> accessibleIds.contains(p.getId()))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                filtered.stream().map(ProductDTO::from).toList(),
                pageable,
                products.getTotalElements());
    }

    public ProductDTO getProduct(Long userId, Long productId) {
        List<Long> accessibleIds = getAccessibleProductIds(userId);
        if (!accessibleIds.contains(productId)) {
            throw new SecurityException("Access denied to product " + productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        trackView(userId, productId);
        auditService.log(userId, AuditAction.VIEWED, "PRODUCT", productId, null);

        return ProductDTO.from(product);
    }

    @CacheEvict(value = "userProducts", key = "#userId")
    public ProductDTO createProduct(Long userId, CreateProductRequest req) {
        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId()).orElse(null);
        }

        Product product = new Product();
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setQuantity(req.quantity() != null ? req.quantity() : 0);
        product.setCategory(category);
        product.setOwnerId(userId);
        product.setImageUrl(req.imageUrl());

        product = productRepository.save(product);
        auditService.log(userId, AuditAction.CREATED, "PRODUCT", product.getId(), "Created product: " + req.name());

        return ProductDTO.from(product);
    }

    @Transactional
    public void bulkGrantAccess(Long adminId, List<Long> userIds, List<Long> productIds, AccessLevel level) {
        for (Long userId : userIds) {
            for (Long productId : productIds) {
                if (!userAccessRepository.existsByUserIdAndProductId(userId, productId)) {
                    userAccessRepository.save(new UserAccess(userId, productId, level, adminId));
                }
            }
        }
        auditService.log(adminId, AuditAction.ACCESS_GRANTED, "USER_ACCESS", null,
                "Granted " + level + " access to users " + userIds + " for products " + productIds);
    }

    @CacheEvict(value = "userProducts", key = "#userId")
    public void revokeAccess(Long adminId, Long userId, Long productId) {
        userAccessRepository.deleteByUserIdAndProductId(userId, productId);
        auditService.log(adminId, AuditAction.ACCESS_REVOKED, "USER_ACCESS", productId,
                "Revoked access for user " + userId);
    }

    private void trackView(Long userId, Long productId) {
        LocalDate today = LocalDate.now();
        ProductAccessMetrics metrics = metricsRepository
                .findByProductIdAndDate(productId, today)
                .orElse(new ProductAccessMetrics(productId, today));
        metrics.setTotalViewCount(metrics.getTotalViewCount() + 1);
        metrics.setUniqueUserCount(metrics.getUniqueUserCount() + 1);
        metricsRepository.save(metrics);
    }
}
