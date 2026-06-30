package com.seisou.aiagentserver.converter;

import com.seisou.aiagentserver.dto.SkillDto;
import com.seisou.aiagentserver.entity.SkillDefinition;
import org.springframework.stereotype.Component;

@Component
public class SkillConverter {

    public SkillDto toDto(SkillDefinition skill) {
        return new SkillDto(
                skill.getId(),
                skill.getCode(),
                skill.getName(),
                skill.getDescription(),
                skill.getInstruction(),
                skill.isEnabled()
        );
    }

    public SkillDefinition toEntity(SkillDto dto) {
        SkillDefinition skill = new SkillDefinition();
        skill.setCode(dto.code());
        skill.setName(dto.name());
        skill.setDescription(dto.description());
        skill.setInstruction(dto.instruction());
        skill.setEnabled(dto.enabled());
        return skill;
    }
}
