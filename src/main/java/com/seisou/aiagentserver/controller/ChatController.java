package com.seisou.aiagentserver.controller;

import com.seisou.aiagentserver.dto.ChatRequest;
import com.seisou.aiagentserver.dto.ChatResponse;
import com.seisou.aiagentserver.service.ClientIpService;
import com.seisou.aiagentserver.service.ChatOrchestrationService;
import com.seisou.aiagentserver.service.VisitorIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatOrchestrationService chatService;
    private final VisitorIdentityService visitorIdentityService;
    private final ClientIpService clientIpService;

    public ChatController(
            ChatOrchestrationService chatService,
            VisitorIdentityService visitorIdentityService,
            ClientIpService clientIpService
    ) {
        this.chatService = chatService;
        this.visitorIdentityService = visitorIdentityService;
        this.clientIpService = clientIpService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        httpResponse.setHeader("X-Seisou-Backend", "seisou-ai-agent-server");
        String visitorId = visitorIdentityService.resolveVisitorId(httpRequest, httpResponse);
        String clientIp = clientIpService.resolveClientIp(httpRequest);
        return chatService.chat(request, visitorId, clientIp);
    }
}
