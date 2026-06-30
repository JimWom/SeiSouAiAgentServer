package com.seisou.aiagentserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ChatRequest(
        UUID sessionId,
        @NotBlank @Size(max = 4000) String message,
        @Valid RobotCheckDto robotCheck,
        @Size(max = 20) List<String> skillCodes
) {
}
