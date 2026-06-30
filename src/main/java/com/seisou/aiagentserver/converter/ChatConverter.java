package com.seisou.aiagentserver.converter;

import com.seisou.aiagentserver.dto.MessageDto;
import com.seisou.aiagentserver.entity.ChatMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChatConverter {

    public MessageDto toDto(ChatMessage message) {
        return new MessageDto(message.getRole(), message.getContent());
    }

    public List<MessageDto> toDtoList(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::toDto)
                .toList();
    }
}
