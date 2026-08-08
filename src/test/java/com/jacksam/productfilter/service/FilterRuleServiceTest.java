package com.jacksam.productfilter.service;

import com.jacksam.productfilter.entity.FilterRule;
import com.jacksam.productfilter.repository.FilterRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilterRuleServiceTest {

    private final FilterRuleRepository repository = mock(FilterRuleRepository.class);
    private FilterRuleService service;

    @BeforeEach
    void setUp() {
        service = new FilterRuleService(repository);
    }

    private Map<String, Object> product() {
        return Map.of(
                "id", 1L,
                "name", "MacBook Pro",
                "description", "Apple laptop",
                "price", 2499.99,
                "quantity", 25,
                "categoryName", "Laptops"
        );
    }

    private FilterRule rule(String field, String operator, String value, String actionType, String actionValue) {
        FilterRule r = new FilterRule();
        r.setName("test rule");
        r.setField(field);
        r.setOperator(operator);
        r.setRuleValue(value);
        r.setActionType(actionType);
        r.setActionValue(actionValue);
        return r;
    }

    @Test
    void eq_matchesCaseInsensitive() {
        FilterRule r = rule("name", "eq", "macbook pro", "TAG", "mac");
        assertThat(service.evaluate(r, product())).isNotEmpty();
    }

    @Test
    void neq_mismatch() {
        FilterRule r = rule("name", "neq", "ThinkPad", "TAG", "not-thinkpad");
        assertThat(service.evaluate(r, product())).isNotEmpty();
    }

    @Test
    void numericOperators_gt_gte_lt_lte() {
        Map<String, Object> p = product();

        assertThat(service.evaluate(rule("price", "gt", "2000", "TAG", "x"), p)).isNotEmpty();
        assertThat(service.evaluate(rule("price", "gt", "3000", "TAG", "x"), p)).isEmpty();
        assertThat(service.evaluate(rule("price", "gte", "2499.99", "TAG", "x"), p)).isNotEmpty();
        assertThat(service.evaluate(rule("quantity", "lt", "30", "TAG", "x"), p)).isNotEmpty();
        assertThat(service.evaluate(rule("quantity", "lte", "25", "TAG", "x"), p)).isNotEmpty();
    }

    @Test
    void contains_and_starts() {
        Map<String, Object> p = product();

        assertThat(service.evaluate(rule("name", "contains", "mac", "TAG", "x"), p)).isNotEmpty();
        assertThat(service.evaluate(rule("name", "contains", "thinkpad", "TAG", "x"), p)).isEmpty();
        assertThat(service.evaluate(rule("name", "starts", "mac", "TAG", "x"), p)).isNotEmpty();
        assertThat(service.evaluate(rule("name", "starts", "pro", "TAG", "x"), p)).isEmpty();
    }

    @Test
    void in_operator_withCommaList() {
        Map<String, Object> p = product();

        assertThat(service.evaluate(rule("categoryName", "in", "Phones,Laptops", "TAG", "x"), p)).isNotEmpty();
        assertThat(service.evaluate(rule("categoryName", "in", "Phones,Clothing", "TAG", "x"), p)).isEmpty();
    }

    @Test
    void unknownOperator_returnsEmpty() {
        assertThat(service.evaluate(rule("name", "bogus", "x", "TAG", "x"), product())).isEmpty();
    }

    @Test
    void nullField_returnsEmpty() {
        Map<String, Object> p = Map.of("id", 1L, "name", "MacBook Pro");
        assertThat(service.evaluate(rule("quantity", "eq", "5", "TAG", "x"), p)).isEmpty();
    }

    @Test
    void numericOperator_onNonNumericValue_doesNotThrow() {
        Map<String, Object> p = Map.of("name", "not-a-number");
        FilterRule r = rule("name", "gt", "5", "TAG", "x");
        assertThat(service.evaluate(r, p)).isEmpty();
    }

    @Test
    void getEnabled_filtersToEnabled() {
        FilterRule r1 = rule("name", "eq", "MacBook Pro", "TAG", "mac");
        r1.setEnabled(true);
        FilterRule r2 = rule("name", "eq", "MacBook Pro", "TAG", "mac");
        r2.setEnabled(false);

        when(repository.findByEnabledTrueOrderByRuleOrderAsc()).thenReturn(List.of(r1));
        when(repository.findAllByOrderByRuleOrderAsc()).thenReturn(List.of(r1, r2));

        assertThat(service.getEnabled()).containsExactly(r1);
        assertThat(service.getAll()).hasSize(2);
    }
}
