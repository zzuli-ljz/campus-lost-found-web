package com.campus.lostfound.controller;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ClaimController {

    private final ClaimService claimService;
    private final com.campus.lostfound.repository.ClaimRepository claimRepository;

    @GetMapping("/claims/apply")
    public String apply(@RequestParam Long itemId,
                        @AuthenticationPrincipal User user,
                        Model model) {
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("user", user);
        model.addAttribute("itemId", itemId);
        return "claim-apply";
    }

    @PostMapping("/claims/apply")
    public String submit(@RequestParam Long itemId,
                         @RequestParam String verificationDetail,
                         @AuthenticationPrincipal User user,
                         Model model,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (user == null) return "redirect:/auth/login";
        try {
            Claim c = claimService.submitClaim(itemId, user, verificationDetail);
            redirectAttributes.addFlashAttribute("success", "认领申请已提交，等待发布者确认");
            return "redirect:/items/" + c.getItem().getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("itemId", itemId);
            return "claim-apply";
        }
    }

    /** 发布者确认认领同意/驳回 */
    @PostMapping("/claims/{id}/owner-confirm")
    public String ownerConfirm(@PathVariable Long id,
                               @RequestParam boolean agree,
                               @RequestParam(required = false) String note,
                               @AuthenticationPrincipal User user,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (user == null) return "redirect:/auth/login";
        claimService.ownerConfirm(id, user, agree, note);
        redirectAttributes.addFlashAttribute("success", agree?"已确认该认领，等待管理员审核":"已驳回该认领");
        // 返回物品详情
        com.campus.lostfound.entity.Claim c = claimRepository.findById(id).orElse(null);
        Long itemId = c != null && c.getItem()!=null ? c.getItem().getId() : null;
        return itemId!=null ? "redirect:/items/"+itemId : "redirect:/";
    }

    @GetMapping("/admin/claims")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminClaims(@AuthenticationPrincipal User admin, Model model) {
        model.addAttribute("user", admin);
        model.addAttribute("claims", claimService.listPending());
        return "admin-claims";
    }

    @PostMapping("/admin/claims/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public String review(@PathVariable Long id,
                         @RequestParam boolean pass,
                         @RequestParam(required = false) String note,
                         @AuthenticationPrincipal User admin) {
        try {
            log.info("[ADMIN REVIEW] claimId={}, pass={}, admin={}", id, pass, admin != null ? admin.getUsername() : "null");
            claimService.reviewClaim(id, admin, pass, note);
            log.info("[ADMIN REVIEW] success claimId={}", id);
            return "redirect:/admin/claims?ok=" + (pass ? "approved" : "rejected");
        } catch (Exception e) {
            log.error("[ADMIN REVIEW] error claimId={}, msg=", id, e);
            return "redirect:/admin/claims?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /** 便捷GET：管理员通过 */
    @GetMapping("/admin/claims/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveByGet(@PathVariable Long id,
                               @AuthenticationPrincipal User admin) {
        return review(id, true, null, admin);
    }

    /** 便捷GET：管理员拒绝 */
    @GetMapping("/admin/claims/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectByGet(@PathVariable Long id,
                              @AuthenticationPrincipal User admin) {
        return review(id, false, null, admin);
    }

    @PostMapping("/admin/claims/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public String complete(@PathVariable Long id,
                           @AuthenticationPrincipal User admin) {
        claimService.completeClaim(id, admin);
        return "redirect:/admin/claims";
    }
}


