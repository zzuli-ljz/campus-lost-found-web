package com.campus.lostfound.service;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.observer.ItemMatchObserver;
import com.campus.lostfound.observer.ItemMatchSubject;
import com.campus.lostfound.repository.ItemMatchRepository;
import com.campus.lostfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchingService implements ItemMatchSubject {

    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final NotificationService notificationService;
    
    // 观察者列表 - 使用线程安全的CopyOnWriteArrayList
    private final List<ItemMatchObserver> observers = new CopyOnWriteArrayList<>();

    @Transactional
    public List<ItemMatch> findAndCreateMatches(Item item) {
        log.info("开始为物品 {} (ID: {}) 查找匹配项", item.getTitle(), item.getId());
        List<ItemMatch> createdMatches = new ArrayList<>();

        Item.PostType oppositeType = item.getPostType() == Item.PostType.LOST ?
                Item.PostType.FOUND : Item.PostType.LOST;

        // 查找相同权重、相同地点、相反类型的已审核物品
        List<Item> candidates = itemRepository.findMatchingItemsByWeight(
                oppositeType, item.getTotalWeight(), item.getLocation());

        log.info("找到 {} 个候选匹配物品", candidates.size());

        for (Item candidate : candidates) {
            // 检查是否已经匹配过
            if (!itemMatchRepository.existsMatchBetweenItems(item, candidate)) {
                double matchScore = calculateMatchScore(item, candidate);
                if (matchScore >= 0.7) { // 假设匹配度阈值为0.7
                    ItemMatch match = createMatch(item, candidate, matchScore);
                    createdMatches.add(match);
                    // 使用观察者模式通知所有观察者
                    notifyMatchFound(match);
                    log.info("创建匹配记录: item1={}, item2={}, score={}", item.getId(), candidate.getId(), matchScore);
                }
            }
        }
        return createdMatches;
    }

    @Transactional
    public ItemMatch createMatch(Item item1, Item item2, double matchScore) {
        ItemMatch match = new ItemMatch();
        if (item1.getPostType() == Item.PostType.LOST) {
            match.setLostItem(item1);
            match.setFoundItem(item2);
        } else {
            match.setLostItem(item2);
            match.setFoundItem(item1);
        }
        match.setMatchWeight(matchScore);
        match.setMatchedAt(LocalDateTime.now());
        match.setStatus(ItemMatch.MatchStatus.ACTIVE);
        return itemMatchRepository.save(match);
    }

    private double calculateMatchScore(Item item1, Item item2) {
        double score = 0.0;

        // 基础权重匹配 (40%)
        if (item1.getTotalWeight().equals(item2.getTotalWeight())) {
            score += 0.4;
        }

        // 地点匹配 (30%)
        if (item1.getLocation().equals(item2.getLocation())) {
            score += 0.3;
        }

        // 详细地点匹配 (20%)
        if (item1.getDetailedLocation() != null && item2.getDetailedLocation() != null &&
                item1.getDetailedLocation().equals(item2.getDetailedLocation())) {
            score += 0.2;
        }

        // 类别匹配 (10%)
        if (item1.getCategory().equals(item2.getCategory())) {
            score += 0.1;
        }
        return score;
    }

    public List<ItemMatch> getMatchesForItem(Item item) {
        List<ItemMatch> matches;
        if (item.getPostType() == Item.PostType.LOST) {
            matches = itemMatchRepository.findByLostItemOrderByMatchedAtDesc(item);
        } else {
            matches = itemMatchRepository.findByFoundItemOrderByMatchedAtDesc(item);
        }
        return matches;
    }
    
    // ========== 观察者模式实现 ==========
    
    @Override
    public void addObserver(ItemMatchObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            log.debug("添加观察者: {}", observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void removeObserver(ItemMatchObserver observer) {
        if (observer != null) {
            observers.remove(observer);
            log.debug("移除观察者: {}", observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void notifyMatchFound(ItemMatch match) {
        log.debug("通知 {} 个观察者匹配已找到", observers.size());
        for (ItemMatchObserver observer : observers) {
            try {
                observer.onMatchFound(match);
            } catch (Exception e) {
                log.error("通知观察者时发生错误: {}", e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void notifyMatchUpdated(ItemMatch match) {
        log.debug("通知 {} 个观察者匹配已更新", observers.size());
        for (ItemMatchObserver observer : observers) {
            try {
                observer.onMatchUpdated(match);
            } catch (Exception e) {
                log.error("通知观察者时发生错误: {}", e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void notifyMatchCancelled(ItemMatch match) {
        log.debug("通知 {} 个观察者匹配已取消", observers.size());
        for (ItemMatchObserver observer : observers) {
            try {
                observer.onMatchCancelled(match);
            } catch (Exception e) {
                log.error("通知观察者时发生错误: {}", e.getMessage(), e);
            }
        }
    }
}
