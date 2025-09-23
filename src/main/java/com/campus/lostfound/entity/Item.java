package com.campus.lostfound.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品实体类
 */
@Entity
@Table(name = "items")
@Data
@EqualsAndHashCode(callSuper = true)
public class Item extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "描述不能为空")
    @Size(max = 1000, message = "描述不能超过1000个字符")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING_APPROVAL;
    
    @NotNull(message = "丢失/拾获时间不能为空")
    @Column(nullable = false)
    private LocalDateTime lostFoundTime;
    
    @jakarta.persistence.Convert(converter = com.campus.lostfound.entity.converter.LocationConverter.class)
    @Column(nullable = false)
    private Location location;
    
    @jakarta.persistence.Convert(converter = com.campus.lostfound.entity.converter.DetailedLocationConverter.class)
    @Column(nullable = false)
    private DetailedLocation detailedLocation;
    
    @Column(nullable = false)
    private Integer totalWeight;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @Column(nullable = false)
    private Boolean approved = false;
    
    @Column
    private String approvalNote;
    
    @Column
    private LocalDateTime approvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    // 关联关系
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims = new ArrayList<>();
    
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatThread> chatThreads = new ArrayList<>();
    
    // 枚举定义
    public enum ItemCategory {
        DOCUMENTS("证件类"),
        ELECTRONICS("电子设备"),
        STATIONERY("文具用品"),
        CLOTHING("服饰"),
        ACCESSORY("配件饰品"),
        BOOKS("图书资料"),
        SPORTS("运动用品"),
        OTHER("其他");
        
        private final String description;
        
        ItemCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum PostType {
        LOST("失物"),
        FOUND("拾获");
        
        private final String description;
        
        PostType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum ItemStatus {
        PENDING_APPROVAL("待审核"),
        PENDING_CLAIM("待认领"),
        CLAIMED("已认领"),
        UNCLAIMED("无人认领"),
        EXPIRED("已过期"),
        COMPLETED("已完成");
        
        private final String description;
        
        ItemStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 业务方法
    public boolean isLost() {
        return PostType.LOST.equals(postType);
    }
    
    public boolean isFound() {
        return PostType.FOUND.equals(postType);
    }
    
    public boolean isApproved() {
        return approved != null && approved;
    }
    
    public boolean canBeClaimed() {
        return isFound() && isApproved() && ItemStatus.PENDING_CLAIM.equals(status);
    }
    
    public boolean isActive() {
        return isApproved() && (ItemStatus.PENDING_CLAIM.equals(status) || ItemStatus.CLAIMED.equals(status));
    }
    
    /**
     * 计算物品的总权重
     * 权重 = 地点权重 + 详细地点权重 + 类别权重 + 时间权重
     */
    public void calculateTotalWeight() {
        int weight = 0;
        
        // 地点权重
        if (location != null) {
            weight += location.getWeight();
        }
        
        // 详细地点权重
        if (detailedLocation != null) {
            weight += detailedLocation.getWeight();
        }
        
        // 类别权重
        if (category != null) {
            weight += getCategoryWeight(category);
        }
        
        // 时间权重（基于发布时间的新鲜度）
        if (getCreatedAt() != null) {
            weight += getTimeWeight(getCreatedAt());
        }
        
        this.totalWeight = weight;
    }
    
    /**
     * 获取类别权重
     */
    private int getCategoryWeight(ItemCategory category) {
        switch (category) {
            case DOCUMENTS:
                return 20; // 证件类权重最高
            case ELECTRONICS:
                return 15; // 电子设备权重较高
            case BOOKS:
                return 12; // 图书资料权重较高
            case STATIONERY:
                return 8;  // 文具用品权重中等
            case CLOTHING:
                return 6;   // 服饰权重较低
            case ACCESSORY:
                return 5;   // 配件饰品权重较低
            case SPORTS:
                return 4;   // 运动用品权重较低
            case OTHER:
                return 2;   // 其他权重最低
            default:
                return 1;
        }
    }
    
    /**
     * 获取时间权重（基于发布时间的新鲜度）
     */
    private int getTimeWeight(LocalDateTime createdAt) {
        long daysSinceCreated = java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        
        if (daysSinceCreated <= 1) {
            return 10; // 1天内发布，权重最高
        } else if (daysSinceCreated <= 3) {
            return 8;  // 3天内发布，权重较高
        } else if (daysSinceCreated <= 7) {
            return 5;  // 1周内发布，权重中等
        } else if (daysSinceCreated <= 14) {
            return 3;  // 2周内发布，权重较低
        } else {
            return 1;  // 超过2周，权重最低
        }
    }
    
    /**
     * 检查两个物品是否匹配
     * 匹配条件：总权重相等且拾获地点相同
     */
    public boolean matchesWith(Item other) {
        if (other == null) {
            return false;
        }
        
        // 必须是相反的类型（失物匹配拾获，拾获匹配失物）
        if (this.postType == other.postType) {
            return false;
        }
        
        // 总权重必须相等
        if (!this.totalWeight.equals(other.totalWeight)) {
            return false;
        }
        
        // 拾获地点必须相同
        if (!this.location.equals(other.location)) {
            return false;
        }
        
        return true;
    }
}