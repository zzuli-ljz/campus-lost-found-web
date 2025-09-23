package com.campus.lostfound.controller;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.Location;
import com.campus.lostfound.entity.DetailedLocation;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品控制器
 */
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    
    private final ItemService itemService;
    private final NotificationService notificationService;
    private final com.campus.lostfound.repository.ClaimRepository claimRepository;
    
    /**
     * 发布物品页面
     */
    @GetMapping("/post")
    public String postItemPage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("item", new Item());
        model.addAttribute("categories", Item.ItemCategory.values());
        model.addAttribute("postTypes", Item.PostType.values());
        model.addAttribute("locations", Location.values());
        
        return "post-item";
    }
    
    /**
     * 提交发布物品
     */
    @PostMapping("/post")
    public String postItem(@RequestParam("title") String title,
                          @RequestParam("description") String description,
                          @RequestParam("category") String categoryStr,
                          @RequestParam("postType") String postTypeStr,
                          @RequestParam("lostFoundTime") String lostFoundTimeStr,
                          @RequestParam("location") String locationStr,
                          @RequestParam("detailedLocation") String detailedLocationStr,
                          @RequestParam(value = "images", required = false) List<MultipartFile> images,
                          @AuthenticationPrincipal User user,
                          RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/login";
        }
        
        // 参数验证
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "标题不能为空");
            return "redirect:/items/post";
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "描述不能为空");
            return "redirect:/items/post";
        }
        
        try {
            // 转换枚举类型
            Item.ItemCategory category = Item.ItemCategory.valueOf(categoryStr);
            Item.PostType postType = Item.PostType.valueOf(postTypeStr);
            Location location = convertToLocation(locationStr);
            DetailedLocation detailedLocation = convertToDetailedLocation(detailedLocationStr);
            
            // 解析时间
            LocalDateTime lostFoundTime = LocalDateTime.parse(lostFoundTimeStr.replace(" ", "T"));
            
            // 处理图片
            List<MultipartFile> validImages = new ArrayList<>();
            if (images != null) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        validImages.add(image);
                    }
                }
            }
            
            Item savedItem = itemService.postItem(
                    title,
                    description,
                    category,
                    postType,
                    location,
                    detailedLocation,
                    lostFoundTime,
                    user,
                    validImages
            );
            
            // 物品创建后等待审核，不进行匹配
            // 匹配将在物品审核通过后进行
            
            redirectAttributes.addFlashAttribute("success", "物品发布成功，等待管理员审核");
            return "redirect:/items/my-items";
            
        } catch (Exception e) {
            log.error("发布物品失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "发布失败: " + e.getMessage());
            return "redirect:/items/post";
        }
    }
    
    /**
     * 物品详情页面
     */
    @GetMapping("/{id}")
    public String itemDetail(@PathVariable Long id, 
                           @AuthenticationPrincipal User user, 
                           Model model) {
        
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 只有已审核的物品才能查看详情
        if (!item.isApproved()) {
            if (user == null || !user.getId().equals(item.getOwner().getId()) && 
                user.getRole() != User.UserRole.ADMIN) {
                return "redirect:/";
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("item", item);
        try {
            java.util.List<com.campus.lostfound.entity.Claim> claims = claimRepository.findByItemOrderByCreatedAtDesc(item);
            model.addAttribute("claims", claims);
        } catch (Exception ignored) {}
        
        return "item-detail";
    }
    
    /**
     * 我的物品页面
     */
    @GetMapping("/my-items")
    public String myItems(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Item> items = itemService.getUserItems(user);
        model.addAttribute("user", user);
        model.addAttribute("items", items);
        
        return "my-items";
    }

    /** 已完成列表（与我相关：我发布的或我参与认领的且已完成） */
    @GetMapping("/completed")
    public String completed(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        // 我发布且完成
        java.util.List<Item> mineCompleted = itemService.getUserItems(user).stream()
                .filter(i -> i.getStatus() == Item.ItemStatus.COMPLETED)
                .toList();
        // 我认领且完成
        java.util.List<com.campus.lostfound.entity.Claim> myClaims = claimRepository.findByClaimantOrderByCreatedAtDesc(user);
        java.util.List<Item> claimedCompleted = myClaims.stream()
                .filter(c -> c.getStatus() == com.campus.lostfound.entity.Claim.ClaimStatus.COMPLETED)
                .map(com.campus.lostfound.entity.Claim::getItem)
                .distinct()
                .toList();
        java.util.LinkedHashSet<Item> all = new java.util.LinkedHashSet<>();
        all.addAll(mineCompleted);
        all.addAll(claimedCompleted);
        model.addAttribute("user", user);
        model.addAttribute("items", new java.util.ArrayList<>(all));
        return "completed";
    }
    
    /**
     * 编辑物品页面
     */
    @GetMapping("/{id}/edit")
    public String editItemPage(@PathVariable Long id, 
                             @AuthenticationPrincipal User user, 
                             Model model) {
        
        if (user == null) {
            return "redirect:/login";
        }
        
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 只有物品所有者可以编辑
        if (!user.getId().equals(item.getOwner().getId())) {
            return "redirect:/items/my-items";
        }
        
        // 已审核的物品不能编辑
        if (item.isApproved()) {
            return "redirect:/items/my-items";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("item", item);
        model.addAttribute("categories", Item.ItemCategory.values());
        model.addAttribute("postTypes", Item.PostType.values());
        
        return "edit-item";
    }
    
    /**
     * 更新物品
     */
    @PostMapping("/{id}/edit")
    public String updateItem(@PathVariable Long id,
                           @Valid @ModelAttribute("item") Item item,
                           BindingResult bindingResult,
                           @RequestParam("lostFoundTime") String lostFoundTimeStr,
                           @AuthenticationPrincipal User user,
                           RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/login";
        }
        
        Item existingItem = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 只有物品所有者可以编辑
        if (!user.getId().equals(existingItem.getOwner().getId())) {
            return "redirect:/items/my-items";
        }
        
        if (bindingResult.hasErrors()) {
            return "edit-item";
        }
        
        try {
            LocalDateTime lostFoundTime = LocalDateTime.parse(lostFoundTimeStr.replace(" ", "T"));
            
            existingItem.setTitle(item.getTitle());
            existingItem.setDescription(item.getDescription());
            existingItem.setCategory(item.getCategory());
            existingItem.setPostType(item.getPostType());
            existingItem.setLocation(item.getLocation());
            existingItem.setDetailedLocation(item.getDetailedLocation());
            existingItem.setLostFoundTime(lostFoundTime);
            
            itemService.save(existingItem);
            
            redirectAttributes.addFlashAttribute("success", "物品更新成功");
            return "redirect:/items/my-items";
            
        } catch (Exception e) {
            log.error("更新物品失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
            return "redirect:/items/" + id + "/edit";
        }
    }
    
    /**
     * 获取用户的匹配物品
     */
    @GetMapping("/matches")
    public String getMatches(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        
        List<ItemMatch> matches = itemService.getUserMatches(user);
        model.addAttribute("user", user);
        model.addAttribute("matches", matches);
        
        return "matches";
    }
    
    /**
     * 获取与指定物品匹配的所有物品
     */
    @GetMapping("/{id}/matches")
    public String getItemMatches(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 只有物品所有者可以查看匹配
        if (!user.getId().equals(item.getOwner().getId())) {
            return "redirect:/items/my-items";
        }
        
        List<Item> matchingItems = itemService.getMatchingItemsForItem(item);
        model.addAttribute("user", user);
        model.addAttribute("item", item);
        model.addAttribute("matchingItems", matchingItems);
        
        return "item-matches";
    }
    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id,
                           @AuthenticationPrincipal User user,
                           RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/login";
        }
        
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        // 只有物品所有者或管理员可以删除
        if (!user.getId().equals(item.getOwner().getId()) && 
            user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/items/my-items";
        }
        
        try {
            itemService.deleteItem(id);
            redirectAttributes.addFlashAttribute("success", "物品删除成功");
        } catch (Exception e) {
            log.error("删除物品失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        
        return "redirect:/items/my-items";
    }

    /**
     * 用户确认已找到（失物找回/拾物已交还）
     */
    @PostMapping("/{id}/confirm-found")
    public String confirmFound(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        if (!user.getId().equals(item.getOwner().getId())) {
            return "redirect:/items/my-items";
        }
        try {
            item.setStatus(Item.ItemStatus.CLAIMED);
            item.setApproved(true);
            itemService.save(item);
            redirectAttributes.addFlashAttribute("success", "已确认：该条记录已完成");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/items/my-items";
    }
    
    /**
     * 将地点字符串转换为Location枚举
     */
    private Location convertToLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) {
            return Location.OTHER;
        }
        
        // 如果是枚举常量，直接转换
        try {
            return Location.valueOf(locationStr);
        } catch (IllegalArgumentException e) {
            // 如果是中文描述，根据描述查找对应的枚举
            for (Location location : Location.values()) {
                if (location.getDescription().equals(locationStr)) {
                    return location;
                }
            }
            // 如果找不到匹配的，返回OTHER
            return Location.OTHER;
        }
    }
    
    /**
     * 将详细地点字符串转换为DetailedLocation枚举
     */
    private DetailedLocation convertToDetailedLocation(String detailedLocationStr) {
        if (detailedLocationStr == null || detailedLocationStr.isEmpty()) {
            return DetailedLocation.OTHER_UNKNOWN;
        }
        
        // 如果是枚举常量，直接转换
        try {
            return DetailedLocation.valueOf(detailedLocationStr);
        } catch (IllegalArgumentException e) {
            // 如果是中文描述，根据描述查找对应的枚举
            for (DetailedLocation detailedLocation : DetailedLocation.values()) {
                if (detailedLocation.getDescription().equals(detailedLocationStr)) {
                    return detailedLocation;
                }
            }
            // 如果找不到匹配的，返回OTHER_UNKNOWN
            return DetailedLocation.OTHER_UNKNOWN;
        }
    }

    /**
     * 确认物品完成
     */
    @PostMapping("/{id}/confirm-completion")
    public String confirmCompletion(@PathVariable Long id, @AuthenticationPrincipal User user) {
        log.info("=== ItemController.confirmCompletion 被调用 ===");
        log.info("物品ID: {}", id);
        log.info("用户: {}", user != null ? user.getUsername() : "null");
        
        if (user == null) {
            log.warn("用户未登录，重定向到登录页面");
            return "redirect:/auth/login";
        }
        
        try {
            log.info("开始调用ItemService.confirmItemCompletion...");
            itemService.confirmItemCompletion(id, user);
            log.info("ItemService.confirmItemCompletion 调用成功，重定向到成功页面");
            return "redirect:/items/my-items?success=completed";
        } catch (Exception e) {
            log.error("=== ItemController.confirmCompletion 异常 ===");
            log.error("物品ID: {}", id);
            log.error("用户ID: {}", user.getId());
            log.error("异常类型: {}", e.getClass().getSimpleName());
            log.error("异常信息: {}", e.getMessage());
            log.error("异常堆栈: ", e);
            return "redirect:/items/my-items?error=completion_failed";
        }
    }
    
    /**
     * 测试确认完成功能
     */
    @GetMapping("/test-confirm/{id}")
    public String testConfirm(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        if (user == null) return "redirect:/auth/login";
        
        try {
            // 查找物品
            Optional<Item> itemOpt = itemService.findById(id);
            if (itemOpt.isEmpty()) {
                model.addAttribute("error", "物品不存在");
                return "error";
            }
            Item item = itemOpt.get();
            
            model.addAttribute("item", item);
            model.addAttribute("user", user);
            model.addAttribute("canComplete", item.getOwner().getId().equals(user.getId()) && 
                             item.getApproved() && 
                             item.getStatus() != Item.ItemStatus.COMPLETED &&
                             item.getStatus() != Item.ItemStatus.PENDING_APPROVAL);
            
            return "test-confirm";
        } catch (Exception e) {
            model.addAttribute("error", "获取物品信息失败: " + e.getMessage());
            return "error";
        }
    }
}