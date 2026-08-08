package com.jacksam.productfilter.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET =
            "jacksam-product-filter-jwt-secret-key-must-be-at-least-256-bits-long";
    private static final long EXPIRATION_MS = 86400000;

    private final JwtTokenProvider provider =
            new JwtTokenProvider(SECRET, EXPIRATION_MS);

    @Test
    void generateAndParseToken_roundTrip() {
        String token = provider.generateToken(42L, "alice", List.of("ADMIN", "MANAGER"));

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(provider.getRolesFromToken(token)).containsExactly("ADMIN", "MANAGER");
    }

    @Test
    void validateToken_rejectsTamperedToken() {
        String token = provider.generateToken(1L, "bob", List.of("VIEWER"));

        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_rejectsGarbage() {
        assertThat(provider.validateToken("not-a-jwt")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    void getUserIdFromToken_rejectsInvalidToken() {
        assertThatThrownBy(() -> provider.getUserIdFromToken("garbage"))
                .isInstanceOf(RuntimeException.class);
    }
}
