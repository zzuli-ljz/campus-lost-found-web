package com.campus.lostfound.controller;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ItemService itemService;
    private final com.campus.lostfound.service.NotificationService notificationService;
    private final com.campus.lostfound.service.ReportService reportService;
    private final com.campus.lostfound.service.UserService userService;
    private final com.campus.lostfound.repository.UserRepository userRepository;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal User admin, Model model) {
        List<Item> pending = itemService.getPendingItems();
        model.addAttribute("user", admin);
        model.addAttribute("pending", pending);
        return "admin";
    }

    @GetMapping("/approve")
    public String approve(@RequestParam Long id, @AuthenticationPrincipal User admin) {
        itemService.approveItem(id, admin, "审核通过");
        return "redirect:/admin";
    }

    @GetMapping("/reject")
    public String reject(@RequestParam Long id, @AuthenticationPrincipal User admin) {
        itemService.rejectItem(id, admin, "管理员驳回");
        return "redirect:/admin";
    }

    /** 清理所有物品（测试数据） */
    @PostMapping("/cleanup-items")
    public String cleanupItems(@AuthenticationPrincipal User admin) {
        if (admin == null || admin.getRole() != User.UserRole.ADMIN) {
            return "redirect:/";
        }
        itemService.deleteAllItems();
        return "redirect:/admin";
    }

    @GetMapping("/users")
    public String users(@AuthenticationPrincipal User admin, Model model) {
        model.addAttribute("user", admin);
        model.addAttribute("users", userRepository.findAll());
        return "admin-users";
    }

    @org.springframework.web.bind.annotation.PostMapping("/users/{id}/toggle")
    public String toggleUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users";
    }

    @org.springframework.web.bind.annotation.PostMapping("/users/{id}/reset-password")
    public String resetPassword(@org.springframework.web.bind.annotation.PathVariable Long id,
                                @org.springframework.web.bind.annotation.RequestParam(defaultValue = "user123") String newPassword) {
        // 简化：使用管理员重置时跳过旧密码校验
        com.campus.lostfound.entity.User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        // 复用服务的修改密码：设置旧密码为新密码直接写入简化，这里直接编码写入更直接
        org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        u.setPassword(encoder.encode(newPassword));
        userRepository.save(u);
        return "redirect:/admin/users";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal User admin, Model model) {
        model.addAttribute("user", admin);
        model.addAttribute("reports", reportService.listPending());
        return "admin-reports";
    }

    @org.springframework.web.bind.annotation.PostMapping("/reports/{id}/process")
    public String processReport(@org.springframework.web.bind.annotation.PathVariable Long id,
                                @org.springframework.web.bind.annotation.RequestParam String note,
                                @AuthenticationPrincipal User admin) {
        reportService.process(id, admin, note);
        return "redirect:/admin/reports";
    }

    @org.springframework.web.bind.annotation.PostMapping("/reports/{id}/reject")
    public String rejectReport(@org.springframework.web.bind.annotation.PathVariable Long id,
                               @org.springframework.web.bind.annotation.RequestParam(required = false) String reason,
                               @AuthenticationPrincipal User admin) {
        reportService.rejectReport(id, admin, reason);
        return "redirect:/admin/reports";
    }

    @org.springframework.web.bind.annotation.PostMapping("/reports/{id}/accept-delete")
    public String acceptReportAndDelete(@org.springframework.web.bind.annotation.PathVariable Long id,
                                        @org.springframework.web.bind.annotation.RequestParam(required = false) String reason,
                                        @AuthenticationPrincipal User admin) {
        reportService.acceptReportAndDeleteItem(id, admin, reason);
        return "redirect:/admin/reports";
    }

    @GetMapping("/announcements")
    public String announcements(@AuthenticationPrincipal User admin, Model model) {
        model.addAttribute("user", admin);
        return "admin-announcements";
    }

    @GetMapping("/items")
    public String items(@AuthenticationPrincipal User admin, 
                       @RequestParam(value = "type", defaultValue = "ALL") String type,
                       @RequestParam(value = "category", defaultValue = "ALL") String category,
                       @RequestParam(value = "keyword", defaultValue = "") String keyword,
                       Model model) {
        List<Item> allItems = itemService.getAllApprovedItems();
        
        // 筛选失物/拾物
        if (!"ALL".equals(type)) {
            Item.PostType postType = Item.PostType.valueOf(type);
            allItems = allItems.stream()
                    .filter(item -> item.getPostType() == postType)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 筛选分类
        if (!"ALL".equals(category)) {
            Item.ItemCategory itemCategory = Item.ItemCategory.valueOf(category);
            allItems = allItems.stream()
                    .filter(item -> item.getCategory() == itemCategory)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 搜索关键词
        if (!keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            allItems = allItems.stream()
                    .filter(item -> 
                        item.getTitle().toLowerCase().contains(lowerKeyword) ||
                        item.getDescription().toLowerCase().contains(lowerKeyword) ||
                        item.getLocation().getDescription().toLowerCase().contains(lowerKeyword) ||
                        item.getDetailedLocation().getDescription().toLowerCase().contains(lowerKeyword)
                    )
                    .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("user", admin);
        model.addAttribute("items", allItems);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "admin-items";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, @AuthenticationPrincipal User admin) {
        if (admin == null || admin.getRole() != User.UserRole.ADMIN) {
            return "redirect:/";
        }
        itemService.deleteItem(id);
        return "redirect:/admin/items";
    }

    @GetMapping("/admins")
    public String admins(@AuthenticationPrincipal User admin, Model model) {
        model.addAttribute("user", admin);
        model.addAttribute("admins", userRepository.findByRole(User.UserRole.ADMIN));
        model.addAttribute("users", userRepository.findByRole(User.UserRole.USER));
        return "admin-admins";
    }

    @org.springframework.web.bind.annotation.PostMapping("/announcements")
    public String postAnnouncement(@AuthenticationPrincipal User admin,
                                   @org.springframework.web.bind.annotation.RequestParam String title,
                                   @org.springframework.web.bind.annotation.RequestParam String content) {
        notificationService.broadcastAdminAlert(title, content);
        return "redirect:/admin/announcements";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admins/{id}/promote")
    public String promote(@org.springframework.web.bind.annotation.PathVariable Long id) {
        com.campus.lostfound.entity.User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        u.setRole(User.UserRole.ADMIN);
        userRepository.save(u);
        return "redirect:/admin/admins";
    }

    @org.springframework.web.bind.annotation.PostMapping("/admins/{id}/demote")
    public String demote(@org.springframework.web.bind.annotation.PathVariable Long id) {
        com.campus.lostfound.entity.User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (u.getUsername().equals("admin")) {
            return "redirect:/admin/admins"; // 保护默认admin
        }
        u.setRole(User.UserRole.USER);
        userRepository.save(u);
        return "redirect:/admin/admins";
    }
}


