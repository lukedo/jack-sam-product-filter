package com.jacksam.productfilter.entity;

import com.jacksam.productfilter.enums.AccessLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class UserAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt = LocalDateTime.now();

    public UserAccess() {}

    public UserAccess(Long userId, Long productId, AccessLevel accessLevel, Long grantedBy) {
        this.userId = userId;
        this.productId = productId;
        this.accessLevel = accessLevel;
        this.grantedBy = grantedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public AccessLevel getAccessLevel() { return accessLevel; }
    public void setAccessLevel(AccessLevel accessLevel) { this.accessLevel = accessLevel; }
    public Long getGrantedBy() { return grantedBy; }
    public void setGrantedBy(Long grantedBy) { this.grantedBy = grantedBy; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
}
