package com.jacksam.productfilter.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "product_access_metrics",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "date"}))
public class ProductAccessMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "total_views")
    private long totalViewCount;

    @Column(name = "total_edits")
    private long totalEditCount;

    @Column(name = "unique_users")
    private long uniqueUserCount;

    @Column(nullable = false)
    private LocalDate date;

    public ProductAccessMetrics() {}

    public ProductAccessMetrics(Long productId, LocalDate date) {
        this.productId = productId;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public long getTotalViewCount() { return totalViewCount; }
    public void setTotalViewCount(long totalViewCount) { this.totalViewCount = totalViewCount; }
    public long getTotalEditCount() { return totalEditCount; }
    public void setTotalEditCount(long totalEditCount) { this.totalEditCount = totalEditCount; }
    public long getUniqueUserCount() { return uniqueUserCount; }
    public void setUniqueUserCount(long uniqueUserCount) { this.uniqueUserCount = uniqueUserCount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
