package com.campus.lostfound.controller;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 首页控制器
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    
    private final ItemService itemService;
    private final NotificationService notificationService;
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal User user, 
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        // 获取最新发布的物品
        Page<Item> recentItems = itemService.getRecentItems(pageable);
        
        // 获取热门物品
        Page<Item> popularItems = itemService.getPopularItems(PageRequest.of(0, 5));
        
        // 获取统计数据
        long totalItems = itemService.countApprovedItems();
        long lostItems = itemService.countLostItems();
        long foundItems = itemService.countFoundItems();
        long completedItems = itemService.countCompletedItems();
        
        // 获取用户的未读通知数量
        int unreadCount = 0;
        if (user != null) {
            unreadCount = notificationService.getUnreadCount(user);
        }
        
        model.addAttribute("user", user);
        model.addAttribute("recentItems", recentItems);
        model.addAttribute("popularItems", popularItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("lostItems", lostItems);
        model.addAttribute("foundItems", foundItems);
        model.addAttribute("completedItems", completedItems);
        model.addAttribute("unreadCount", unreadCount);
        
        return "home";
    }
    
    /**
     * 搜索页面
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Item.PostType postType,
                        @RequestParam(required = false) Item.ItemCategory category,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @AuthenticationPrincipal User user,
                        Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Item> items;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (postType != null || category != null) {
                    items = itemService.advancedSearch(keyword.trim(), postType, category, pageable);
                } else {
                    items = itemService.searchItems(keyword.trim(), pageable);
                }
            } else {
                items = itemService.getApprovedItems(pageable);
            }
            
            model.addAttribute("user", user);
            model.addAttribute("items", items);
            model.addAttribute("keyword", keyword);
            model.addAttribute("postType", postType);
            model.addAttribute("category", category);
            model.addAttribute("categories", Item.ItemCategory.values());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", items.getTotalPages());
            
            return "search";
        } catch (Exception e) {
            log.error("搜索功能出现错误: {}", e.getMessage(), e);
            model.addAttribute("error", "搜索功能暂时不可用，请稍后再试");
            model.addAttribute("user", user);
            model.addAttribute("categories", Item.ItemCategory.values());
            return "search";
        }
    }
    
    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String about(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "about";
    }
    
    /**
     * 帮助页面
     */
    @GetMapping("/help")
    public String help(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "help";
    }
}
