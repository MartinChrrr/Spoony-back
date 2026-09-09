package com.spoony.backend.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(
                new ObjectMapper(),
                new SimpleMeterRegistry(),
                Clock.fixed(Instant.parse("2026-09-09T08:00:00Z"), ZoneOffset.UTC),
                262_144
        );
        chain = mock(FilterChain.class);
    }

    @Test
    void should_LimitLoginByNormalizedEmail_When_AttemptsExceedTen() throws Exception {
        MockHttpServletResponse lastResponse = null;
        for (int attempt = 0; attempt < 11; attempt++) {
            lastResponse = execute("/api/v1/auth/login", "127.0.0.1",
                    "{\"email\":\"  USER@Example.com \",\"password\":\"wrong\"}");
        }

        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getStatus()).isEqualTo(429);
        assertThat(lastResponse.getHeader("Retry-After")).isEqualTo("60");
        verify(chain, times(10)).doFilter(any(), any());
    }

    @Test
    void should_NotShareIdentityBucketBetweenUsersBehindSameIp() throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            assertThat(execute("/api/v1/auth/login", "10.0.0.1",
                    "{\"email\":\"first@example.com\",\"password\":\"wrong\"}").getStatus())
                    .isEqualTo(200);
        }

        assertThat(execute("/api/v1/auth/login", "10.0.0.1",
                "{\"email\":\"second@example.com\",\"password\":\"wrong\"}").getStatus())
                .isEqualTo(200);
    }

    @Test
    void should_KeepSeparateBucketsForLoginAndRegister() throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            execute("/api/auth/login", "10.0.0.2",
                    "{\"email\":\"same@example.com\",\"password\":\"wrong\"}");
        }

        assertThat(execute("/api/v1/auth/register", "10.0.0.2",
                "{\"email\":\"same@example.com\",\"password\":\"password123\"}").getStatus())
                .isEqualTo(200);
    }

    @Test
    void should_RejectOversizedChunkedAuthBody_When_ContentLengthIsMissing() throws Exception {
        String body = "x".repeat(101);
        filter = new RateLimitFilter(
                new ObjectMapper(),
                new SimpleMeterRegistry(),
                Clock.systemUTC(),
                100
        );

        MockHttpServletResponse response = execute("/api/v1/auth/login", "127.0.0.1", body);

        assertThat(response.getStatus()).isEqualTo(413);
    }

    private MockHttpServletResponse execute(String path, String ip, String json) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(json.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);
        return response;
    }
}
