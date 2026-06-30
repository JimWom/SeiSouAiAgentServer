package com.seisou.aiagentserver.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID sessionId,
        String reply,
        List<MessageDto> context,
        LocalDateTime createdAt
) {
}
