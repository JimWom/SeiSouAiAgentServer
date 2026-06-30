package com.seisou.aiagentserver.config;

import com.seisou.aiagentserver.dao.SkillDefinitionDao;
import com.seisou.aiagentserver.entity.SkillDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedSkills(SkillDefinitionDao skillDefinitionDao) {
        return args -> {
            createIfMissing(
                    skillDefinitionDao,
                    "official-info",
                    "公式情報案内",
                    "公式サイトの内容、会社情報、サービス紹介に関する質問に回答します。",
                    "公式に確認できる情報のみを使用してください。不明な場合は、担当者への確認が必要であることを伝えてください。"
            );
            createIfMissing(
                    skillDefinitionDao,
                    "lead-support",
                    "問い合わせ支援",
                    "来訪者の相談内容を整理し、担当者へ引き継ぐための情報を集めます。",
                    "必要に応じて、目的、予算、希望時期、連絡方法について簡潔に確認してください。"
            );
        };
    }

    private void createIfMissing(
            SkillDefinitionDao skillDefinitionDao,
            String code,
            String name,
            String description,
            String instruction
    ) {
        if (skillDefinitionDao.findByCode(code) != null) {
            return;
        }

        SkillDefinition skill = new SkillDefinition();
        skill.setCode(code);
        skill.setName(name);
        skill.setDescription(description);
        skill.setInstruction(instruction);
        skillDefinitionDao.insert(skill);
    }
}
