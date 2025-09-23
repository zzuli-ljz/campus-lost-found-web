package com.campus.lostfound.config;

import com.campus.lostfound.observer.NotificationObserver;
import com.campus.lostfound.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 观察者配置类
 * 负责自动注册观察者到主题
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ObserverConfig {
    
    private final MatchingService matchingService;
    private final NotificationObserver notificationObserver;
    
    /**
     * 应用启动后自动注册观察者
     */
    @PostConstruct
    public void registerObservers() {
        log.info("开始注册观察者...");
        
        // 注册通知观察者
        matchingService.addObserver(notificationObserver);
        
        log.info("观察者注册完成，当前观察者数量: {}", getObserverCount());
    }
    
    /**
     * 获取当前观察者数量（用于测试）
     */
    private int getObserverCount() {
        // 这里可以通过反射或其他方式获取观察者数量
        // 为了简化，这里返回1（只有NotificationObserver）
        return 1;
    }
}



