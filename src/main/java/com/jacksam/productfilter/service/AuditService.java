package com.jacksam.productfilter.service;

import com.jacksam.productfilter.entity.AuditLog;
import com.jacksam.productfilter.enums.AuditAction;
import com.jacksam.productfilter.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Long userId, AuditAction action, String resourceType, Long resourceId, String details) {
        AuditLog log = new AuditLog(userId, action, resourceType, resourceId, details);
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getAuditLogs(Long userId, Pageable pageable) {
        if (userId != null) {
            return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }
}
