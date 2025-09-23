package com.campus.lostfound.controller;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ItemMatchRepository;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 数据库信息控制器
 */
@Controller
@RequestMapping("/admin/database")
@RequiredArgsConstructor
@Slf4j
public class DatabaseController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;

    /**
     * 查看数据库统计信息
     */
    @GetMapping("/stats")
    public String databaseStats(@AuthenticationPrincipal User user, Model model) {
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/auth/login";
        }

        try {
            // 用户统计
            long userCount = userRepository.count();
            long adminCount = userRepository.countActiveAdmins();
            long normalUserCount = userCount - adminCount;

            // 物品统计
            long itemCount = itemRepository.count();
            long approvedItemCount = itemRepository.countApprovedItems();
            long completedItemCount = itemRepository.countCompletedItems();
            long lostItemCount = itemRepository.countLostItems();
            long foundItemCount = itemRepository.countFoundItems();

            // 匹配统计
            long matchCount = itemMatchRepository.count();
            long activeMatchCount = itemMatchRepository.findByStatusOrderByMatchedAtDesc(ItemMatch.MatchStatus.ACTIVE).size();
            long completedMatchCount = itemMatchRepository.findByStatusOrderByMatchedAtDesc(ItemMatch.MatchStatus.COMPLETED).size();

            // 最近数据
            List<User> recentUsers = userRepository.findRecentUsers(org.springframework.data.domain.PageRequest.of(0, 5));
            List<Item> recentItems = itemRepository.findByApprovedTrueOrderByCreatedAtDesc().stream().limit(5).collect(java.util.stream.Collectors.toList());
            List<ItemMatch> recentMatches = itemMatchRepository.findByStatusOrderByMatchedAtDesc(ItemMatch.MatchStatus.ACTIVE).stream().limit(5).collect(java.util.stream.Collectors.toList());

            model.addAttribute("userCount", userCount);
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("normalUserCount", normalUserCount);
            model.addAttribute("itemCount", itemCount);
            model.addAttribute("approvedItemCount", approvedItemCount);
            model.addAttribute("completedItemCount", completedItemCount);
            model.addAttribute("lostItemCount", lostItemCount);
            model.addAttribute("foundItemCount", foundItemCount);
            model.addAttribute("matchCount", matchCount);
            model.addAttribute("activeMatchCount", activeMatchCount);
            model.addAttribute("completedMatchCount", completedMatchCount);
            model.addAttribute("recentUsers", recentUsers);
            model.addAttribute("recentItems", recentItems);
            model.addAttribute("recentMatches", recentMatches);

            return "admin-database-stats";
        } catch (Exception e) {
            log.error("获取数据库统计信息失败: {}", e.getMessage(), e);
            model.addAttribute("error", "获取数据库信息失败: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 查看表结构信息
     */
    @GetMapping("/tables")
    public String tableInfo(@AuthenticationPrincipal User user, Model model) {
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/auth/login";
        }

        try {
            // 这里可以添加查看表结构的逻辑
            model.addAttribute("message", "表结构信息功能待实现");
            return "admin-database-tables";
        } catch (Exception e) {
            log.error("获取表结构信息失败: {}", e.getMessage(), e);
            model.addAttribute("error", "获取表结构信息失败: " + e.getMessage());
            return "error";
        }
    }
}
