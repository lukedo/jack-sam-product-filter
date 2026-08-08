package com.jacksam.productfilter.service;

import com.jacksam.productfilter.entity.AuditLog;
import com.jacksam.productfilter.enums.AuditAction;
import com.jacksam.productfilter.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks private AuditService auditService;

    @Test
    void log_savesAuditLog() {
        auditService.log(1L, AuditAction.CREATED, "PRODUCT", 5L, "Created product");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogs_withUserId_delegatesToFilteredQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(1L, pageable))
                .thenReturn(Page.empty());

        Page<AuditLog> result = auditService.getAuditLogs(1L, pageable);

        assertThat(result).isEmpty();
        verify(auditLogRepository).findByUserIdOrderByTimestampDesc(1L, pageable);
    }

    @Test
    void getAuditLogs_withoutUserId_returnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<AuditLog> result = auditService.getAuditLogs(null, pageable);

        assertThat(result).isEmpty();
        verify(auditLogRepository).findAll(pageable);
    }
}
