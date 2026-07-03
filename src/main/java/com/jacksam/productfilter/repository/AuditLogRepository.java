package com.jacksam.productfilter.repository;

import com.jacksam.productfilter.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    Page<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(
            String resourceType, Long resourceId, Pageable pageable);
}
