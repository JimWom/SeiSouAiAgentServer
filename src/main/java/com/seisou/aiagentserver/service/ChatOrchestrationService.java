package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.client.OpenClawChatRequest;
import com.seisou.aiagentserver.client.OpenClawChatResponse;
import com.seisou.aiagentserver.client.OpenClawClient;
import com.seisou.aiagentserver.config.OpenClawProperties;
import com.seisou.aiagentserver.converter.ChatConverter;
import com.seisou.aiagentserver.dao.ChatSessionDao;
import com.seisou.aiagentserver.dto.ChatRequest;
import com.seisou.aiagentserver.dto.ChatResponse;
import com.seisou.aiagentserver.entity.ChatMessage;
import com.seisou.aiagentserver.entity.ChatSession;
import com.seisou.aiagentserver.entity.MessageRole;
import com.seisou.aiagentserver.entity.SkillDefinition;
import com.seisou.aiagentserver.exception.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatOrchestrationService {

    private final RobotCheckService robotCheckService;
    private final SkillCatalogService skillCatalogService;
    private final RateLimitService rateLimitService;
    private final UsageQuotaService usageQuotaService;
    private final AgentContextService agentContextService;
    private final OpenClawClient openClawClient;
    private final OpenClawProperties openClawProperties;
    private final ChatSessionDao chatSessionDao;
    private final ChatConverter chatConverter;

    public ChatOrchestrationService(
            RobotCheckService robotCheckService,
            SkillCatalogService skillCatalogService,
            RateLimitService rateLimitService,
            UsageQuotaService usageQuotaService,
            AgentContextService agentContextService,
            OpenClawClient openClawClient,
            OpenClawProperties openClawProperties,
            ChatSessionDao chatSessionDao,
            ChatConverter chatConverter
    ) {
        this.robotCheckService = robotCheckService;
        this.skillCatalogService = skillCatalogService;
        this.rateLimitService = rateLimitService;
        this.usageQuotaService = usageQuotaService;
        this.agentContextService = agentContextService;
        this.openClawClient = openClawClient;
        this.openClawProperties = openClawProperties;
        this.chatSessionDao = chatSessionDao;
        this.chatConverter = chatConverter;
    }

    @Transactional
    public ChatResponse chat(ChatRequest request, String visitorId, String clientIp) {
        rateLimitService.check(visitorId, clientIp);
        usageQuotaService.checkBeforeChat(visitorId, request.sessionId());
        robotCheckService.verify(request.robotCheck(), visitorId, clientIp);

        boolean newSession = request.sessionId() == null;
        ChatSession session = resolveSession(request, visitorId);
        List<SkillDefinition> skills = skillCatalogService.findEnabledByCodes(request.skillCodes());
        OpenClawChatRequest openClawRequest = new OpenClawChatRequest(
                openClawProperties.getModel(),
                agentContextService.buildContext(session, skills, request.message())
        );

        OpenClawChatResponse openClawResponse = openClawClient.chat(openClawRequest);
        String reply = openClawResponse.reply();

        session.addMessage(new ChatMessage(MessageRole.USER, request.message()));
        session.addMessage(new ChatMessage(MessageRole.ASSISTANT, reply));
        persistSession(session, newSession);
        usageQuotaService.recordSuccessfulChat(visitorId);

        return new ChatResponse(
                session.getId(),
                reply,
                chatConverter.toDtoList(session.getMessages()),
                LocalDateTime.now()
        );
    }

    private ChatSession resolveSession(ChatRequest request, String visitorId) {
        if (request.sessionId() == null) {
            ChatSession session = new ChatSession();
            session.setVisitorId(visitorId);
            return session;
        }

        ChatSession session = chatSessionDao.findByIdAndVisitorId(request.sessionId(), visitorId);
        if (session == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "チャットセッションが見つかりませんでした。ページを更新してもう一度お試しください。");
        }
        session.setMessages(chatSessionDao.findMessagesBySessionId(session.getId()));
        return session;
    }

    private void persistSession(ChatSession session, boolean newSession) {
        if (newSession) {
            chatSessionDao.insertSession(session);
        } else {
            chatSessionDao.updateSession(session);
        }

        List<ChatMessage> messages = session.getMessages();
        chatSessionDao.insertMessage(messages.get(messages.size() - 2));
        chatSessionDao.insertMessage(messages.get(messages.size() - 1));
    }
}
