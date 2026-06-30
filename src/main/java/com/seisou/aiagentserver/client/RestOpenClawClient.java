package com.seisou.aiagentserver.client;

import com.seisou.aiagentserver.config.OpenClawProperties;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class RestOpenClawClient implements OpenClawClient {

    private final OpenClawProperties properties;
    private final RestClient.Builder restClientBuilder;

    public RestOpenClawClient(OpenClawProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public OpenClawChatResponse chat(OpenClawChatRequest request) {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            return new OpenClawChatResponse("【バックエンド応答】OpenClaw はまだ未設定です。受信メッセージ: " + lastUserMessage(request));
        }

        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + Optional.ofNullable(properties.getApiKey()).orElse(""))
                .build();

        OpenClawChatResponse response = restClient.post()
                .uri(properties.getChatPath())
                .body(request)
                .retrieve()
                .body(OpenClawChatResponse.class);

        return Optional.ofNullable(response)
                .orElseGet(() -> new OpenClawChatResponse(""));
    }

    private String lastUserMessage(OpenClawChatRequest request) {
        return request.messages().stream()
                .filter(message -> "user".equals(message.role()))
                .reduce((first, second) -> second)
                .map(OpenClawMessage::content)
                .orElse("");
    }
}
