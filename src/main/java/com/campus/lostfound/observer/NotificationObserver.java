package com.campus.lostfound.observer;

import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知观察者
 * 观察者模式中的具体观察者实现
 * 负责处理匹配相关的通知
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationObserver implements ItemMatchObserver {
    
    private final NotificationService notificationService;
    
    @Override
    public void onMatchFound(ItemMatch match) {
        log.info("通知观察者处理匹配找到事件: 失物{} <-> 拾获{}", 
                match.getLostItem().getId(), match.getFoundItem().getId());
        
        // 调用原有的通知服务方法，保持功能不变
        notificationService.notifyWeightBasedMatching(match);
    }
    
    @Override
    public void onMatchUpdated(ItemMatch match) {
        log.info("通知观察者处理匹配更新事件: 匹配ID{}", match.getId());
        
        // 可以在这里添加匹配更新时的通知逻辑
        // 目前保持原有功能不变
    }
    
    @Override
    public void onMatchCancelled(ItemMatch match) {
        log.info("通知观察者处理匹配取消事件: 匹配ID{}", match.getId());
        
        // 可以在这里添加匹配取消时的通知逻辑
        // 目前保持原有功能不变
    }
}




