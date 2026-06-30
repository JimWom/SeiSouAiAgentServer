package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.config.AppSecurityProperties;
import com.seisou.aiagentserver.dao.ChatSessionDao;
import com.seisou.aiagentserver.exception.ApiException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UsageQuotaService {

    private final AppSecurityProperties properties;
    private final ChatSessionDao chatSessionDao;
    private final Map<String, AtomicInteger> dailyVisitorCounts = new ConcurrentHashMap<>();

    public UsageQuotaService(AppSecurityProperties properties, ChatSessionDao chatSessionDao) {
        this.properties = properties;
        this.chatSessionDao = chatSessionDao;
    }

    public void checkBeforeChat(String visitorId, UUID sessionId) {
        AppSecurityProperties.UsageQuota usageQuota = properties.getUsageQuota();
        if (!usageQuota.isEnabled()) {
            return;
        }

        checkDailyVisitorQuota(visitorId, usageQuota.getDailyVisitorLimit());
        checkSessionQuota(sessionId, usageQuota.getSessionUserMessageLimit());
    }

    public void recordSuccessfulChat(String visitorId) {
        AppSecurityProperties.UsageQuota usageQuota = properties.getUsageQuota();
        if (!usageQuota.isEnabled()) {
            return;
        }

        dailyVisitorCounts.computeIfAbsent(dailyKey(visitorId), ignored -> new AtomicInteger())
                .incrementAndGet();
    }

    private void checkDailyVisitorQuota(String visitorId, int limit) {
        int currentCount = dailyVisitorCounts.computeIfAbsent(dailyKey(visitorId), ignored -> new AtomicInteger())
                .get();
        if (currentCount >= limit) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "本日のご利用上限に達しました。明日以降にもう一度お試しください。");
        }
    }

    private void checkSessionQuota(UUID sessionId, int limit) {
        if (sessionId == null) {
            return;
        }

        int currentCount = chatSessionDao.countUserMessagesBySessionId(sessionId);
        if (currentCount >= limit) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "このチャットの送信上限に達しました。新しいチャットを開始してください。");
        }
    }

    private String dailyKey(String visitorId) {
        return LocalDate.now() + ":" + visitorId;
    }
}
