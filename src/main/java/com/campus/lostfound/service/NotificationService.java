package com.campus.lostfound.service;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.Notification;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final com.campus.lostfound.repository.UserRepository userRepository;
    
    /**
     * 获取用户的通知
     */
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * 获取用户的未读通知
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    /**
     * 获取用户未读通知数量
     */
    public int getUnreadCount(User user) {
        return (int) notificationRepository.countUnreadByUser(user);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限操作此通知");
        }
        
        notification.markAsRead();
        notificationRepository.save(notification);
        
        log.info("通知标记为已读: {}", notificationId);
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
        log.info("用户所有通知标记为已读: {}", user.getUsername());
    }

    /**
     * 删除所有通知
     */
    @Transactional
    public void deleteAll(User user) {
        notificationRepository.deleteAllByUser(user);
        log.info("用户所有通知已删除: {}", user.getUsername());
    }
    
    /**
     * 发送通知
     */
    @Transactional
    public void sendNotification(User user, Notification.NotificationType type, 
                               String title, String content, Item relatedItem) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedItem(relatedItem);
        notification.setIsRead(false);
        
        notificationRepository.save(notification);
        log.info("发送通知: {} -> {}", type.getDescription(), user.getUsername());
    }
    
    /**
     * 发送物品审核通过通知
     */
    @Transactional
    public void notifyItemApproved(Item item) {
        sendNotification(
            item.getOwner(),
            Notification.NotificationType.ITEM_APPROVED,
            "物品审核通过",
            "您的物品《" + item.getTitle() + "》已通过审核，现在可以在搜索中看到。",
            item
        );
    }
    
    /**
     * 发送物品审核拒绝通知
     */
    @Transactional
    public void notifyItemRejected(Item item, String reason) {
        sendNotification(
            item.getOwner(),
            Notification.NotificationType.ITEM_REJECTED,
            "物品审核拒绝",
            "您的物品《" + item.getTitle() + "》未通过审核。原因：" + reason,
            item
        );
    }
    
    /**
     * 发送匹配通知（基于权重）
     */
    @Transactional
    public void notifyWeightBasedMatching(ItemMatch match) {
        Item lostItem = match.getLostItem();
        Item foundItem = match.getFoundItem();
        
        // 给失物发布者发送通知
        sendNotification(
            lostItem.getOwner(),
            Notification.NotificationType.MATCH_FOUND,
            "找到匹配的拾获物品",
            "为您发布的失物《" + lostItem.getTitle() + "》找到了匹配的拾获物品《" + foundItem.getTitle() + "》。匹配权重：" + match.getMatchWeight(),
            lostItem
        );
        
        // 给拾获物品发布者发送通知
        sendNotification(
            foundItem.getOwner(),
            Notification.NotificationType.MATCH_FOUND,
            "找到匹配的失物",
            "为您发布的拾获物品《" + foundItem.getTitle() + "》找到了匹配的失物《" + lostItem.getTitle() + "》。匹配权重：" + match.getMatchWeight(),
            foundItem
        );
        
        log.info("发送权重匹配通知: 失物{} <-> 拾获{}", lostItem.getId(), foundItem.getId());
    }
    
    /**
     * 发送匹配列表通知
     */
    @Transactional
    public void notifyMatchingList(User user, Item newItem, List<Item> matchingItems) {
        if (matchingItems.isEmpty()) {
            return;
        }
        
        String content;
        if (matchingItems.size() == 1) {
            content = "为您发布的《" + newItem.getTitle() + "》找到了1个匹配物品：《" + matchingItems.get(0).getTitle() + "》";
        } else {
            content = "为您发布的《" + newItem.getTitle() + "》找到了" + matchingItems.size() + "个匹配物品，请查看匹配列表";
        }
        
        sendNotification(
            user,
            Notification.NotificationType.MATCH_FOUND,
            "找到匹配物品",
            content,
            newItem
        );
        
        log.info("发送匹配列表通知: 用户{} 找到{}个匹配物品", user.getUsername(), matchingItems.size());
    }
    
    /**
     * 发送认领申请通知
     */
    @Transactional
    public void notifyClaimSubmitted(Item item, User claimant) {
        sendNotification(
            item.getOwner(),
            Notification.NotificationType.CLAIM_SUBMITTED,
            "收到认领申请",
            "用户 " + claimant.getDisplayName() + " 对您的物品《" + item.getTitle() + "》提交了认领申请。",
            item
        );
    }
    
    /**
     * 发送认领审核通过通知
     */
    @Transactional
    public void notifyClaimApproved(User claimant, Item item) {
        sendNotification(
            claimant,
            Notification.NotificationType.CLAIM_APPROVED,
            "认领审核通过",
            "您对《" + item.getTitle() + "》的认领申请已通过审核。",
            item
        );
    }
    
    /**
     * 发送认领审核拒绝通知
     */
    @Transactional
    public void notifyClaimRejected(User claimant, Item item, String reason) {
        sendNotification(
            claimant,
            Notification.NotificationType.CLAIM_REJECTED,
            "认领审核拒绝",
            "您对《" + item.getTitle() + "》的认领申请未通过审核。原因：" + reason,
            item
        );
    }
    
    /**
     * 发送聊天消息通知
     */
    @Transactional
    public void notifyChatMessage(User user, String senderName, Item item) {
        sendNotification(
            user,
            Notification.NotificationType.CHAT_MESSAGE,
            "收到新消息",
            senderName + " 在关于《" + item.getTitle() + "》的对话中发送了新消息。",
            item
        );
    }
    
    /**
     * 发送管理员通知
     */
    @Transactional
    public void notifyAdminAlert(User user, String title, String content) {
        sendNotification(
            user,
            Notification.NotificationType.ADMIN_ALERT,
            title,
            content,
            null
        );
    }

    /**
     * 向全体用户广播管理员公告
     */
    @Transactional
    public void broadcastAdminAlert(String title, String content) {
        for (User u : userRepository.findAll()) {
            notifyAdminAlert(u, title, content);
        }
    }
    
    /**
     * 发送系统通知
     */
    @Transactional
    public void notifySystemMessage(User user, String title, String content) {
        sendNotification(
            user,
            Notification.NotificationType.SYSTEM_NOTIFICATION,
            title,
            content,
            null
        );
    }
    
    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权限删除此通知");
        }
        
        notificationRepository.delete(notification);
        log.info("删除通知: {}", notificationId);
    }
}