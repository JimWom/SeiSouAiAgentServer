package com.seisou.aiagentserver.dao;

import com.seisou.aiagentserver.entity.ChatMessage;
import com.seisou.aiagentserver.entity.ChatSession;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChatSessionDao {

    @Select("""
            select id, visitor_id, status, created_at, updated_at
            from chat_sessions
            where id = #{id}
              and visitor_id = #{visitorId}
            """)
    ChatSession findByIdAndVisitorId(@Param("id") UUID id, @Param("visitorId") String visitorId);

    @Select("""
            select id, session_id, role, content, created_at
            from chat_messages
            where session_id = #{sessionId}
            order by created_at asc
            """)
    List<ChatMessage> findMessagesBySessionId(UUID sessionId);

    @Select("""
            select count(*)
            from chat_messages
            where session_id = #{sessionId}
              and role = 'USER'
            """)
    int countUserMessagesBySessionId(UUID sessionId);

    @Insert("""
            insert into chat_sessions (id, visitor_id, status, created_at, updated_at)
            values (#{id}, #{visitorId}, #{status}, #{createdAt}, #{updatedAt})
            """)
    void insertSession(ChatSession session);

    @Update("""
            update chat_sessions
            set status = #{status},
                updated_at = #{updatedAt}
            where id = #{id}
            """)
    void updateSession(ChatSession session);

    @Insert("""
            insert into chat_messages (id, session_id, role, content, created_at)
            values (#{id}, #{sessionId}, #{role}, #{content}, #{createdAt})
            """)
    void insertMessage(ChatMessage message);
}
