package com.campus.lostfound.repository;

import com.campus.lostfound.entity.ChatMessage;
import com.campus.lostfound.entity.ChatThread;
import com.campus.lostfound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByThreadOrderByCreatedAtAsc(ChatThread thread);
    List<ChatMessage> findByThreadAndSenderNotAndIsReadFalse(ChatThread thread, User sender);
}






