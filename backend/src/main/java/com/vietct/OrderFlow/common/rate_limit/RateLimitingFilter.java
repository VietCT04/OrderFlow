package com.vietct.OrderFlow.common.rate_limit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_ORDERS_PER_MINUTE = 20;
    private static final int WINDOW_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!"POST".equalsIgnoreCase(method)) {
            return true;
        }

        return !uri.startsWith("/orders");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String identity = resolveIdentity(request);
        long window = System.currentTimeMillis() / (WINDOW_SECONDS * 1000L);
        String key = buildKey(identity, window);

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > MAX_ORDERS_PER_MINUTE) {
            writeRateLimitResponse(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveIdentity(HttpServletRequest request) {
        String userHeader = request.getHeader("X-User-Id");
        if (userHeader != null && !userHeader.isBlank()) {
            return "user:" + userHeader;
        }
        String ip = request.getRemoteAddr();
        return "ip:" + ip;
    }

    private String buildKey(String identity, long window) {
        return "rl:orders:" + identity + ":" + window;
    }

    private void writeRateLimitResponse(HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 429);
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded for this endpoint");
        body.put("path", request.getRequestURI());
        body.put("fieldErrors", Map.of());

        objectMapper.writeValue(response.getWriter(), body);
    }
}
