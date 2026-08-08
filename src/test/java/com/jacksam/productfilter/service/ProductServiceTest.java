package com.jacksam.productfilter.service;

import com.jacksam.productfilter.dto.CreateProductRequest;
import com.jacksam.productfilter.dto.ProductDTO;
import com.jacksam.productfilter.dto.ProductFilterRequest;
import com.jacksam.productfilter.entity.Category;
import com.jacksam.productfilter.entity.FilterRule;
import com.jacksam.productfilter.entity.Product;
import com.jacksam.productfilter.entity.ProductAccessMetrics;
import com.jacksam.productfilter.enums.AccessLevel;
import com.jacksam.productfilter.enums.AuditAction;
import com.jacksam.productfilter.repository.CategoryRepository;
import com.jacksam.productfilter.repository.ProductAccessMetricsRepository;
import com.jacksam.productfilter.repository.ProductRepository;
import com.jacksam.productfilter.repository.UserAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserAccessRepository userAccessRepository;
    @Mock private ProductAccessMetricsRepository metricsRepository;
    @Mock private AuditService auditService;
    @Mock private FilterRuleService filterRuleService;

    @InjectMocks private ProductService productService;

    private Product product(long id, String name, BigDecimal price) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setDescription("desc of " + name);
        p.setPrice(price);
        p.setQuantity(10);
        p.setOwnerId(1L);
        Category c = new Category("Laptops", "Laptops", null);
        c.setId(1L);
        p.setCategory(c);
        return p;
    }

    private FilterRule rule(String actionType, String actionValue) {
        FilterRule r = new FilterRule();
        r.setName("r");
        r.setField("price");
        r.setOperator("gt");
        r.setRuleValue("100");
        r.setActionType(actionType);
        r.setActionValue(actionValue);
        r.setEnabled(true);
        return r;
    }

    private ProductFilterRequest filter() {
        return new ProductFilterRequest(null, null, null, null, false, null, null, "name", "asc", 0, 20);
    }

    private List<Long> mutable(Long... ids) {
        return new ArrayList<>(List.of(ids));
    }

    // ─── getAccessibleProductIds ───────────────────────────

    @Test
    void getAccessibleProductIds_combinesOwnedAndGranted_distinct() {
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(1L, 2L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of(2L, 3L));

        List<Long> ids = productService.getAccessibleProductIds(1L);

        assertThat(ids).containsExactly(1L, 2L, 3L);
    }

    @Test
    void getAccessibleProductIds_noAccess_returnsEmpty() {
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable());
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());

        assertThat(productService.getAccessibleProductIds(1L)).isEmpty();
    }

    // ─── getAccessibleProducts + rules ─────────────────────

    @Test
    void getAccessibleProducts_noRules_passthrough() {
        Product p = product(1L, "MacBook", new BigDecimal("2499.99"));
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        when(productRepository.findAccessibleByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(filterRuleService.getEnabled()).thenReturn(List.of());

        Page<ProductDTO> result = productService.getAccessibleProducts(1L, filter());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("MacBook");
        assertThat(result.getContent().get(0).tags()).isEmpty();
    }

    @Test
    void getAccessibleProducts_tagRule_addsTag() {
        Product p = product(1L, "MacBook", new BigDecimal("2499.99"));
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        FilterRule tagRule = rule("TAG", "premium");
        when(productRepository.findAccessibleByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(filterRuleService.getEnabled()).thenReturn(List.of(tagRule));
        when(filterRuleService.evaluate(eq(tagRule), any())).thenReturn(
                List.of(Map.of("rule", "r", "action", "TAG", "value", "premium")));

        Page<ProductDTO> result = productService.getAccessibleProducts(1L, filter());

        assertThat(result.getContent().get(0).tags()).containsExactly("premium");
    }

    @Test
    void getAccessibleProducts_flagRule_addsFlaggedTag() {
        Product p = product(1L, "MacBook", new BigDecimal("2499.99"));
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        FilterRule flagRule = rule("FLAG", "low-stock");
        when(productRepository.findAccessibleByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(filterRuleService.getEnabled()).thenReturn(List.of(flagRule));
        when(filterRuleService.evaluate(eq(flagRule), any())).thenReturn(
                List.of(Map.of("rule", "r", "action", "FLAG", "value", "low-stock")));

        Page<ProductDTO> result = productService.getAccessibleProducts(1L, filter());

        assertThat(result.getContent().get(0).tags()).containsExactly("FLAG:low-stock");
    }

    @Test
    void getAccessibleProducts_hideRule_removesProduct() {
        Product p = product(1L, "MacBook", new BigDecimal("2499.99"));
        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        FilterRule hideRule = rule("HIDE", "blocked");
        when(productRepository.findAccessibleByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(filterRuleService.getEnabled()).thenReturn(List.of(hideRule));
        when(filterRuleService.evaluate(eq(hideRule), any())).thenReturn(
                List.of(Map.of("rule", "r", "action", "HIDE", "value", "blocked")));

        Page<ProductDTO> result = productService.getAccessibleProducts(1L, filter());

        assertThat(result.getContent()).isEmpty();
    }

    // ─── getProduct ────────────────────────────────────────

    @Test
    void getProduct_noAccess_throwsSecurityException() {
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(2L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> productService.getProduct(1L, 1L))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void getProduct_notFound_throwsRuntimeException() {
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(1L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void getProduct_success_tracksMetricsAndAudits() {
        Product p = product(1L, "MacBook", new BigDecimal("2499.99"));
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(1L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(metricsRepository.findByProductIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());
        when(filterRuleService.getEnabled()).thenReturn(List.of());

        ProductDTO dto = productService.getProduct(1L, 1L);

        assertThat(dto.id()).isEqualTo(1L);
        verify(metricsRepository).save(any(ProductAccessMetrics.class));
        verify(auditService).log(eq(1L), eq(AuditAction.VIEWED), eq("PRODUCT"), eq(1L), eq(null));
    }

    // ─── createProduct ─────────────────────────────────────

    @Test
    void createProduct_setsOwnerAndAudits() {
        CreateProductRequest req = new CreateProductRequest(
                "New Gadget", "desc", new BigDecimal("99.99"), 5, null, null);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product saved = inv.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        ProductDTO dto = productService.createProduct(1L, req);

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.name()).isEqualTo("New Gadget");
        assertThat(dto.ownerId()).isEqualTo(1L);
        verify(auditService).log(eq(1L), eq(AuditAction.CREATED), eq("PRODUCT"), eq(42L), any());
    }

    @Test
    void createProduct_withCategory_resolvesCategory() {
        Category cat = new Category("Laptops", "Laptops", null);
        cat.setId(1L);
        CreateProductRequest req = new CreateProductRequest(
                "New Gadget", "desc", new BigDecimal("99.99"), 5, 1L, null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product saved = inv.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        ProductDTO dto = productService.createProduct(1L, req);

        assertThat(dto.categoryId()).isEqualTo(1L);
        assertThat(dto.categoryName()).isEqualTo("Laptops");
    }

    // ─── updateProduct ─────────────────────────────────────

    @Test
    void updateProduct_noAccess_throwsSecurityException() {
        CreateProductRequest req = new CreateProductRequest(
                "Renamed", "desc", new BigDecimal("1.0"), 1, null, null);
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(2L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> productService.updateProduct(1L, 1L, req))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void updateProduct_success_updatesAndAudits() {
        Product p = product(1L, "Old Name", new BigDecimal("10.00"));
        CreateProductRequest req = new CreateProductRequest(
                "New Name", "new desc", new BigDecimal("20.00"), 7, null, null);
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(1L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenReturn(p);

        ProductDTO dto = productService.updateProduct(1L, 1L, req);

        assertThat(dto.name()).isEqualTo("New Name");
        assertThat(dto.quantity()).isEqualTo(7);
        verify(auditService).log(eq(1L), eq(AuditAction.UPDATED), eq("PRODUCT"), eq(1L), any());
    }

    // ─── deleteProduct ─────────────────────────────────────

    @Test
    void deleteProduct_noAccess_throwsSecurityException() {
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(2L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void deleteProduct_success_deletesAndAudits() {
        when(productRepository.findIdsByOwnerId(1L)).thenReturn(mutable(1L));
        when(userAccessRepository.findProductIdsByUserIdAndLevels(1L, List.of(AccessLevel.READ, AccessLevel.WRITE, AccessLevel.ADMIN)))
                .thenReturn(List.of());

        productService.deleteProduct(1L, 1L);

        verify(productRepository).deleteById(1L);
        verify(auditService).log(eq(1L), eq(AuditAction.DELETED), eq("PRODUCT"), eq(1L), any());
    }

    // ─── bulkGrantAccess / revokeAccess ────────────────────

    @Test
    void bulkGrantAccess_savesWhenMissing() {
        when(userAccessRepository.existsByUserIdAndProductId(2L, 1L)).thenReturn(false);

        productService.bulkGrantAccess(1L, List.of(2L), List.of(1L), AccessLevel.READ);

        verify(userAccessRepository).save(any());
        verify(auditService).log(eq(1L), eq(AuditAction.ACCESS_GRANTED), eq("USER_ACCESS"), eq(null), any());
    }

    @Test
    void bulkGrantAccess_skipsExisting() {
        when(userAccessRepository.existsByUserIdAndProductId(2L, 1L)).thenReturn(true);

        productService.bulkGrantAccess(1L, List.of(2L), List.of(1L), AccessLevel.READ);

        verify(userAccessRepository, never()).save(any());
        verify(auditService).log(eq(1L), eq(AuditAction.ACCESS_GRANTED), eq("USER_ACCESS"), eq(null), any());
    }

    @Test
    void revokeAccess_deletesAndAudits() {
        productService.revokeAccess(1L, 2L, 1L);

        verify(userAccessRepository).deleteByUserIdAndProductId(2L, 1L);
        verify(auditService).log(eq(1L), eq(AuditAction.ACCESS_REVOKED), eq("USER_ACCESS"), eq(1L), any());
    }
}
