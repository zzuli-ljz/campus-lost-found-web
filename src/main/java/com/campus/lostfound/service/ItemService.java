package com.campus.lostfound.service;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ItemMatchRepository;
import com.campus.lostfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 物品服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final MatchingService matchingService;
    private final com.campus.lostfound.repository.ClaimRepository claimRepository;
    private final com.campus.lostfound.repository.NotificationRepository notificationRepository;
    private final com.campus.lostfound.repository.ReportRepository reportRepository;
    
    /**
     * 发布物品
     */
    @Transactional
    public Item postItem(String title, String description, Item.ItemCategory category, 
                        Item.PostType postType, com.campus.lostfound.entity.Location location, 
                        com.campus.lostfound.entity.DetailedLocation detailedLocation,
                        LocalDateTime lostFoundTime, User owner, List<MultipartFile> images) {
        
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setPostType(postType);
        item.setLocation(location);
        item.setDetailedLocation(detailedLocation);
        item.setLostFoundTime(lostFoundTime);
        item.setOwner(owner);
        item.setStatus(Item.ItemStatus.PENDING_APPROVAL);
        item.setApproved(false);
        
        // 计算权重
        item.calculateTotalWeight();
        
        Item savedItem = itemRepository.save(item);
        
        // 保存图片
        if (images != null && !images.isEmpty()) {
            try {
                fileStorageService.saveItemImages(savedItem, images);
            } catch (IOException e) {
                log.error("保存物品图片失败: {}", e.getMessage());
                // 不抛出异常，允许没有图片的物品发布
            }
        }
        
        log.info("物品发布成功: {} - {}", postType.getDescription(), title);
        return savedItem;
    }
    
    /**
     * 根据ID查找物品
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
    
    /**
     * 获取已审核的物品
     */
    public Page<Item> getApprovedItems(Pageable pageable) {
        return itemRepository.findByApprovedTrue(pageable);
    }
    
    /**
     * 获取待审核的物品
     */
    public List<Item> getPendingItems() {
        return itemRepository.findByApprovedFalseOrderByCreatedAtAsc();
    }

    /**
     * 获取所有已审核通过的物品
     */
    public List<Item> getAllApprovedItems() {
        return itemRepository.findByApprovedTrueOrderByCreatedAtDesc();
    }

    /**
     * 确认物品完成 - 将物品状态改为已完成，并更新相关匹配记录
     */
    @Transactional
    public void confirmItemCompletion(Long itemId, User user) {
        log.info("=== 开始确认物品完成 ===");
        log.info("物品ID: {}", itemId);
        log.info("用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        
        try {
            // 查找物品
            log.info("正在查找物品...");
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> {
                        log.error("物品不存在，ID: {}", itemId);
                        return new RuntimeException("物品不存在，ID: " + itemId);
                    });

            log.info("找到物品: {} (状态: {}, 审核状态: {})", 
                    item.getTitle(), item.getStatus(), item.getApproved());

            // 检查用户是否有权限操作此物品
            log.info("检查用户权限...");
            log.info("物品所有者ID: {}, 当前用户ID: {}", item.getOwner().getId(), user.getId());
            if (!item.getOwner().getId().equals(user.getId())) {
                log.error("用户无权限操作此物品: userId={}, ownerId={}", user.getId(), item.getOwner().getId());
                throw new RuntimeException("无权限操作此物品");
            }
            log.info("权限检查通过");

            // 简化的状态检查
            log.info("检查物品状态...");
            if (item.getStatus() == Item.ItemStatus.COMPLETED) {
                log.warn("物品已经是完成状态: {}", item.getTitle());
                return; // 已经是完成状态，直接返回
            }

            log.info("状态检查通过，开始更新物品状态...");

            // 更新物品状态为已完成
            item.setStatus(Item.ItemStatus.COMPLETED);
            log.info("物品状态已设置为COMPLETED，正在保存到数据库...");
            
            Item savedItem = itemRepository.save(item);
            log.info("物品状态已更新为已完成: {} (新状态: {})", savedItem.getTitle(), savedItem.getStatus());

            // 更新相关的匹配记录状态
            try {
                log.info("开始更新匹配记录...");
                List<ItemMatch> matches = itemMatchRepository.findByLostItemOrFoundItem(item);
                log.info("找到 {} 个匹配记录", matches.size());
                
                for (ItemMatch match : matches) {
                    if (match.getStatus() == ItemMatch.MatchStatus.ACTIVE) {
                        match.markAsCompleted();
                        itemMatchRepository.save(match);
                        log.info("匹配记录已标记为完成: {} (匹配ID: {})", match.getId(), match.getId());
                    }
                }
                
                // 如果没有匹配记录，创建一个虚拟的完成记录用于历史显示
                if (matches.isEmpty()) {
                    log.info("物品 {} 没有匹配记录，创建完成记录用于历史显示", item.getTitle());
                    // 这里可以创建一个特殊的完成记录，或者只是记录日志
                }
            } catch (Exception e) {
                log.error("更新匹配记录时出错: {}", e.getMessage(), e);
                // 不抛出异常，因为物品状态已经更新成功
            }

            log.info("=== 物品确认完成成功: {} (ID: {}) ===", item.getTitle(), itemId);
        } catch (Exception e) {
            log.error("=== 确认物品完成失败 ===");
            log.error("物品ID: {}", itemId);
            log.error("用户ID: {}", user.getId());
            log.error("错误信息: {}", e.getMessage());
            log.error("错误堆栈: ", e);
            throw new RuntimeException("确认完成失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户的物品
     */
    public List<Item> getUserItems(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        // 改为按用户ID查询，避免因持久化上下文或equals未覆写导致的误匹配
        return itemRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId());
    }
    
    /**
     * 搜索物品
     */
    public Page<Item> searchItems(String keyword, Pageable pageable) {
        return itemRepository.searchApprovedItems(keyword, pageable);
    }
    
    /**
     * 高级搜索物品
     */
    public Page<Item> advancedSearch(String keyword, Item.PostType postType, 
                                   Item.ItemCategory category, Pageable pageable) {
        return itemRepository.advancedSearch(keyword, postType, category, pageable);
    }
    
    /**
     * 获取最近发布的物品
     */
    public Page<Item> getRecentItems(Pageable pageable) {
        return itemRepository.findRecentItems(pageable);
    }
    
    /**
     * 获取热门物品
     */
    public Page<Item> getPopularItems(Pageable pageable) {
        return itemRepository.findPopularItems(pageable);
    }
    
    /**
     * 审核物品
     */
    @Transactional
    public void approveItem(Long itemId, User approver, String approvalNote) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        item.setApproved(true);
        item.setApprovedBy(approver);
        item.setApprovalNote(approvalNote);
        item.setApprovedAt(LocalDateTime.now());
        item.setStatus(Item.ItemStatus.PENDING_CLAIM);
        
        itemRepository.save(item);
        log.info("物品审核通过: {}", item.getTitle());
        
        // 发送审核通过通知
        notificationService.notifyItemApproved(item);
        
        // 物品审核通过后进行匹配
        try {
            List<ItemMatch> createdMatches = matchingService.findAndCreateMatches(item);
            if (!createdMatches.isEmpty()) {
                // 发送匹配列表通知
                List<Item> matchingItems = new ArrayList<>();
                for (ItemMatch match : createdMatches) {
                    if (item.getPostType() == Item.PostType.LOST) {
                        matchingItems.add(match.getFoundItem());
                    } else {
                        matchingItems.add(match.getLostItem());
                    }
                }
                notificationService.notifyMatchingList(item.getOwner(), item, matchingItems);
            }
        } catch (Exception e) {
            log.error("物品审核后匹配失败: {}", e.getMessage());
        }
    }
    
    /**
     * 拒绝物品
     */
    @Transactional
    public void rejectItem(Long itemId, User approver, String rejectionNote) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 标记为未通过并记录审批信息（便于审计日志）
        item.setApproved(false);
        item.setApprovedBy(approver);
        item.setApprovalNote(rejectionNote);
        item.setApprovedAt(LocalDateTime.now());
        itemRepository.save(item);
        
        // 向发布者发送驳回通知（不关联具体物品，避免物品删除时通知被一并清理）
        try {
            notificationService.sendNotification(
                    item.getOwner(),
                    com.campus.lostfound.entity.Notification.NotificationType.ITEM_REJECTED,
                    "物品审核拒绝",
                    "您的物品《" + item.getTitle() + "》未通过审核。原因：" + (rejectionNote == null ? "" : rejectionNote),
                    null
            );
        } catch (Exception e) {
            log.error("发送驳回通知失败: {}", e.getMessage());
        }
        
        // 根据需求：被驳回的发布将被删除
        try {
            deleteItem(itemId);
        } catch (Exception e) {
            log.error("驳回后删除物品失败: {}", e.getMessage());
        }
        
        log.info("物品审核拒绝并删除: {}", item.getTitle());
    }
    
    /**
     * 更新物品状态
     */
    @Transactional
    public void updateItemStatus(Long itemId, Item.ItemStatus status) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        item.setStatus(status);
        itemRepository.save(item);
        
        log.info("物品状态更新: {} -> {}", item.getTitle(), status.getDescription());
    }
    
    /**
     * 保存物品
     */
    @Transactional
    public Item save(Item item) {
        return itemRepository.save(item);
    }
    
    /**
     * 删除物品
     */
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 删除相关图片
        try {
            fileStorageService.deleteItemImages(item);
        } catch (IOException e) {
            log.error("删除物品图片失败: {}", e.getMessage());
        }
        
        // 先清理依赖关系（匹配、认领、通知、举报）
        itemMatchRepository.deleteByItem(item);
        com.campus.lostfound.repository.ClaimRepository claimRepository = null; // will be injected via field if exists
        com.campus.lostfound.repository.NotificationRepository notificationRepository = null; // same
        com.campus.lostfound.repository.ReportRepository reportRepository = null; // same
        try {
            // 使用 Spring 上下文已注入的bean（这些字段应当在类上声明为 final，并通过构造器注入）。
            claimRepository = this.claimRepository;
            notificationRepository = this.notificationRepository;
            reportRepository = this.reportRepository;
        } catch (Exception ignored) {
        }
        if (claimRepository != null) claimRepository.deleteByItem(item);
        if (notificationRepository != null) notificationRepository.deleteByRelatedItem(item);
        if (reportRepository != null) reportRepository.deleteAll(reportRepository.findAll().stream().filter(r -> r.getItem().getId().equals(item.getId())).toList());

        itemRepository.delete(item);
        log.info("物品删除成功: {}", item.getTitle());
    }

    /**
     * 管理员清理所有物品（用于移除历史测试数据）
     */
    @Transactional
    public void deleteAllItems() {
        // 分步清理，避免外键约束错误
        for (Item item : itemRepository.findAll()) {
            deleteItem(item.getId());
        }
        log.info("已清理所有物品数据");
    }
    
    /**
     * 查找匹配的物品（基于权重）
     */
    public List<Item> findMatchingItems(Item item) {
        Item.PostType oppositeType = item.getPostType() == Item.PostType.LOST ? 
                Item.PostType.FOUND : Item.PostType.LOST;
        
        // 查找相同权重、相同地点、相反类型的已审核物品
        List<Item> candidates = itemRepository.findMatchingItemsByWeight(
                oppositeType, item.getTotalWeight(), item.getLocation());
        
        // 过滤掉已经匹配过的物品
        List<Item> matchingItems = new ArrayList<>();
        for (Item candidate : candidates) {
            if (!itemMatchRepository.existsMatchBetweenItems(item, candidate)) {
                matchingItems.add(candidate);
            }
        }
        
        return matchingItems;
    }
    
    /**
     * 创建匹配记录
     */
    @Transactional
    public ItemMatch createMatch(Item item1, Item item2) {
        ItemMatch match = new ItemMatch();
        
        // 确定失物和拾获物品
        if (item1.getPostType() == Item.PostType.LOST) {
            match.setLostItem(item1);
            match.setFoundItem(item2);
        } else {
            match.setLostItem(item2);
            match.setFoundItem(item1);
        }
        
        match.setMatchWeight(item1.getTotalWeight().doubleValue());
        match.setMatchedAt(LocalDateTime.now());
        match.setStatus(ItemMatch.MatchStatus.ACTIVE);
        
        return itemMatchRepository.save(match);
    }
    
    /**
     * 获取用户的匹配物品
     */
    public List<ItemMatch> getUserMatches(User user) {
        return itemMatchRepository.findByUserAndStatus(user, ItemMatch.MatchStatus.ACTIVE);
    }
    
    /**
     * 获取与指定物品匹配的所有物品
     */
    public List<Item> getMatchingItemsForItem(Item item) {
        List<ItemMatch> matches;
        if (item.getPostType() == Item.PostType.LOST) {
            matches = itemMatchRepository.findByLostItemOrderByMatchedAtDesc(item);
        } else {
            matches = itemMatchRepository.findByFoundItemOrderByMatchedAtDesc(item);
        }
        
        List<Item> matchingItems = new ArrayList<>();
        for (ItemMatch match : matches) {
            if (match.isActive()) {
                if (item.getPostType() == Item.PostType.LOST) {
                    matchingItems.add(match.getFoundItem());
                } else {
                    matchingItems.add(match.getLostItem());
                }
            }
        }
        
        return matchingItems;
    }
    
    /**
     * 获取过期物品
     */
    public List<Item> getExpiredItems(int days) {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(days);
        return itemRepository.findExpiredItems(expiryDate);
    }
    
    /**
     * 统计信息
     */
    public long countApprovedItems() {
        return itemRepository.countApprovedItems();
    }
    
    public long countLostItems() {
        return itemRepository.countLostItems();
    }
    
    public long countItems() {
        return itemRepository.count();
    }
    
    public long countFoundItems() {
        return itemRepository.countFoundItems();
    }
    
    public long countCompletedItems() {
        return itemRepository.countCompletedItems();
    }
    
    public List<Object[]> countItemsByCategory() {
        return itemRepository.countItemsByCategory();
    }
    
    public List<Object[]> countItemsByLocation() {
        return itemRepository.countItemsByLocation();
    }
    
    /**
     * 从文本中提取关键词
     */
    private String extractKeywords(String text) {
        // 简单的关键词提取，可以后续优化
        return text.toLowerCase()
                .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ")
                .trim();
    }
}
