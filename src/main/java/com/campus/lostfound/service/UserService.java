package com.campus.lostfound.service;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 实现UserDetailsService接口
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("尝试加载用户: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        log.debug("成功加载用户: {}, 角色: {}, 启用状态: {}", username, user.getRole(), user.isEnabled());
        return user;
    }
    
    /**
     * 注册新用户
     */
    @Transactional
    public User register(String username, String displayName, String studentId, 
                        String email, String phone, String password, User.UserRole role) {
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查学号是否已存在
        if (userRepository.existsByStudentId(studentId)) {
            throw new RuntimeException("学号已存在");
        }
        
        // 检查邮箱是否已存在
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setStudentId(studentId);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        log.info("新用户注册成功: {}", username);
        
        return savedUser;
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据学号查找用户
     */
    public Optional<User> findByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }
    
    /**
     * 搜索用户
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }
    
    /**
     * 获取所有用户
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long userId, String displayName, String email, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否被其他用户使用
        if (email != null && !email.isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }
        
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setPhone(phone);
        
        User savedUser = userRepository.save(user);
        log.info("用户信息更新成功: {}", user.getUsername());
        
        return savedUser;
    }
    
    /**
     * 更新用户信息（接受User对象）
     */
    @Transactional
    public User updateUser(User user) {
        // 检查邮箱是否被其他用户使用
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }
        
        User savedUser = userRepository.save(user);
        log.info("用户信息更新成功: {}", user.getUsername());
        
        return savedUser;
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码修改成功: {}", user.getUsername());
    }
    
    /**
     * 启用/禁用用户
     */
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        
        log.info("用户状态变更: {} -> {}", user.getUsername(), user.getEnabled() ? "启用" : "禁用");
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否为 admin 用户
        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("不能删除admin用户");
        }

        userRepository.delete(user);
        log.info("用户删除成功: {}", user.getUsername());
    }
    
    /**
     * 统计用户数量
     */
    public long countUsers() {
        return userRepository.count();
    }
    
    /**
     * 统计活跃用户数量
     */
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
    
    /**
     * 统计管理员数量
     */
    public long countActiveAdmins() {
        return userRepository.countActiveAdmins();
    }
    
    /**
     * 获取最近注册的用户
     */
    public List<User> getRecentUsers(Pageable pageable) {
        return userRepository.findRecentUsers(pageable);
    }
}