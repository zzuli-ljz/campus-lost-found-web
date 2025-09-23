package com.campus.lostfound.service;

import com.campus.lostfound.entity.ChatMessage;
import com.campus.lostfound.entity.ChatThread;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ChatMessageRepository;
import com.campus.lostfound.repository.ChatThreadRepository;
import com.campus.lostfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatThreadRepository threadRepo;
    private final ChatMessageRepository msgRepo;
    private final ItemRepository itemRepo;

    @Transactional
    public ChatThread startOrGetThread(Long itemId, User current, Long otherUserId) {
        Item item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("物品不存在"));
        User other = new User(); other.setId(otherUserId); // 仅作键引用
        return threadRepo.findByItemAndUserAAndUserB(item, current, other)
                .or(() -> threadRepo.findByItemAndUserAAndUserB(item, other, current))
                .orElseGet(() -> {
                    ChatThread t = new ChatThread();
                    t.setItem(item);
                    t.setUserA(current);
                    t.setUserB(other);
                    return threadRepo.save(t);
                });
    }

    @Transactional(readOnly = true)
    public List<ChatThread> myThreads(User me) {
        List<ChatThread> threadsAsA = threadRepo.findByUserAOrderByUpdatedAtDesc(me);
        List<ChatThread> threadsAsB = threadRepo.findByUserBOrderByUpdatedAtDesc(me);
        
        // 合并两个列表并去重
        List<ChatThread> allThreads = new java.util.ArrayList<>();
        allThreads.addAll(threadsAsA);
        allThreads.addAll(threadsAsB);
        
        // 按更新时间排序
        allThreads.sort((t1, t2) -> {
            if (t1.getUpdatedAt() == null && t2.getUpdatedAt() == null) return 0;
            if (t1.getUpdatedAt() == null) return 1;
            if (t2.getUpdatedAt() == null) return -1;
            return t2.getUpdatedAt().compareTo(t1.getUpdatedAt());
        });
        
        // 为每个线程计算未读消息数量
        for (ChatThread thread : allThreads) {
            List<ChatMessage> unreadMessages = msgRepo.findByThreadAndSenderNotAndIsReadFalse(thread, me);
            thread.setUnreadCount(unreadMessages.size());
        }
        
        return allThreads;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> listMessages(Long threadId) {
        ChatThread t = threadRepo.findById(threadId).orElseThrow(() -> new RuntimeException("会话不存在"));
        return msgRepo.findByThreadOrderByCreatedAtAsc(t);
    }

    @Transactional
    public ChatMessage sendText(Long threadId, User sender, String content) {
        ChatThread t = threadRepo.findById(threadId).orElseThrow(() -> new RuntimeException("会话不存在"));
        ChatMessage m = new ChatMessage();
        m.setThread(t);
        m.setSender(sender);
        m.setContent(content);
        return msgRepo.save(m);
    }

    @Transactional(readOnly = true)
    public Item getItemById(Long itemId) {
        return itemRepo.findById(itemId).orElse(null);
    }

    /**
     * 获取用户的未读聊天消息数量
     */
    @Transactional(readOnly = true)
    public long getUnreadChatCount(User user) {
        List<ChatThread> userThreads = myThreads(user);
        long unreadCount = 0;
        
        for (ChatThread thread : userThreads) {
            // 计算该线程中不是当前用户发送的未读消息数量
            List<ChatMessage> unreadMessages = msgRepo.findByThreadAndSenderNotAndIsReadFalse(thread, user);
            unreadCount += unreadMessages.size();
        }
        
        return unreadCount;
    }

    @Transactional(readOnly = true)
    public ChatThread getThreadById(Long threadId) {
        return threadRepo.findById(threadId).orElse(null);
    }

    /**
     * 标记线程中的所有消息为已读
     */
    @Transactional
    public void markThreadAsRead(Long threadId, User user) {
        ChatThread thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        // 获取该线程中不是当前用户发送的未读消息
        List<ChatMessage> unreadMessages = msgRepo.findByThreadAndSenderNotAndIsReadFalse(thread, user);
        
        // 标记为已读
        for (ChatMessage message : unreadMessages) {
            message.markAsRead();
            msgRepo.save(message);
        }
    }

    @Transactional
    public ChatThread findOrCreateThread(Long itemId, User current, User other) {
        Item item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 查找现有线程
        java.util.Optional<ChatThread> existingThread = threadRepo.findByItemAndUserAAndUserB(item, current, other);
        if (existingThread.isPresent()) {
            return existingThread.get();
        }
        
        existingThread = threadRepo.findByItemAndUserAAndUserB(item, other, current);
        if (existingThread.isPresent()) {
            return existingThread.get();
        }
        
        // 创建新线程
        ChatThread thread = new ChatThread();
        thread.setItem(item);
        thread.setUserA(current);
        thread.setUserB(other);
        thread.setActive(true);
        ChatThread savedThread = threadRepo.save(thread);
        
        // 刷新线程以确保ID被设置
        threadRepo.flush();
        
        return savedThread;
    }
}






