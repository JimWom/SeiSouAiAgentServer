package com.seisou.aiagentserver.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String message,
        List<String> details,
        LocalDateTime timestamp
) {
}
