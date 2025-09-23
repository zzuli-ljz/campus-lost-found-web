package com.campus.lostfound.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息实体类
 */
@Entity
@Table(name = "chat_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private ChatThread thread;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过1000个字符")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column
    private java.time.LocalDateTime readAt;
    
    // 枚举定义
    public enum MessageType {
        TEXT("文本消息"),
        IMAGE("图片消息"),
        FILE("文件消息"),
        SYSTEM("系统消息");
        
        private final String description;
        
        MessageType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 业务方法
    public void markAsRead() {
        this.isRead = true;
        this.readAt = java.time.LocalDateTime.now();
    }
    
    public boolean isUnread() {
        return !isRead;
    }
    
    public boolean isTextMessage() {
        return MessageType.TEXT.equals(type);
    }
}
