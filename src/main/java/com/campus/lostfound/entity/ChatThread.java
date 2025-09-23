package com.campus.lostfound.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天线程实体类
 */
@Entity
@Table(name = "chat_threads")
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatThread extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    // 未读消息数量（不持久化到数据库）
    @Transient
    private Integer unreadCount = 0;
    
    // 关联关系
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> messages = new ArrayList<>();
    
    // 业务方法
    public boolean isActive() {
        return active != null && active;
    }
    
    public User getOtherUser(User currentUser) {
        if (userA.getId().equals(currentUser.getId())) {
            return userB;
        } else {
            return userA;
        }
    }
    
    public boolean involvesUser(User user) {
        return userA.getId().equals(user.getId()) || userB.getId().equals(user.getId());
    }
}
