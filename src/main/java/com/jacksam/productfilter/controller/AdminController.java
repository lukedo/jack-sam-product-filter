package com.jacksam.productfilter.controller;

import com.jacksam.productfilter.dto.GrantAccessRequest;
import com.jacksam.productfilter.service.AuditService;
import com.jacksam.productfilter.service.ProductService;
import com.jacksam.productfilter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductService productService;
    private final UserService userService;
    private final AuditService auditService;

    public AdminController(ProductService productService,
                           UserService userService,
                           AuditService auditService) {
        this.productService = productService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @PostMapping("/user-access/bulk-grant")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> bulkGrantAccess(
            Authentication auth,
            @Valid @RequestBody GrantAccessRequest req) {
        Long adminId = (Long) auth.getPrincipal();
        productService.bulkGrantAccess(adminId, req.userIds(), req.productIds(), req.accessLevel());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user-access/{userId}/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> revokeAccess(
            Authentication auth,
            @PathVariable Long userId,
            @PathVariable Long productId) {
        Long adminId = (Long) auth.getPrincipal();
        productService.revokeAccess(adminId, userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditService.getAuditLogs(userId, PageRequest.of(page, size)));
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

record CreateUserRequest(
        String username,
        String password,
        String email,
        String displayName,
        String roleName,
        Long departmentId) {}
