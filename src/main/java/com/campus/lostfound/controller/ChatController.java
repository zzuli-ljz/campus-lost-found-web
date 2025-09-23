package com.campus.lostfound.controller;

import com.campus.lostfound.entity.ChatMessage;
import com.campus.lostfound.entity.ChatThread;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat")
    public String chat(@AuthenticationPrincipal User user, Model model,
                       @RequestParam(required = false) Long open,
                       @RequestParam(required = false) String error) {
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("user", user);
        model.addAttribute("threads", chatService.myThreads(user));
        if (open != null) {
            // 标记该线程为已读
            chatService.markThreadAsRead(open, user);
            model.addAttribute("openId", open);
            model.addAttribute("messages", chatService.listMessages(open));
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "chat";
    }

    @GetMapping("/chat/{itemId}")
    public String chatWithItem(@PathVariable Long itemId, @AuthenticationPrincipal User user, Model model) {
        if (user == null) return "redirect:/auth/login";
        
        try {
            // 获取物品信息
            com.campus.lostfound.entity.Item item = chatService.getItemById(itemId);
            if (item == null) {
                return "redirect:/chat?error=item_not_found";
            }
            
            // 检查用户是否有权限与该物品的发布者聊天
            if (item.getOwner().getId().equals(user.getId())) {
                return "redirect:/chat?error=cannot_chat_with_self";
            }
            
            // 查找或创建聊天线程
            ChatThread thread = chatService.findOrCreateThread(itemId, user, item.getOwner());
            
            // 标记该线程为已读
            chatService.markThreadAsRead(thread.getId(), user);
            
            // 获取用户的所有聊天线程
            java.util.List<ChatThread> threads = chatService.myThreads(user);
            
            model.addAttribute("user", user);
            model.addAttribute("threads", threads);
            model.addAttribute("openId", thread.getId());
            model.addAttribute("messages", chatService.listMessages(thread.getId()));
            model.addAttribute("currentItem", item);
            
            return "chat";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/chat?error=system_error";
        }
    }

    @PostMapping("/chat/start")
    public String start(@RequestParam Long itemId,
                        @RequestParam Long otherUserId,
                        @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        ChatThread t = chatService.startOrGetThread(itemId, user, otherUserId);
        return "redirect:/chat?open=" + t.getId();
    }

    @PostMapping("/chat/{id}/send")
    public String send(@PathVariable Long id,
                       @RequestParam String content,
                       @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        
        // 检查聊天线程是否存在
        ChatThread thread = chatService.getThreadById(id);
        if (thread == null) {
            return "redirect:/chat?error=thread_not_found";
        }
        
        // 检查物品是否已完成
        Item item = thread.getItem();
        if (item != null && item.getStatus() == Item.ItemStatus.COMPLETED) {
            return "redirect:/chat?open=" + id + "&error=item_completed";
        }
        
        ChatMessage m = chatService.sendText(id, user, content);
        return "redirect:/chat?open=" + id + "#m" + m.getId();
    }

    @PostMapping("/chat/{id}/mark-read")
    public String markAsRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/auth/login";
        chatService.markThreadAsRead(id, user);
        return "redirect:/chat?open=" + id;
    }
}






