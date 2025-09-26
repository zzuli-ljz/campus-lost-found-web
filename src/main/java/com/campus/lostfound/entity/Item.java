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
        // 证件类
        STUDENT_ID("学生证"),
        ID_CARD("身份证"),
        BANK_CARD("银行卡"),
        CAMPUS_CARD("校园卡"),
        DRIVER_LICENSE("驾驶证"),
        
        // 电子设备类
        MOBILE_PHONE("手机"),
        LAPTOP("笔记本电脑"),
        TABLET("平板电脑"),
        HEADPHONES("耳机"),
        POWER_BANK("充电宝"),
        CHARGER("充电器"),
        USB_DRIVE("U盘"),
        CAMERA("相机"),
        SMARTWATCH("智能手表"),
        
        // 学习用品类
        TEXTBOOK("教科书"),
        NOTEBOOK("笔记本"),
        PEN("笔"),
        PENCIL("铅笔"),
        ERASER("橡皮"),
        RULER("尺子"),
        CALCULATOR("计算器"),
        FOLDER("文件夹"),
        BACKPACK("书包"),
        
        // 服饰类
        JACKET("外套"),
        SWEATER("毛衣"),
        T_SHIRT("T恤"),
        PANTS("裤子"),
        SHOES("鞋子"),
        HAT("帽子"),
        SCARF("围巾"),
        GLOVES("手套"),
        
        // 配饰类
        GLASSES("眼镜"),
        SUNGLASSES("太阳镜"),
        WATCH("手表"),
        NECKLACE("项链"),
        BRACELET("手镯"),
        RING("戒指"),
        EARRINGS("耳环"),
        
        // 生活用品类
        WATER_BOTTLE("水杯"),
        UMBRELLA("雨伞"),
        WALLET("钱包"),
        KEY("钥匙"),
        KEYCHAIN("钥匙扣"),
        COSMETICS("化妆品"),
        MEDICINE("药品"),
        
        // 运动用品类
        BASKETBALL("篮球"),
        FOOTBALL("足球"),
        BADMINTON_RACKET("羽毛球拍"),
        TABLE_TENNIS_PADDLE("乒乓球拍"),
        SPORTS_SHOES("运动鞋"),
        SPORTS_CLOTHES("运动服"),
        YOGA_MAT("瑜伽垫"),
        
        // 其他
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
            // 证件类 - 权重最高
            case STUDENT_ID:
            case ID_CARD:
            case CAMPUS_CARD:
            case DRIVER_LICENSE:
                return 20;
            case BANK_CARD:
                return 18;
                
            // 电子设备类 - 权重较高
            case MOBILE_PHONE:
            case LAPTOP:
            case TABLET:
                return 15;
            case HEADPHONES:
            case POWER_BANK:
            case CHARGER:
            case CAMERA:
            case SMARTWATCH:
                return 12;
            case USB_DRIVE:
                return 10;
                
            // 学习用品类 - 权重中等偏高
            case TEXTBOOK:
            case CALCULATOR:
                return 12;
            case NOTEBOOK:
            case BACKPACK:
                return 10;
            case PEN:
            case PENCIL:
            case ERASER:
            case RULER:
            case FOLDER:
                return 8;
                
            // 服饰类 - 权重中等
            case JACKET:
            case SWEATER:
            case SHOES:
                return 8;
            case T_SHIRT:
            case PANTS:
            case HAT:
            case SCARF:
            case GLOVES:
                return 6;
                
            // 配饰类 - 权重中等偏低
            case GLASSES:
            case WATCH:
                return 8;
            case SUNGLASSES:
            case NECKLACE:
            case BRACELET:
            case RING:
            case EARRINGS:
                return 5;
                
            // 生活用品类 - 权重中等偏低
            case WALLET:
            case KEY:
                return 10;
            case WATER_BOTTLE:
            case UMBRELLA:
            case KEYCHAIN:
            case COSMETICS:
            case MEDICINE:
                return 6;
                
            // 运动用品类 - 权重较低
            case BASKETBALL:
            case FOOTBALL:
            case BADMINTON_RACKET:
            case TABLE_TENNIS_PADDLE:
            case SPORTS_SHOES:
            case SPORTS_CLOTHES:
            case YOGA_MAT:
                return 4;
                
            // 其他 - 权重最低
            case OTHER:
                return 2;
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