package com.seisou.aiagentserver.dao;

import com.seisou.aiagentserver.entity.SkillDefinition;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SkillDefinitionDao {

    @Select("""
            select id, code, name, description, instruction, enabled, created_at
            from skill_definitions
            where enabled = true
            order by name asc
            """)
    List<SkillDefinition> findEnabledSkills();

    @Select("""
            select id, code, name, description, instruction, enabled, created_at
            from skill_definitions
            where code = #{code}
            """)
    SkillDefinition findByCode(String code);

    @Insert("""
            insert into skill_definitions (id, code, name, description, instruction, enabled, created_at)
            values (#{id}, #{code}, #{name}, #{description}, #{instruction}, #{enabled}, #{createdAt})
            """)
    void insert(SkillDefinition skill);
}
