package com.campus.lostfound.config;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ChatService;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全局模型属性配置
 * 为所有页面添加通用的模型属性
 */
@Component
@ControllerAdvice
@RequiredArgsConstructor
public class ModelAttributeConfig {

    private final NotificationService notificationService;
    private final ChatService chatService;

    /**
     * 为所有页面添加未读通知数量
     */
    @ModelAttribute("unreadCount")
    public Long getUnreadCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return 0L;
        }
        return Long.valueOf(notificationService.getUnreadCount(user));
    }

    /**
     * 为所有页面添加未读聊天消息数量
     */
    @ModelAttribute("unreadChatCount")
    public Long getUnreadChatCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return 0L;
        }
        return Long.valueOf(chatService.getUnreadChatCount(user));
    }
}
