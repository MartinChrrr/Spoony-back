package com.spoony.backend.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;
    private static final int RETRY_AFTER_SECONDS = 60;

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final int maxRequestSizeBytes;
    private final ConcurrentMap<String, RateWindow> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper,
                           MeterRegistry meterRegistry,
                           Clock businessClock,
                           @Value("${app.http.max-request-size-bytes:262144}") int maxRequestSizeBytes) {
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.clock = businessClock;
        this.maxRequestSizeBytes = maxRequestSizeBytes;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        return policyFor(request.getRequestURI()) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        byte[] body = request.getInputStream().readNBytes(maxRequestSizeBytes + 1);
        if (body.length > maxRequestSizeBytes) {
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":\"fail\",\"data\":{\"code\":\"REQUEST_TOO_LARGE\",\"message\":\"Requête trop volumineuse\"}}"
            );
            return;
        }

        CachedBodyRequest wrappedRequest = new CachedBodyRequest(request, body);
        LimitPolicy policy = policyFor(request.getRequestURI());
        if (policy == null) {
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        evictExpiredEntries();
        String clientIp = request.getRemoteAddr();
        boolean ipExceeded = incrementAndCheck(
                policy.route() + ":ip:" + hash(clientIp),
                policy.ipLimit()
        );

        String identifier = extractIdentifier(body, policy.identifierField());
        boolean identityExceeded = identifier != null && incrementAndCheck(
                policy.route() + ":identity:" + hash(identifier),
                policy.identityLimit()
        );

        if (ipExceeded || identityExceeded) {
            meterRegistry.counter("spoony.auth.rate_limited", "route", policy.route()).increment();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", Integer.toString(RETRY_AFTER_SECONDS));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":\"fail\",\"data\":{\"code\":\"RATE_LIMITED\",\"message\":\"Trop de tentatives. Réessayez dans une minute.\"}}"
            );
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private LimitPolicy policyFor(String uri) {
        String route = uri.replaceFirst("^/api/(?:v1/)?auth/", "");
        return switch (route) {
            case "login" -> new LimitPolicy("login", "email", 50, 10);
            case "register" -> new LimitPolicy("register", "email", 20, 5);
            case "refresh" -> new LimitPolicy("refresh", "refreshToken", 120, 30);
            default -> null;
        };
    }

    private String extractIdentifier(byte[] body, String fieldName) {
        try {
            JsonNode value = objectMapper.readTree(body).get(fieldName);
            if (value == null || !value.isTextual() || value.textValue().isBlank()) {
                return null;
            }
            return value.textValue().trim().toLowerCase(Locale.ROOT);
        } catch (IOException ignored) {
            return null;
        }
    }

    private boolean incrementAndCheck(String key, int limit) {
        long now = clock.millis();
        RateWindow window = windows.compute(key, (ignored, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_MS) {
                return new RateWindow(now, 1);
            }
            return new RateWindow(existing.windowStart(), existing.count() + 1);
        });
        return window.count() > limit;
    }

    private void evictExpiredEntries() {
        long now = clock.millis();
        Iterator<Map.Entry<String, RateWindow>> iterator = windows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RateWindow> entry = iterator.next();
            if (now - entry.getValue().windowStart() >= WINDOW_MS) {
                iterator.remove();
            }
        }
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private record LimitPolicy(String route, String identifierField, int ipLimit, int identityLimit) {
    }

    private record RateWindow(long windowStart, int count) {
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Requests are read synchronously by Spring MVC.
                }

                @Override
                public int read() {
                    return input.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
