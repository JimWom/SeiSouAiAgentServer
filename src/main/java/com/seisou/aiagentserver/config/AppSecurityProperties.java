package com.seisou.aiagentserver.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppSecurityProperties {

    private Cors cors = new Cors();
    private OriginGuard originGuard = new OriginGuard();
    private Visitor visitor = new Visitor();
    private RateLimit rateLimit = new RateLimit();
    private UsageQuota usageQuota = new UsageQuota();
    private RobotCheck robotCheck = new RobotCheck();

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public OriginGuard getOriginGuard() {
        return originGuard;
    }

    public void setOriginGuard(OriginGuard originGuard) {
        this.originGuard = originGuard;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public UsageQuota getUsageQuota() {
        return usageQuota;
    }

    public void setUsageQuota(UsageQuota usageQuota) {
        this.usageQuota = usageQuota;
    }

    public RobotCheck getRobotCheck() {
        return robotCheck;
    }

    public void setRobotCheck(RobotCheck robotCheck) {
        this.robotCheck = robotCheck;
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class OriginGuard {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Visitor {
        private String cookieName = "seisou_visitor";
        private String signingSecret = "change-me-before-production";
        private boolean secureCookie = false;
        private long maxAgeSeconds = 2_592_000;

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public String getSigningSecret() {
            return signingSecret;
        }

        public void setSigningSecret(String signingSecret) {
            this.signingSecret = signingSecret;
        }

        public boolean isSecureCookie() {
            return secureCookie;
        }

        public void setSecureCookie(boolean secureCookie) {
            this.secureCookie = secureCookie;
        }

        public long getMaxAgeSeconds() {
            return maxAgeSeconds;
        }

        public void setMaxAgeSeconds(long maxAgeSeconds) {
            this.maxAgeSeconds = maxAgeSeconds;
        }
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int visitorLimit = 30;
        private int ipLimit = 100;
        private long windowSeconds = 60;
        private long cooldownSeconds = 120;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getVisitorLimit() {
            return visitorLimit;
        }

        public void setVisitorLimit(int visitorLimit) {
            this.visitorLimit = visitorLimit;
        }

        public int getIpLimit() {
            return ipLimit;
        }

        public void setIpLimit(int ipLimit) {
            this.ipLimit = ipLimit;
        }

        public long getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(long windowSeconds) {
            this.windowSeconds = windowSeconds;
        }

        public long getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(long cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }
    }

    public static class UsageQuota {
        private boolean enabled = true;
        private int dailyVisitorLimit = 30;
        private int sessionUserMessageLimit = 50;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDailyVisitorLimit() {
            return dailyVisitorLimit;
        }

        public void setDailyVisitorLimit(int dailyVisitorLimit) {
            this.dailyVisitorLimit = dailyVisitorLimit;
        }

        public int getSessionUserMessageLimit() {
            return sessionUserMessageLimit;
        }

        public void setSessionUserMessageLimit(int sessionUserMessageLimit) {
            this.sessionUserMessageLimit = sessionUserMessageLimit;
        }
    }

    public static class RobotCheck {
        private boolean enabled = false;
        private String provider = "turnstile";
        private String turnstileVerifyUrl = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
        private String turnstileSecretKey;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getTurnstileVerifyUrl() {
            return turnstileVerifyUrl;
        }

        public void setTurnstileVerifyUrl(String turnstileVerifyUrl) {
            this.turnstileVerifyUrl = turnstileVerifyUrl;
        }

        public String getTurnstileSecretKey() {
            return turnstileSecretKey;
        }

        public void setTurnstileSecretKey(String turnstileSecretKey) {
            this.turnstileSecretKey = turnstileSecretKey;
        }
    }
}
