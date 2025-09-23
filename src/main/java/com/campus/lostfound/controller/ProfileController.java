package com.campus.lostfound.controller;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        if (user == null) return "redirect:/login";
        
        model.addAttribute("user", user);
        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        model.addAttribute("profileUpdateForm", new ProfileUpdateForm());
        
        return "profile";
    }
    
    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal User user,
                                @Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (user == null) return "redirect:/login";
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "密码格式不正确");
            return "redirect:/profile";
        }
        
        try {
            // 验证旧密码
            if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "旧密码不正确");
                return "redirect:/profile";
            }
            
            // 更新密码
            userService.changePassword(user.getId(), form.getOldPassword(), form.getNewPassword());
            
            redirectAttributes.addFlashAttribute("success", "密码修改成功");
            log.info("用户{}修改密码成功", user.getUsername());
            
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "密码修改失败: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                               @Valid @ModelAttribute("profileUpdateForm") ProfileUpdateForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (user == null) return "redirect:/login";
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "输入信息有误");
            return "redirect:/profile";
        }
        
        try {
            // 更新用户信息
            user.setDisplayName(form.getDisplayName());
            user.setEmail(form.getEmail());
            user.setPhone(form.getPhone());
            user.setStudentId(form.getStudentId());
            
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("success", "个人信息更新成功");
            log.info("用户{}更新个人信息成功", user.getUsername());
            
        } catch (Exception e) {
            log.error("更新个人信息失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/profile/logout")
    public String logout() {
        return "redirect:/logout";
    }
    
    // 密码修改表单
    public static class PasswordChangeForm {
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;
        
        // Getters and Setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
    
    // 个人信息更新表单
    public static class ProfileUpdateForm {
        private String displayName;
        private String email;
        private String phone;
        private String studentId;
        
        // Getters and Setters
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
    }
}


