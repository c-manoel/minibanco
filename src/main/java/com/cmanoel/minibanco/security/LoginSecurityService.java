package com.cmanoel.minibanco.security;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cmanoel.minibanco.dto.LoginAttemptResponse;
import com.cmanoel.minibanco.exception.TooManyRequestsException;

@Service
public class LoginSecurityService {

    private static final int MAX_AUDIT_ITEMS = 1000;

    private final int maxAttempts;
    private final long windowSeconds;
    private final long blockSeconds;

    private final Map<String, Deque<Instant>> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> blockedUntil = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<LoginAttemptResponse> auditLog = new ConcurrentLinkedDeque<>();

    public LoginSecurityService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.window-seconds:60}") long windowSeconds,
            @Value("${security.login.block-seconds:120}") long blockSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.blockSeconds = blockSeconds;
    }

    public void assertCanAttempt(String email, String ip, String userAgent) {
        String key = buildKey(email, ip);
        Instant now = Instant.now();
        Instant until = blockedUntil.get(key);
        if (until != null && now.isBefore(until)) {
            appendAudit(new LoginAttemptResponse(
                OffsetDateTime.now(),
                safeEmail(email),
                ip,
                "BLOCKED",
                "Bloqueado por tentativas excessivas"
            ));
            throw new TooManyRequestsException("Muitas tentativas de login. Aguarde e tente novamente");
        }
        if (until != null && now.isAfter(until)) {
            blockedUntil.remove(key);
        }
    }

    public void recordSuccess(String email, String ip, String userAgent) {
        String key = buildKey(email, ip);
        failedAttempts.remove(key);
        blockedUntil.remove(key);
        appendAudit(new LoginAttemptResponse(
            OffsetDateTime.now(),
            safeEmail(email),
            ip,
            "SUCCESS",
            "Login realizado"
        ));
    }

    public void recordFailure(String email, String ip, String userAgent, String reason) {
        String key = buildKey(email, ip);
        Instant now = Instant.now();
        Deque<Instant> queue = failedAttempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        Instant windowStart = now.minusSeconds(windowSeconds);

        while (!queue.isEmpty() && queue.peekFirst().isBefore(windowStart)) {
            queue.pollFirst();
        }

        queue.addLast(now);
        if (queue.size() >= maxAttempts) {
            blockedUntil.put(key, now.plusSeconds(blockSeconds));
            queue.clear();
        }

        appendAudit(new LoginAttemptResponse(
            OffsetDateTime.now(),
            safeEmail(email),
            ip,
            "FAILURE",
            reason
        ));
    }

    public List<LoginAttemptResponse> findRecentByEmail(String email, int limit) {
        return auditLog.stream()
            .filter(item -> item.getEmail().equalsIgnoreCase(email))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private void appendAudit(LoginAttemptResponse item) {
        auditLog.addFirst(item);
        while (auditLog.size() > MAX_AUDIT_ITEMS) {
            auditLog.pollLast();
        }
    }

    private String buildKey(String email, String ip) {
        return safeEmail(email).toLowerCase() + "|" + ip;
    }

    private String safeEmail(String email) {
        return email == null || email.isBlank() ? "desconhecido" : email;
    }
}
