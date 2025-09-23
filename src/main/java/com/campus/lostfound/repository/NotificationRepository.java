package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Notification;
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
 * 通知数据访问层
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据用户查找通知
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * 根据用户和读取状态查找通知
     */
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    
    /**
     * 查找用户的未读通知
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    /**
     * 根据通知类型查找通知
     */
    List<Notification> findByType(Notification.NotificationType type);
    
    /**
     * 根据用户和通知类型查找通知
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, Notification.NotificationType type);
    
    /**
     * 统计用户未读通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadByUser(@Param("user") User user);

    /**
     * 将用户的所有通知标记为已读
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadByUser(@Param("user") User user);

    /**
     * 删除用户的所有通知
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user")
    int deleteAllByUser(@Param("user") User user);

    /** 删除与指定物品相关的所有通知 */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedItem = :item")
    int deleteByRelatedItem(@Param("item") Item item);
    
    /**
     * 统计通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n")
    long countAllNotifications();
    
    /**
     * 按类型统计通知数量
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> countNotificationsByType();
    
    /**
     * 查找最近的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("user") User user, Pageable pageable);
    
    
}
