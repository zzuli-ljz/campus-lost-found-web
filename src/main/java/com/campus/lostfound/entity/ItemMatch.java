package com.campus.lostfound.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物品匹配记录实体类
 */
@Entity
@Table(name = "item_matches")
@Data
@EqualsAndHashCode(callSuper = true)
public class ItemMatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private Item lostItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", nullable = false)
    private Item foundItem;

    @Column(nullable = false)
    private Double matchWeight;

    @Column(nullable = false)
    private LocalDateTime matchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.ACTIVE;

    @Column
    private LocalDateTime completedAt;

    // 匹配状态枚举
    public enum MatchStatus {
        ACTIVE("活跃"),
        COMPLETED("已完成"),
        EXPIRED("已过期"),
        CANCELLED("已取消");

        private final String description;

        MatchStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 业务方法
    public boolean isActive() {
        return MatchStatus.ACTIVE.equals(status);
    }

    public boolean isCompleted() {
        return MatchStatus.COMPLETED.equals(status);
    }

    public void markAsCompleted() {
        this.status = MatchStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = MatchStatus.CANCELLED;
    }

    public User getLostItemOwner() {
        return lostItem.getOwner();
    }

    public User getFoundItemOwner() {
        return foundItem.getOwner();
    }

    public boolean involvesUser(User user) {
        return lostItem.getOwner().getId().equals(user.getId()) || 
               foundItem.getOwner().getId().equals(user.getId());
    }
}



