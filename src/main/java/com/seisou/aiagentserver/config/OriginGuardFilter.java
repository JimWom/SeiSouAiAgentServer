package com.seisou.aiagentserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seisou.aiagentserver.exception.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OriginGuardFilter extends OncePerRequestFilter {

    private final AppSecurityProperties properties;
    private final ObjectMapper objectMapper;

    public OriginGuardFilter(AppSecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!shouldCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (!StringUtils.hasText(origin) || !properties.getCors().getAllowedOrigins().contains(origin)) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldCheck(HttpServletRequest request) {
        if (!properties.getOriginGuard().isEnabled()) {
            return false;
        }
        if (!request.getRequestURI().startsWith("/api/")) {
            return false;
        }

        String method = request.getMethod();
        return HttpMethod.POST.matches(method)
                || HttpMethod.PUT.matches(method)
                || HttpMethod.PATCH.matches(method)
                || HttpMethod.DELETE.matches(method);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "許可されていないアクセス元です。",
                List.of(),
                LocalDateTime.now()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}
