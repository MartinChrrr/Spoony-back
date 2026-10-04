package com.spoony.backend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET =
            "test-secret-key-for-jwt-tokens-in-tests-minimum-256-bits-xxxxxxxxx";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, 900000L, 604800000L);
    }

    @Test
    void should_GenerateValidAccessToken_When_UserIdProvided() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.generateAccessToken(userId);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("access");
    }

    @Test
    void should_GenerateValidRefreshToken_When_UserIdProvided() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.generateRefreshToken(userId);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("refresh");
    }

    @Test
    void should_ValidateToken_When_TokenIsValid() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void should_RejectToken_When_TokenIsExpired() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(TEST_SECRET, 1L, 1L);
        UUID userId = UUID.randomUUID();
        String token = shortLivedProvider.generateAccessToken(userId);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(shortLivedProvider.validateToken(token)).isFalse();
    }

    @Test
    void should_RejectToken_When_TokenIsMalformed() {
        assertThat(jwtTokenProvider.validateToken("not-a-valid-jwt-token")).isFalse();
    }

    @Test
    void should_RejectToken_When_TokenIsNull() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void should_RejectToken_When_SignatureIsInvalid() {
        String differentSecret =
                "different-secret-key-for-testing-signature-validation-256-bits-xx";
        JwtTokenProvider otherProvider = new JwtTokenProvider(differentSecret, 900000L, 604800000L);

        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);

        assertThat(otherProvider.validateToken(token)).isFalse();
    }

    @Test
    void should_ExtractUserId_When_TokenIsValid() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);

        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }
}
