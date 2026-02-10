package com.pharmacy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int windowSeconds;
    private final int loginLimit;
    private final int registerLimit;
    private final int refreshLimit;

    private final Map<String, Deque<Long>> requestTimes = new ConcurrentHashMap<>();

    public RateLimitFilter(boolean enabled,
                           int windowSeconds,
                           int loginLimit,
                           int registerLimit,
                           int refreshLimit) {
        this.enabled = enabled;
        this.windowSeconds = windowSeconds;
        this.loginLimit = loginLimit;
        this.registerLimit = registerLimit;
        this.refreshLimit = refreshLimit;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/api/auth/login")
                && !path.equals("/api/auth/register")
                && !path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();
        int limit = resolveLimit(path);
        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientKey(request) + ":" + path;
        if (!isAllowed(key, limit)) {
            response.setStatus(429);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Too many requests. Please try again shortly.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path) {
        if (path.equals("/api/auth/login")) {
            return loginLimit;
        }
        if (path.equals("/api/auth/register")) {
            return registerLimit;
        }
        if (path.equals("/api/auth/refresh")) {
            return refreshLimit;
        }
        return 0;
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAllowed(String key, int limit) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        Deque<Long> deque = requestTimes.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            if (deque.size() >= limit) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }
}
