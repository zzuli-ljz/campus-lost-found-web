package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 匹配记录数据访问层
 */
@Repository
public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {
    
    /**
     * 根据失物查找匹配记录
     */
    List<ItemMatch> findByLostItemOrderByMatchedAtDesc(Item lostItem);
    
    /**
     * 根据拾获物品查找匹配记录
     */
    List<ItemMatch> findByFoundItemOrderByMatchedAtDesc(Item foundItem);
    
    /**
     * 根据用户查找所有相关的匹配记录
     */
    @Query("SELECT m FROM ItemMatch m WHERE " +
           "(m.lostItem.owner = :user OR m.foundItem.owner = :user) " +
           "ORDER BY m.matchedAt DESC")
    List<ItemMatch> findByUser(@Param("user") User user);
    
    /**
     * 根据用户和状态查找匹配记录
     */
    @Query("SELECT m FROM ItemMatch m WHERE " +
           "(m.lostItem.owner = :user OR m.foundItem.owner = :user) " +
           "AND m.status = :status " +
           "ORDER BY m.matchedAt DESC")
    List<ItemMatch> findByUserAndStatus(@Param("user") User user, @Param("status") ItemMatch.MatchStatus status);
    
    /**
     * 查找活跃的匹配记录
     */
    List<ItemMatch> findByStatusOrderByMatchedAtDesc(ItemMatch.MatchStatus status);
    
    /**
     * 查找过期的匹配记录
     */
    @Query("SELECT m FROM ItemMatch m WHERE " +
           "m.status = 'ACTIVE' AND m.matchedAt < :expiryDate")
    List<ItemMatch> findExpiredMatches(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * 检查两个物品是否已经匹配
     */
    @Query("SELECT COUNT(m) > 0 FROM ItemMatch m WHERE " +
           "((m.lostItem = :item1 AND m.foundItem = :item2) OR " +
           "(m.lostItem = :item2 AND m.foundItem = :item1)) " +
           "AND m.status = 'ACTIVE'")
    boolean existsMatchBetweenItems(@Param("item1") Item item1, @Param("item2") Item item2);
    
    /**
     * 统计用户的匹配数量
     */
    @Query("SELECT COUNT(m) FROM ItemMatch m WHERE " +
           "(m.lostItem.owner = :user OR m.foundItem.owner = :user) " +
           "AND m.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") ItemMatch.MatchStatus status);

    /** 删除与指定物品相关的所有匹配记录 */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItemMatch m WHERE m.lostItem = :item OR m.foundItem = :item")
    int deleteByItem(@Param("item") Item item);

    /**
     * 查找与指定物品相关的所有匹配记录
     */
    @Query("SELECT m FROM ItemMatch m WHERE m.lostItem = :item OR m.foundItem = :item")
    List<ItemMatch> findByLostItemOrFoundItem(@Param("item") Item item);
}










