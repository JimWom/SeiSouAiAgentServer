package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.client.OpenClawMessage;
import com.seisou.aiagentserver.entity.ChatMessage;
import com.seisou.aiagentserver.entity.ChatSession;
import com.seisou.aiagentserver.entity.MessageRole;
import com.seisou.aiagentserver.entity.SkillDefinition;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AgentContextService {

    private static final int MAX_HISTORY_MESSAGES = 20;

    public List<OpenClawMessage> buildContext(ChatSession session, List<SkillDefinition> skills, String userMessage) {
        List<OpenClawMessage> context = new ArrayList<>();
        context.add(new OpenClawMessage("system", buildSystemPrompt(skills)));

        List<ChatMessage> history = session.getMessages();
        int fromIndex = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        history.subList(fromIndex, history.size()).stream()
                .map(this::toOpenClawMessage)
                .forEach(context::add);

        context.add(new OpenClawMessage("user", userMessage));
        return context;
    }

    private String buildSystemPrompt(List<SkillDefinition> skills) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are SeiSou official-site AI chat agent. ");
        prompt.append("Answer politely, ask clarifying questions when needed, and avoid inventing company facts.");

        if (!skills.isEmpty()) {
            prompt.append("\n\nAvailable skills:");
            for (SkillDefinition skill : skills) {
                prompt.append("\n- ")
                        .append(skill.getName())
                        .append(" (")
                        .append(skill.getCode())
                        .append("): ")
                        .append(skill.getInstruction());
            }
        }

        return prompt.toString();
    }

    private OpenClawMessage toOpenClawMessage(ChatMessage message) {
        return new OpenClawMessage(toOpenClawRole(message.getRole()), message.getContent());
    }

    private String toOpenClawRole(MessageRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            case TOOL -> "tool";
        };
    }
}
