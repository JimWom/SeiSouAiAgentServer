package com.seisou.aiagentserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SkillDto(
        UUID id,
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 500) String description,
        @NotBlank String instruction,
        boolean enabled
) {
}
