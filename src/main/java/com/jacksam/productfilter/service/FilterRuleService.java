package com.jacksam.productfilter.service;

import com.jacksam.productfilter.entity.FilterRule;
import com.jacksam.productfilter.repository.FilterRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FilterRuleService {

    private final FilterRuleRepository repository;

    public FilterRuleService(FilterRuleRepository repository) {
        this.repository = repository;
    }

    public List<FilterRule> getAll() {
        return repository.findAllByOrderByRuleOrderAsc();
    }

    public FilterRule create(FilterRule rule) {
        if (rule.getRuleOrder() == 0) {
            rule.setRuleOrder((int) repository.count() + 1);
        }
        return repository.save(rule);
    }

    public FilterRule update(Long id, FilterRule rule) {
        FilterRule existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        existing.setName(rule.getName());
        existing.setDescription(rule.getDescription());
        existing.setField(rule.getField());
        existing.setOperator(rule.getOperator());
        existing.setValue(rule.getValue());
        existing.setLogicGroup(rule.getLogicGroup());
        existing.setActionType(rule.getActionType());
        existing.setActionValue(rule.getActionValue());
        existing.setEnabled(rule.isEnabled());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Map<String, String>> evaluate(FilterRule rule, Map<String, Object> product) {
        Object fieldValue = product.get(rule.getField());
        if (fieldValue == null) return List.of();

        boolean matches = switch (rule.getOperator()) {
            case "eq" -> fieldValue.toString().equalsIgnoreCase(rule.getValue());
            case "neq" -> !fieldValue.toString().equalsIgnoreCase(rule.getValue());
            case "gt" -> toDouble(fieldValue) > toDouble(rule.getValue());
            case "gte" -> toDouble(fieldValue) >= toDouble(rule.getValue());
            case "lt" -> toDouble(fieldValue) < toDouble(rule.getValue());
            case "lte" -> toDouble(fieldValue) <= toDouble(rule.getValue());
            case "contains" -> fieldValue.toString().toLowerCase().contains(rule.getValue().toLowerCase());
            case "starts" -> fieldValue.toString().toLowerCase().startsWith(rule.getValue().toLowerCase());
            case "in" -> List.of(rule.getValue().split(",")).stream()
                    .anyMatch(v -> v.trim().equalsIgnoreCase(fieldValue.toString()));
            default -> false;
        };

        if (matches) {
            return List.of(Map.of(
                    "rule", rule.getName(),
                    "action", rule.getActionType(),
                    "value", rule.getActionValue()
            ));
        }
        return List.of();
    }

    private double toDouble(Object v) {
        try { return Double.parseDouble(v.toString()); }
        catch (NumberFormatException e) { return 0; }
    }
}
