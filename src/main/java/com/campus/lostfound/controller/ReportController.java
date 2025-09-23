package com.campus.lostfound.controller;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    @GetMapping("/items/report")
    public String report(@RequestParam Long itemId,
                         @AuthenticationPrincipal User user,
                         Model model) {
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("user", user);
        model.addAttribute("itemId", itemId);
        return "report";
    }

    @PostMapping("/items/report")
    public String submitReport(@RequestParam Long itemId,
                               @RequestParam String reason,
                               @AuthenticationPrincipal User user,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (user == null) return "redirect:/auth/login";
        reportService.submit(itemId, user, reason);
        redirectAttributes.addFlashAttribute("success", "举报已提交，我们会尽快处理");
        return "redirect:/items/" + itemId;
    }
}


