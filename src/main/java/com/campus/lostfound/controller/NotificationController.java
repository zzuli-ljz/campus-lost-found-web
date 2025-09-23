package com.campus.lostfound.controller;

import com.campus.lostfound.entity.Notification;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal User user,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(user, pageable);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    @GetMapping("/notifications/mark-read")
    public String markRead(@RequestParam Long id,
                           @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        notificationService.markAsRead(id, user);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllRead(@AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/delete-all")
    public String deleteAll(@AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        notificationService.deleteAll(user);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String deleteOne(@PathVariable Long id,
                            @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        notificationService.deleteNotification(id, user);
        return "redirect:/notifications";
    }
}

