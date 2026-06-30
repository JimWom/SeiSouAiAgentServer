package com.seisou.aiagentserver.service;

import com.seisou.aiagentserver.converter.SkillConverter;
import com.seisou.aiagentserver.dao.SkillDefinitionDao;
import com.seisou.aiagentserver.dto.SkillDto;
import com.seisou.aiagentserver.entity.SkillDefinition;
import com.seisou.aiagentserver.exception.ApiException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillCatalogService {

    private final SkillDefinitionDao skillDefinitionDao;
    private final SkillConverter skillConverter;

    public SkillCatalogService(SkillDefinitionDao skillDefinitionDao, SkillConverter skillConverter) {
        this.skillDefinitionDao = skillDefinitionDao;
        this.skillConverter = skillConverter;
    }

    @Transactional(readOnly = true)
    public List<SkillDto> listEnabledSkills() {
        return skillDefinitionDao.findEnabledSkills()
                .stream()
                .map(skillConverter::toDto)
                .toList();
    }

    @Transactional
    public SkillDto createSkill(SkillDto request) {
        Optional.ofNullable(skillDefinitionDao.findByCode(request.code())).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Skill code already exists.");
        });

        SkillDefinition skill = skillConverter.toEntity(request);
        skillDefinitionDao.insert(skill);
        return skillConverter.toDto(skill);
    }

    @Transactional(readOnly = true)
    public List<SkillDefinition> findEnabledByCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return skillDefinitionDao.findEnabledSkills();
        }

        return codes.stream()
                .map(code -> Optional.ofNullable(skillDefinitionDao.findByCode(code))
                        .filter(SkillDefinition::isEnabled)
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown or disabled skill: " + code)))
                .toList();
    }
}
