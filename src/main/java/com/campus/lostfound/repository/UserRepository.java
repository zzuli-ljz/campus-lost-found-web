package com.campus.lostfound.repository;

import com.campus.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据学号查找用户
     */
    Optional<User> findByStudentId(String studentId);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查学号是否存在
     */
    boolean existsByStudentId(String studentId);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据角色查找用户
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * 根据启用状态查找用户
     */
    List<User> findByEnabled(Boolean enabled);
    
    /**
     * 搜索用户（按用户名、显示名称、学号）
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
    
    /**
     * 统计管理员数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN' AND u.enabled = true")
    long countActiveAdmins();
    
    /**
     * 查找最近注册的用户
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);
}
