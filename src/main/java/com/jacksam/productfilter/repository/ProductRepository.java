package com.jacksam.productfilter.repository;

import com.jacksam.productfilter.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p WHERE
            (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:categoryId IS NULL OR p.category.id = :categoryId
                OR (:includeSub = true AND p.category.id IN (
                    SELECT c.id FROM Category c WHERE c.parentCategoryId = :categoryId
                )))
            AND (:inStock IS NULL OR (:inStock = true AND p.quantity > 0) OR (:inStock = false))
            AND (:active IS NULL OR p.active = :active)
            """)
    Page<Product> findByFilters(
            @Param("search") String search,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("includeSub") Boolean includeSub,
            @Param("inStock") Boolean inStock,
            @Param("active") Boolean active,
            Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.ownerId = :userId")
    List<Long> findIdsByOwnerId(@Param("userId") Long userId);

    @Query("SELECT p.id FROM Product p WHERE p.departmentId = :deptId")
    List<Long> findIdsByDepartmentId(@Param("deptId") Long deptId);
}
