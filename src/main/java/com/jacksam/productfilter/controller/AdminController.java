package com.jacksam.productfilter.controller;

import com.jacksam.productfilter.dto.GrantAccessRequest;
import com.jacksam.productfilter.entity.Category;
import com.jacksam.productfilter.entity.FilterRule;
import com.jacksam.productfilter.entity.User;
import com.jacksam.productfilter.repository.CategoryRepository;
import com.jacksam.productfilter.repository.FilterRuleRepository;
import com.jacksam.productfilter.repository.UserRepository;
import com.jacksam.productfilter.service.AuditService;
import com.jacksam.productfilter.service.FilterRuleService;
import com.jacksam.productfilter.service.ProductService;
import com.jacksam.productfilter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductService productService;
    private final UserService userService;
    private final AuditService auditService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FilterRuleRepository filterRuleRepository;
    private final FilterRuleService filterRuleService;

    public AdminController(ProductService productService,
                           UserService userService,
                           AuditService auditService,
                           CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           FilterRuleRepository filterRuleRepository,
                           FilterRuleService filterRuleService) {
        this.productService = productService;
        this.userService = userService;
        this.auditService = auditService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.filterRuleRepository = filterRuleRepository;
        this.filterRuleService = filterRuleService;
    }

    // ─── User Access ───────────────────────────────────────

    @PostMapping("/user-access/bulk-grant")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> bulkGrantAccess(
            Authentication auth, @Valid @RequestBody GrantAccessRequest req) {
        Long adminId = (Long) auth.getPrincipal();
        productService.bulkGrantAccess(adminId, req.userIds(), req.productIds(), req.accessLevel());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user-access/{userId}/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> revokeAccess(
            @PathVariable Long userId, @PathVariable Long productId) {
        productService.revokeAccess(null, userId, productId);
        return ResponseEntity.ok().build();
    }

    // ─── Audit Logs ────────────────────────────────────────

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditService.getAuditLogs(userId, PageRequest.of(page, size)));
    }

    // ─── Categories ────────────────────────────────────────

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(categoryRepository.findByParentCategoryIdIsNull());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        Category c = new Category(body.get("name"), body.get("description"),
                body.get("parentCategoryId") != null
                        ? Long.parseLong(body.get("parentCategoryId")) : null);
        return ResponseEntity.ok(categoryRepository.save(c));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (body.containsKey("name")) c.setName(body.get("name"));
        if (body.containsKey("description")) c.setDescription(body.get("description"));
        if (body.containsKey("parentCategoryId")) {
            String pid = body.get("parentCategoryId");
            c.setParentCategoryId(pid != null ? Long.parseLong(pid) : null);
        }
        return ResponseEntity.ok(categoryRepository.save(c));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ─── Filter Rules ─────────────────────────────────────

    @GetMapping("/filter-rules")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getFilterRules() {
        return ResponseEntity.ok(filterRuleService.getAll());
    }

    @PostMapping("/filter-rules")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createFilterRule(@RequestBody FilterRule rule) {
        return ResponseEntity.ok(filterRuleService.create(rule));
    }

    @PutMapping("/filter-rules/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateFilterRule(@PathVariable Long id, @RequestBody FilterRule rule) {
        return ResponseEntity.ok(filterRuleService.update(id, rule));
    }

    @DeleteMapping("/filter-rules/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteFilterRule(@PathVariable Long id) {
        filterRuleService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/filter-rules/evaluate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> evaluateRules(@RequestBody EvaluateRequest req) {
        var enabledRules = filterRuleService.getAll().stream()
                .filter(FilterRule::isEnabled).toList();
        var results = enabledRules.stream()
                .map(r -> filterRuleService.evaluate(r, req.product()))
                .flatMap(List::stream)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ─── Users ─────────────────────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        var user = userService.createUser(
                req.username(), req.password(), req.email(),
                req.displayName(), req.roleName(), req.departmentId());
        return ResponseEntity.ok(user);
    }
}

record CreateUserRequest(String username, String password, String email,
                         String displayName, String roleName, Long departmentId) {}

record EvaluateRequest(java.util.Map<String, Object> product) {}
