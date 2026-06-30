package com.seisou.aiagentserver.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seisou.aiagentserver.config.AppSecurityProperties;
import com.seisou.aiagentserver.dto.RobotCheckDto;
import com.seisou.aiagentserver.exception.ApiException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class SimpleRobotCheckService implements RobotCheckService {

    private final AppSecurityProperties properties;
    private final RestClient.Builder restClientBuilder;

    public SimpleRobotCheckService(AppSecurityProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public void verify(RobotCheckDto robotCheck, String visitorId, String clientIp) {
        AppSecurityProperties.RobotCheck robotCheckProperties = properties.getRobotCheck();
        if (!robotCheckProperties.isEnabled()) {
            return;
        }
        if (robotCheck == null || !StringUtils.hasText(robotCheck.token())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Robot check token is required.");
        }
        if (!StringUtils.hasText(visitorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Visitor id is required for robot check.");
        }

        if (!"turnstile".equalsIgnoreCase(robotCheckProperties.getProvider())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unsupported robot check provider.");
        }
        if (!StringUtils.hasText(robotCheckProperties.getTurnstileSecretKey())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Robot check is not configured.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", robotCheckProperties.getTurnstileSecretKey());
        form.add("response", robotCheck.token());
        if (StringUtils.hasText(clientIp)) {
            form.add("remoteip", clientIp);
        }

        TurnstileResponse response;
        try {
            response = restClientBuilder.build()
                    .post()
                    .uri(robotCheckProperties.getTurnstileVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TurnstileResponse.class);
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Robot check provider is unavailable.");
        }

        if (response == null || !response.success()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Robot check failed.");
        }
    }

    private record TurnstileResponse(
            boolean success,
            @JsonProperty("error-codes") List<String> errorCodes
    ) {
    }
}
