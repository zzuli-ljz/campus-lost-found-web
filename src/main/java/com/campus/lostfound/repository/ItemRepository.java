package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.Location;
import com.campus.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品数据访问层
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    /**
     * 根据发布者查找物品（建议首选按ID过滤，避免实体匹配不一致）
     */
    List<Item> findByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * 根据发布者ID查找物品（更健壮）
     */
    List<Item> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    
    /**
     * 根据发布类型查找物品
     */
    Page<Item> findByPostType(Item.PostType postType, Pageable pageable);
    
    /**
     * 根据类别查找物品
     */
    Page<Item> findByCategory(Item.ItemCategory category, Pageable pageable);
    
    /**
     * 根据状态查找物品
     */
    List<Item> findByStatus(Item.ItemStatus status);
    
    /**
     * 查找已审核的物品
     */
    Page<Item> findByApprovedTrue(Pageable pageable);
    
    /**
     * 查找所有已审核的物品（按创建时间降序）
     */
    List<Item> findByApprovedTrueOrderByCreatedAtDesc();
    
    /**
     * 查找待审核的物品
     */
    List<Item> findByApprovedFalseOrderByCreatedAtAsc();
    
    /**
     * 根据发布者和审核状态查找物品
     */
    List<Item> findByOwnerAndApproved(User owner, Boolean approved);
    
    /**
     * 搜索物品（按标题和描述）
     */
    @Query("SELECT i FROM Item i WHERE " +
           "i.approved = true AND (" +
           "LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> searchApprovedItems(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 高级搜索物品
     */
    @Query("SELECT i FROM Item i WHERE " +
           "i.approved = true AND " +
           "(:postType IS NULL OR i.postType = :postType) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> advancedSearch(@Param("keyword") String keyword,
                              @Param("postType") Item.PostType postType,
                              @Param("category") Item.ItemCategory category,
                              Pageable pageable);
    
    /**
     * 查找最近的物品
     */
    @Query("SELECT i FROM Item i WHERE i.approved = true ORDER BY i.createdAt DESC")
    Page<Item> findRecentItems(Pageable pageable);
    
    /**
     * 查找热门物品（按认领数量）
     */
    @Query("SELECT i FROM Item i WHERE i.approved = true AND i.postType = 'FOUND' " +
           "ORDER BY SIZE(i.claims) DESC")
    Page<Item> findPopularItems(Pageable pageable);
    
    /**
     * 统计物品数量
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.approved = true")
    long countApprovedItems();
    
    /**
     * 统计失物数量
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.approved = true AND i.postType = 'LOST'")
    long countLostItems();
    
    /**
     * 统计拾获数量
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.approved = true AND i.postType = 'FOUND'")
    long countFoundItems();
    
    /**
     * 按类别统计物品数量
     */
    @Query("SELECT i.category, COUNT(i) FROM Item i WHERE i.approved = true GROUP BY i.category")
    List<Object[]> countItemsByCategory();
    
    /**
     * 按地点统计物品数量
     */
    @Query("SELECT i.location, COUNT(i) FROM Item i WHERE i.approved = true AND i.location IS NOT NULL GROUP BY i.location")
    List<Object[]> countItemsByLocation();
    
    /**
     * 查找过期的物品
     */
    @Query("SELECT i FROM Item i WHERE i.approved = true AND i.status = 'PENDING_CLAIM' AND i.createdAt < :expiryDate")
    List<Item> findExpiredItems(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * 根据权重和地点查找匹配物品
     */
    @Query("SELECT i FROM Item i WHERE " +
           "i.approved = true AND " +
           "i.postType = :postType AND " +
           "i.totalWeight = :weight AND " +
           "i.location = :location AND " +
           "i.status = 'PENDING_CLAIM'")
    List<Item> findMatchingItemsByWeight(@Param("postType") Item.PostType postType,
                                         @Param("weight") Integer weight,
                                         @Param("location") Location location);

    /**
     * 统计已完成物品数量
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = 'COMPLETED'")
    long countCompletedItems();
}
