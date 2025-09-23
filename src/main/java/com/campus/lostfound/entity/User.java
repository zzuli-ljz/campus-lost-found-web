package com.campus.lostfound.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户实体类
 * 实现UserDetails接口以支持Spring Security
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"items", "notifications", "claims", "chatThreadsA", "chatThreadsB"})
@ToString(callSuper = true, exclude = {"items", "notifications", "claims", "chatThreadsA", "chatThreadsB"})
public class User extends BaseEntity implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "显示名称不能为空")
    @Size(max = 50, message = "显示名称不能超过50个字符")
    @Column(nullable = false)
    private String displayName;
    
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号不能超过20个字符")
    @Column(unique = true, nullable = false)
    private String studentId;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;
    
    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false)
    private Boolean accountNonExpired = true;
    
    @Column(nullable = false)
    private Boolean accountNonLocked = true;
    
    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;
    
    // 关联关系
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();
    
    @OneToMany(mappedBy = "claimant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims = new ArrayList<>();
    
    @OneToMany(mappedBy = "userA", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatThread> chatThreadsA = new ArrayList<>();
    
    @OneToMany(mappedBy = "userB", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatThread> chatThreadsB = new ArrayList<>();
    
    // UserDetails接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // 用户角色枚举
    public enum UserRole {
        USER("普通用户"),
        ADMIN("管理员");
        
        private final String description;
        
        UserRole(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
