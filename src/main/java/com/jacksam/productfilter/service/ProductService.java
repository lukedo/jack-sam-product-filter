package com.jacksam.productfilter.service;

import com.jacksam.productfilter.dto.CreateProductRequest;
import com.jacksam.productfilter.dto.ProductDTO;
import com.jacksam.productfilter.dto.ProductFilterRequest;
import com.jacksam.productfilter.entity.Category;
import com.jacksam.productfilter.entity.FilterRule;
import com.jacksam.productfilter.entity.Product;
import com.jacksam.productfilter.entity.ProductAccessMetrics;
import com.jacksam.productfilter.entity.UserAccess;
import com.jacksam.productfilter.enums.AccessLevel;
import com.jacksam.productfilter.enums.AuditAction;
import com.jacksam.productfilter.repository.CategoryRepository;
import com.jacksam.productfilter.repository.FilterRuleRepository;
import com.jacksam.productfilter.repository.ProductAccessMetricsRepository;
import com.jacksam.productfilter.repository.ProductRepository;
import com.jacksam.productfilter.repository.UserAccessRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserAccessRepository userAccessRepository;
    private final ProductAccessMetricsRepository metricsRepository;
    private final AuditService auditService;
    private final FilterRuleRepository filterRuleRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UserAccessRepository userAccessRepository,
                          ProductAccessMetricsRepository metricsRepository,
                          AuditService auditService,
                          FilterRuleRepository filterRuleRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userAccessRepository = userAccessRepository;
        this.metricsRepository = metricsRepository;
        this.auditService = auditService;
        this.filterRuleRepository = filterRuleRepository;
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

        List<FilterRule> rules = filterRuleRepository.findByEnabledTrueOrderByRuleOrderAsc();

        List<ProductDTO> dtos = applyRules(filtered, rules);

        return new PageImpl<>(dtos, pageable, products.getTotalElements());
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

        List<FilterRule> rules = filterRuleRepository.findByEnabledTrueOrderByRuleOrderAsc();
        var dtos = applyRules(List.of(product), rules);
        return dtos.isEmpty() ? ProductDTO.from(product) : dtos.get(0);
    }

    @CacheEvict(value = "userProducts", key = "#userId")
    public ProductDTO updateProduct(Long userId, Long productId, CreateProductRequest req) {
        List<Long> accessibleIds = getAccessibleProductIds(userId);
        if (!accessibleIds.contains(productId)) {
            throw new SecurityException("Access denied to product " + productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId()).orElse(null);
        }

        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setQuantity(req.quantity() != null ? req.quantity() : 0);
        product.setCategory(category);
        product.setImageUrl(req.imageUrl());

        product = productRepository.save(product);
        auditService.log(userId, AuditAction.UPDATED, "PRODUCT", product.getId(), "Updated product: " + req.name());

        return ProductDTO.from(product);
    }

    @CacheEvict(value = "userProducts", key = "#userId")
    public void deleteProduct(Long userId, Long productId) {
        List<Long> accessibleIds = getAccessibleProductIds(userId);
        if (!accessibleIds.contains(productId)) {
            throw new SecurityException("Access denied to product " + productId);
        }

        productRepository.deleteById(productId);
        auditService.log(userId, AuditAction.DELETED, "PRODUCT", productId, "Deleted product " + productId);
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

    private List<ProductDTO> applyRules(List<Product> products, List<FilterRule> rules) {
        List<ProductDTO> result = new ArrayList<>();

        for (Product p : products) {
            Map<String, Object> productMap = Map.of(
                    "id", p.getId(),
                    "name", p.getName(),
                    "description", p.getDescription() != null ? p.getDescription() : "",
                    "price", p.getPrice(),
                    "quantity", p.getQuantity(),
                    "categoryName", p.getCategory() != null ? p.getCategory().getName() : ""
            );

            List<String> tags = new ArrayList<>();
            boolean hidden = false;

            for (FilterRule rule : rules) {
                var matches = evaluate(rule, productMap);
                if (!matches.isEmpty()) {
                    switch (rule.getActionType()) {
                        case "TAG" -> tags.add(rule.getActionValue());
                        case "FLAG" -> tags.add("FLAG:" + rule.getActionValue());
                        case "HIDE" -> hidden = true;
                    }
                }
            }

            if (hidden) continue;

            ProductDTO dto = ProductDTO.from(p);
            result.add(new ProductDTO(
                    dto.id(), dto.name(), dto.description(),
                    dto.price(), dto.quantity(), dto.active(),
                    dto.imageUrl(), dto.categoryId(), dto.categoryName(),
                    dto.ownerId(), dto.departmentId(),
                    dto.createdAt(), dto.updatedAt(),
                    tags
            ));
        }

        return result;
    }

    private List<Map<String, String>> evaluate(FilterRule rule, Map<String, Object> product) {
        Object fieldValue = product.get(rule.getField());
        if (fieldValue == null) return List.of();

        boolean matches = switch (rule.getOperator()) {
            case "eq" -> fieldValue.toString().equalsIgnoreCase(rule.getRuleValue());
            case "neq" -> !fieldValue.toString().equalsIgnoreCase(rule.getRuleValue());
            case "gt" -> toDouble(fieldValue) > toDouble(rule.getRuleValue());
            case "gte" -> toDouble(fieldValue) >= toDouble(rule.getRuleValue());
            case "lt" -> toDouble(fieldValue) < toDouble(rule.getRuleValue());
            case "lte" -> toDouble(fieldValue) <= toDouble(rule.getRuleValue());
            case "contains" -> fieldValue.toString().toLowerCase().contains(rule.getRuleValue().toLowerCase());
            case "starts" -> fieldValue.toString().toLowerCase().startsWith(rule.getRuleValue().toLowerCase());
            case "in" -> List.of(rule.getRuleValue().split(",")).stream()
                    .anyMatch(v -> v.trim().equalsIgnoreCase(fieldValue.toString()));
            default -> false;
        };

        if (matches) {
            return List.of(Map.of(
                    "rule", rule.getName(),
                    "action", rule.getActionType(),
                    "value", rule.getActionValue()
            ));
        }
        return List.of();
    }

    private double toDouble(Object v) {
        try { return Double.parseDouble(v.toString()); }
        catch (NumberFormatException e) { return 0; }
    }
}
