package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.config.AppSecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VisitorIdentityService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AppSecurityProperties properties;

    public VisitorIdentityService(AppSecurityProperties properties) {
        this.properties = properties;
    }

    public String resolveVisitorId(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> visitorId = readSignedCookie(request)
                .filter(this::isValidUuid);
        if (visitorId.isPresent()) {
            return visitorId.get();
        }

        String newVisitorId = UUID.randomUUID().toString();
        writeVisitorCookie(response, newVisitorId);
        return newVisitorId;
    }

    private Optional<String> readSignedCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        String cookieName = properties.getVisitor().getCookieName();
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return parseSignedToken(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<String> parseSignedToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return Optional.empty();
            }

            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return Optional.empty();
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String expectedSignature = sign(payload);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 2) {
                return Optional.empty();
            }

            long expiresAt = Long.parseLong(payloadParts[1]);
            if (Instant.now().getEpochSecond() > expiresAt) {
                return Optional.empty();
            }
            return Optional.of(payloadParts[0]);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private void writeVisitorCookie(HttpServletResponse response, String visitorId) {
        long maxAgeSeconds = properties.getVisitor().getMaxAgeSeconds();
        long expiresAt = Instant.now().plusSeconds(maxAgeSeconds).getEpochSecond();
        String payload = visitorId + ":" + expiresAt;
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "."
                + sign(payload);

        ResponseCookie cookie = ResponseCookie.from(properties.getVisitor().getCookieName(), token)
                .httpOnly(true)
                .secure(properties.getVisitor().isSecureCookie())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getVisitor().getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign visitor token.", exception);
        }
    }

    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
