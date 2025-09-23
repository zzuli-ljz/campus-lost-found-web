package com.campus.lostfound.controller;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 认证控制器
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 登录页面
     */
    @GetMapping("/auth/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        
        if (logout != null) {
            model.addAttribute("logout", "您已成功退出登录");
        }
        
        return "login";
    }
    
    /**
     * 注册页面
     */
    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    /**
     * 处理注册
     */
    @PostMapping("/auth/register")
    public String register(@RequestParam String username,
                          @RequestParam String displayName,
                          @RequestParam String studentId,
                          @RequestParam String email,
                          @RequestParam String phone,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          RedirectAttributes redirectAttributes) {
        
        try {
            // 验证密码确认
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "两次输入的密码不一致");
                return "redirect:/auth/register";
            }
            
            // 注册用户
            User user = userService.register(username, displayName, studentId, email, phone, password, User.UserRole.USER);
            
            redirectAttributes.addFlashAttribute("success", "注册成功！请登录您的账户");
            return "redirect:/auth/login?registered=true";
            
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "注册失败: " + e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
