package com.campus.lostfound.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 认领实体类
 */
@Entity
@Table(name = "claims")
@Data
@EqualsAndHashCode(callSuper = true)
public class Claim extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;
    
    @NotBlank(message = "验证细节不能为空")
    @Size(max = 500, message = "验证细节不能超过500个字符")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String verificationDetail;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.SUBMITTED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @Size(max = 200, message = "审核备注不能超过200个字符")
    @Column
    private String reviewNote;
    
    @Column
    private LocalDateTime completedAt;
    
    // 发布者对认领的确认（可选）
    @Column
    private Boolean ownerConfirmed; // true 确认、false 驳回、null 未处理
    
    @Column
    private String ownerConfirmNote;
    
    @Column
    private LocalDateTime ownerConfirmedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "relatedClaim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Notification> notifications = new java.util.ArrayList<>();
    
    // 枚举定义
    public enum ClaimStatus {
        SUBMITTED("已提交"),
        APPROVED("审核通过"),
        REJECTED("审核拒绝"),
        COMPLETED("已完成");
        
        private final String description;
        
        ClaimStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 业务方法
    public boolean isPending() {
        return ClaimStatus.SUBMITTED.equals(status);
    }
    
    public boolean isApproved() {
        return ClaimStatus.APPROVED.equals(status);
    }
    
    public boolean isRejected() {
        return ClaimStatus.REJECTED.equals(status);
    }
    
    public boolean isCompleted() {
        return ClaimStatus.COMPLETED.equals(status);
    }
    
    public boolean canBeReviewed() {
        return isPending();
    }
}
