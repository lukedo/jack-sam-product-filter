package com.jacksam.productfilter.repository;

import com.jacksam.productfilter.entity.ProductAccessMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductAccessMetricsRepository extends JpaRepository<ProductAccessMetrics, Long> {
    Optional<ProductAccessMetrics> findByProductIdAndDate(Long productId, LocalDate date);
    List<ProductAccessMetrics> findByProductIdOrderByDateDesc(Long productId);
}
