package com.campus.lostfound.controller;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 测试控制器
 * 用于调试和测试
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final UserService userService;
    
    /**
     * 测试页面 - 显示所有用户
     */
    @GetMapping("/test-users")
    public String testUsers(Model model) {
        try {
            // 测试查找admin用户
            var adminUser = userService.findByUsername("admin");
            log.info("Admin用户查找结果: {}", adminUser.isPresent() ? "存在" : "不存在");
            if (adminUser.isPresent()) {
                User admin = adminUser.get();
                log.info("Admin用户详情: username={}, role={}, enabled={}, password={}", 
                    admin.getUsername(), admin.getRole(), admin.isEnabled(), 
                    admin.getPassword() != null ? "已设置" : "未设置");
            }
            
            // 测试查找user用户
            var userUser = userService.findByUsername("user");
            log.info("User用户查找结果: {}", userUser.isPresent() ? "存在" : "不存在");
            if (userUser.isPresent()) {
                User user = userUser.get();
                log.info("User用户详情: username={}, role={}, enabled={}, password={}", 
                    user.getUsername(), user.getRole(), user.isEnabled(), 
                    user.getPassword() != null ? "已设置" : "未设置");
            }
            
            model.addAttribute("adminUser", adminUser.orElse(null));
            model.addAttribute("userUser", userUser.orElse(null));
            
        } catch (Exception e) {
            log.error("测试用户时发生错误: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
        }
        
        return "test-users";
    }
}

















