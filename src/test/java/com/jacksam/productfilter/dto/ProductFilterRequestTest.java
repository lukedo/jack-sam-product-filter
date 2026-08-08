package com.jacksam.productfilter.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFilterRequestTest {

    @Test
    void defaultSortAndOrder_appliedWhenNullOrBlank() {
        ProductFilterRequest r = new ProductFilterRequest(
                null, null, null, null, false, null, null, null, null, 0, 20);

        assertThat(r.sortBy()).isEqualTo("name");
        assertThat(r.order()).isEqualTo("asc");
    }

    @Test
    void invalidOrder_normalizedToAsc() {
        ProductFilterRequest r = new ProductFilterRequest(
                null, null, null, null, false, null, null, "name", "sideways", 0, 20);

        assertThat(r.order()).isEqualTo("asc");
    }

    @Test
    void invalidSortBy_normalizedToName() {
        ProductFilterRequest r = new ProductFilterRequest(
                null, null, null, null, false, null, null, "  ", "asc", 0, 20);

        assertThat(r.sortBy()).isEqualTo("name");
    }

    @Test
    void negativePage_normalizedToZero() {
        ProductFilterRequest r = new ProductFilterRequest(
                null, null, null, null, false, null, null, "name", "asc", -5, 20);

        assertThat(r.page()).isZero();
    }

    @Test
    void zeroOrTooLargeSize_normalizedTo20() {
        ProductFilterRequest zero = new ProductFilterRequest(
                null, null, null, null, false, null, null, "name", "asc", 0, 0);
        ProductFilterRequest negative = new ProductFilterRequest(
                null, null, null, null, false, null, null, "name", "asc", 0, -10);
        ProductFilterRequest huge = new ProductFilterRequest(
                null, null, null, null, false, null, null, "name", "asc", 0, 500);

        assertThat(zero.size()).isEqualTo(20);
        assertThat(negative.size()).isEqualTo(20);
        assertThat(huge.size()).isEqualTo(20);
    }

    @Test
    void validValues_preserved() {
        ProductFilterRequest r = new ProductFilterRequest(
                "laptop", null, null, 1L, true, true, true, "price", "desc", 3, 50);

        assertThat(r.search()).isEqualTo("laptop");
        assertThat(r.categoryId()).isEqualTo(1L);
        assertThat(r.includeSubCategories()).isTrue();
        assertThat(r.inStock()).isTrue();
        assertThat(r.active()).isTrue();
        assertThat(r.sortBy()).isEqualTo("price");
        assertThat(r.order()).isEqualTo("desc");
        assertThat(r.page()).isEqualTo(3);
        assertThat(r.size()).isEqualTo(50);
    }
}
