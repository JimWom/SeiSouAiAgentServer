package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.config.AppSecurityProperties;
import com.seisou.aiagentserver.exception.ApiException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RateLimitService {

    private final AppSecurityProperties properties;
    private final Map<String, ArrayDeque<Long>> buckets = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();

    public RateLimitService(AppSecurityProperties properties) {
        this.properties = properties;
    }

    public void check(String visitorId, String clientIp) {
        AppSecurityProperties.RateLimit rateLimit = properties.getRateLimit();
        if (!rateLimit.isEnabled()) {
            return;
        }

        checkBlocked("visitor:" + visitorId);
        checkBucket("visitor:" + visitorId, rateLimit.getVisitorLimit(), rateLimit.getWindowSeconds());
        if (StringUtils.hasText(clientIp)) {
            checkBlocked("ip:" + clientIp);
            checkBucket("ip:" + clientIp, rateLimit.getIpLimit(), rateLimit.getWindowSeconds());
        }
    }

    private void checkBlocked(String key) {
        Long blockedUntilSecond = blockedUntil.get(key);
        if (blockedUntilSecond == null) {
            return;
        }

        long now = Instant.now().getEpochSecond();
        if (now < blockedUntilSecond) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "短時間に送信が集中しています。しばらく時間をおいてからお試しください。");
        }
        blockedUntil.remove(key);
    }

    private void checkBucket(String key, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        long oldestAllowed = now - windowSeconds;
        ArrayDeque<Long> bucket = buckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (bucket) {
            while (!bucket.isEmpty() && bucket.peekFirst() < oldestAllowed) {
                bucket.removeFirst();
            }
            if (bucket.size() >= limit) {
                blockedUntil.put(key, now + properties.getRateLimit().getCooldownSeconds());
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "送信回数が多すぎます。しばらく時間をおいてからお試しください。");
            }
            bucket.addLast(now);
        }
    }
}
