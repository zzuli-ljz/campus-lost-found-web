package com.campus.lostfound.controller;

import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ItemMatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * 匹配历史控制器
 */
@Controller
@RequestMapping("/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final ItemMatchRepository itemMatchRepository;

    /**
     * 查看匹配历史
     */
    @GetMapping("/history")
    public String matchHistory(@AuthenticationPrincipal User user,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) ItemMatch.MatchStatus status,
                              Model model) {
        log.info("=== 访问匹配历史页面 ===");
        log.info("用户: {}", user != null ? user.getUsername() : "null");
        log.info("页码: {}, 大小: {}, 状态: {}", page, size, status);
        
        if (user == null) {
            log.warn("用户未登录，重定向到登录页面");
            return "redirect:/auth/login";
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            List<ItemMatch> matches;

            if (status != null) {
                log.info("按状态查找匹配记录: {}", status);
                matches = itemMatchRepository.findByUserAndStatus(user, status);
            } else {
                log.info("查找所有匹配记录");
                matches = itemMatchRepository.findByUser(user);
            }

            log.info("找到 {} 条匹配记录", matches.size());

            // 安全的分页处理
            List<ItemMatch> pageMatches = getPageMatches(matches, pageable);

            // 创建分页对象
            Page<ItemMatch> matchPage = new org.springframework.data.domain.PageImpl<>(
                    pageMatches, pageable, matches.size());

            model.addAttribute("user", user);
            model.addAttribute("matches", matchPage);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", matchPage.getTotalPages());

            log.info("匹配历史页面数据准备完成，总页数: {}", matchPage.getTotalPages());
            return "match-history";
        } catch (Exception e) {
            log.error("匹配历史页面加载失败: {}", e.getMessage(), e);
            model.addAttribute("error", "加载匹配历史失败: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 查看活跃匹配
     */
    @GetMapping("/active")
    public String activeMatches(@AuthenticationPrincipal User user,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        log.info("=== 访问活跃匹配页面 ===");
        log.info("用户: {}", user != null ? user.getUsername() : "null");
        
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            List<ItemMatch> matches = itemMatchRepository.findByUserAndStatus(user, ItemMatch.MatchStatus.ACTIVE);
            log.info("找到 {} 条活跃匹配记录", matches.size());

            // 安全的分页处理
            List<ItemMatch> pageMatches = getPageMatches(matches, pageable);

            // 创建分页对象
            Page<ItemMatch> matchPage = new org.springframework.data.domain.PageImpl<>(
                    pageMatches, pageable, matches.size());

            model.addAttribute("user", user);
            model.addAttribute("matches", matchPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", matchPage.getTotalPages());

            return "active-matches";
        } catch (Exception e) {
            log.error("活跃匹配页面加载失败: {}", e.getMessage(), e);
            model.addAttribute("error", "加载活跃匹配失败: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 查看已完成匹配
     */
    @GetMapping("/completed")
    public String completedMatches(@AuthenticationPrincipal User user,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Model model) {
        log.info("=== 访问已完成匹配页面 ===");
        log.info("用户: {}", user != null ? user.getUsername() : "null");
        
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            List<ItemMatch> matches = itemMatchRepository.findByUserAndStatus(user, ItemMatch.MatchStatus.COMPLETED);
            log.info("找到 {} 条已完成匹配记录", matches.size());

            // 安全的分页处理
            List<ItemMatch> pageMatches = getPageMatches(matches, pageable);

            // 创建分页对象
            Page<ItemMatch> matchPage = new org.springframework.data.domain.PageImpl<>(
                    pageMatches, pageable, matches.size());

            model.addAttribute("user", user);
            model.addAttribute("matches", matchPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", matchPage.getTotalPages());

            return "completed-matches";
        } catch (Exception e) {
            log.error("已完成匹配页面加载失败: {}", e.getMessage(), e);
            model.addAttribute("error", "加载已完成匹配失败: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 安全的分页处理方法
     */
    private List<ItemMatch> getPageMatches(List<ItemMatch> matches, Pageable pageable) {
        if (matches == null || matches.isEmpty()) {
            log.info("匹配记录为空，返回空列表");
            return new ArrayList<>();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matches.size());
        
        // 确保start不会超出范围
        if (start >= matches.size()) {
            log.info("起始位置超出范围，返回空列表");
            return new ArrayList<>();
        }

        log.info("分页处理: start={}, end={}, 总数量={}", start, end, matches.size());
        return matches.subList(start, end);
    }
}