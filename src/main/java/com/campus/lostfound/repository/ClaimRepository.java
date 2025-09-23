package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 认领数据访问层
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    /**
     * 根据物品查找认领
     */
    List<Claim> findByItemOrderByCreatedAtDesc(Item item);
    
    /**
     * 根据认领人查找认领
     */
    List<Claim> findByClaimantOrderByCreatedAtDesc(User claimant);
    
    /**
     * 根据状态查找认领
     */
    List<Claim> findByStatus(Claim.ClaimStatus status);
    
    /**
     * 查找待审核的认领
     */
    List<Claim> findByStatusOrderByCreatedAtAsc(Claim.ClaimStatus status);
    
    /**
     * 根据物品和认领人查找认领
     */
    List<Claim> findByItemAndClaimant(Item item, User claimant);
    
    /**
     * 检查用户是否已对某物品提交认领
     */
    boolean existsByItemAndClaimant(Item item, User claimant);
    
    /**
     * 统计认领数量
     */
    @Query("SELECT COUNT(c) FROM Claim c")
    long countAllClaims();
    
    /**
     * 统计待审核认领数量
     */
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = 'SUBMITTED'")
    long countPendingClaims();
    
    /**
     * 统计已通过认领数量
     */
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = 'APPROVED'")
    long countApprovedClaims();
    
    /**
     * 统计已完成认领数量
     */
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = 'COMPLETED'")
    long countCompletedClaims();
    
    /**
     * 按状态统计认领数量
     */
    @Query("SELECT c.status, COUNT(c) FROM Claim c GROUP BY c.status")
    List<Object[]> countClaimsByStatus();
    
    /**
     * 查找用户的认领历史
     */
    @Query("SELECT c FROM Claim c WHERE c.claimant = :user ORDER BY c.createdAt DESC")
    Page<Claim> findUserClaims(@Param("user") User user, Pageable pageable);
    
    /**
     * 查找物品的认领历史
     */
    @Query("SELECT c FROM Claim c WHERE c.item = :item ORDER BY c.createdAt DESC")
    List<Claim> findItemClaims(@Param("item") Item item);

    /** 删除与指定物品相关的所有认领记录 */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Claim c WHERE c.item = :item")
    int deleteByItem(@Param("item") Item item);
}