package com.seisou.aiagentserver.client;

import java.util.List;

public record OpenClawChatRequest(
        String model,
        List<OpenClawMessage> messages
) {
}
