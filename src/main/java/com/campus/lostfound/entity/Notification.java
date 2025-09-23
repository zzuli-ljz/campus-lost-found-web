package com.campus.lostfound.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体类
 */
@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 100, message = "通知标题不能超过100个字符")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    @Size(max = 500, message = "通知内容不能超过500个字符")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column
    private java.time.LocalDateTime readAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_item_id")
    private Item relatedItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_claim_id")
    private Claim relatedClaim;
    
    // 枚举定义
    public enum NotificationType {
        ITEM_APPROVED("物品审核通过"),
        ITEM_REJECTED("物品审核拒绝"),
        MATCH_FOUND("找到匹配"),
        CLAIM_SUBMITTED("收到认领申请"),
        CLAIM_APPROVED("认领审核通过"),
        CLAIM_REJECTED("认领审核拒绝"),
        CHAT_MESSAGE("聊天消息"),
        ADMIN_ALERT("管理员通知"),
        SYSTEM_NOTIFICATION("系统通知");
        
        private final String description;
        
        NotificationType(String description) {
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
}
