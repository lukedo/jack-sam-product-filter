package com.jacksam.productfilter.repository;

import com.jacksam.productfilter.entity.FilterRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilterRuleRepository extends JpaRepository<FilterRule, Long> {
    List<FilterRule> findByEnabledTrueOrderByRuleOrderAsc();
    List<FilterRule> findAllByOrderByRuleOrderAsc();
}
