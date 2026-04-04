package com.spoony.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;

    private final ConcurrentMap<String, RateWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        evictExpiredEntries();

        String clientIp = getClientIp(request);
        RateWindow window = windows.compute(clientIp, (key, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateWindow(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (window.count > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":\"fail\",\"data\":{\"code\":\"RATE_LIMITED\",\"message\":\"Trop de tentatives. Réessayez dans une minute.\"}}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        // Use remoteAddr only — X-Forwarded-For is handled by the reverse proxy
        // and Spring's ForwardedHeaderFilter if configured. Do not trust raw headers.
        return request.getRemoteAddr();
    }

    private void evictExpiredEntries() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, RateWindow>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, RateWindow> entry = it.next();
            if (now - entry.getValue().windowStart > WINDOW_MS) {
                it.remove();
            }
        }
    }

    private static class RateWindow {
        long windowStart;
        int count;

        RateWindow(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
