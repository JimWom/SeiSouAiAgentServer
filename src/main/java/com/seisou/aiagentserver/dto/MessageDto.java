package com.seisou.aiagentserver.dto;

import com.seisou.aiagentserver.entity.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageDto(
        @NotNull MessageRole role,
        @NotBlank String content
) {
}
