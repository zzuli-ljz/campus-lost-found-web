package com.campus.lostfound.config;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemMatch;
import com.campus.lostfound.entity.Location;
import com.campus.lostfound.entity.DetailedLocation;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ItemMatchRepository;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化类
 * 在应用启动时创建默认用户和测试数据
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserService userService;
    private final ItemService itemService;
    private final PasswordEncoder passwordEncoder;
    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查数据初始化...");
        
        // 检查是否已有数据
        long userCount = userService.countUsers();
        long itemCount = itemService.countItems();
        
        if (userCount == 0) {
            log.info("数据库为空，开始初始化数据...");
            
            // 创建默认管理员用户
            createAdminUser();
            
            // 创建测试普通用户
            createTestUsers();
            
            // 创建测试物品数据
            createTestItems();
            
            // 创建测试匹配数据
            createTestMatches();
            
            log.info("数据初始化完成");
        } else {
            log.info("数据库已有数据，跳过初始化。用户数量: {}, 物品数量: {}", userCount, itemCount);
        }
    }
    
    /**
     * 创建管理员用户
     */
    private void createAdminUser() {
        try {
            User admin = userService.register(
                "admin",
                "系统管理员",
                "20240001",
                "admin@campus.edu",
                "13800000001",
                "admin123",
                User.UserRole.ADMIN
            );
            log.info("创建管理员用户成功: {}", admin.getUsername());
        } catch (Exception e) {
            log.info("管理员用户已存在或创建失败: {}", e.getMessage());
        }
    }
    
    /**
     * 创建测试用户
     */
    private void createTestUsers() {
        List<User> testUsers = Arrays.asList(
            createUser("user", "张三", "20240002", "zhangsan@campus.edu", "13800000002", "user123"),
            createUser("lisi", "李四", "20240003", "lisi@campus.edu", "13800000003", "user123"),
            createUser("wangwu", "王五", "20240004", "wangwu@campus.edu", "13800000004", "user123"),
            createUser("zhaoliu", "赵六", "20240005", "zhaoliu@campus.edu", "13800000005", "user123")
        );
        
        for (User user : testUsers) {
            try {
                userService.register(
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getStudentId(),
                    user.getEmail(),
                    user.getPhone(),
                    "user123", // 所有测试用户都使用相同的密码
                    User.UserRole.USER
                );
                log.info("创建测试用户成功: {}", user.getUsername());
            } catch (Exception e) {
                log.info("测试用户 {} 已存在或创建失败: {}", user.getUsername(), e.getMessage());
            }
        }
    }
    
    /**
     * 创建用户对象
     */
    private User createUser(String username, String displayName, String studentId, 
                          String email, String phone, String password) {
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setStudentId(studentId);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.UserRole.USER);
        user.setEnabled(true);
        return user;
    }
    
    /**
     * 创建测试物品数据
     */
    private void createTestItems() {
        log.info("开始创建测试物品数据...");
        
        try {
            // 检查items表是否有必要的列
            long itemCount = itemService.countApprovedItems();
            log.info("数据库中现有已审核物品数量: {}", itemCount);
            
            if (itemCount > 0) {
                log.info("数据库已有物品数据，跳过创建");
                return;
            }
        } catch (Exception e) {
            log.error("检查数据库结构时出错: {}", e.getMessage());
            log.error("请执行 quick_fix.sql 脚本来修复数据库结构");
            return;
        }
        // 获取测试用户
        User user1 = userService.findByUsername("user").orElse(null);
        User user2 = userService.findByUsername("lisi").orElse(null);
        User user3 = userService.findByUsername("wangwu").orElse(null);
        
        if (user1 == null || user2 == null || user3 == null) {
            log.warn("测试用户不存在，跳过创建测试物品");
            return;
        }
        
        List<Item> testItems = Arrays.asList(
            // 失物
            createItem("丢失iPhone 15", "昨天在图书馆三楼自习时丢失了一部iPhone 15，黑色，有白色手机壳。手机里有很多重要资料，请拾到者联系我。", 
                      Item.ItemCategory.MOBILE_PHONE, Item.PostType.LOST, "图书馆三楼", "自习区A区", user1),
            
            createItem("丢失学生证", "在食堂吃饭时丢失了学生证，姓名张三，学号20240002。请拾到者联系我，必有重谢。", 
                      Item.ItemCategory.STUDENT_ID, Item.PostType.LOST, "第一食堂", "二楼靠窗座位", user1),
            
            createItem("丢失黑色背包", "在体育场锻炼时丢失了黑色双肩包，里面有钱包、钥匙等重要物品。背包上有小黄人挂饰。", 
                      Item.ItemCategory.BACKPACK, Item.PostType.LOST, "体育场", "篮球场附近", user2),
            
            // 拾获
            createItem("拾到笔记本电脑", "在教室捡到一台ThinkPad笔记本电脑，黑色，屏幕14寸。请失主联系我并提供购买凭证。", 
                      Item.ItemCategory.LAPTOP, Item.PostType.FOUND, "教学楼A101", "第一排座位", user2),
            
            createItem("拾到钱包", "在校园小路上拾到一个棕色钱包，内有身份证、银行卡等。请失主尽快联系我认领。", 
                      Item.ItemCategory.WALLET, Item.PostType.FOUND, "校园小径", "樱花大道附近", user3),
            
            createItem("拾到课本", "在图书馆拾到一本《高等数学》课本，扉页有姓名。请失主联系我取回。", 
                      Item.ItemCategory.TEXTBOOK, Item.PostType.FOUND, "图书馆", "二楼阅览室", user3),
            
            createItem("拾到校园卡", "在食堂拾到一张校园卡，姓名李四。请失主联系我认领。", 
                      Item.ItemCategory.CAMPUS_CARD, Item.PostType.FOUND, "第二食堂", "一楼收银台附近", user1),
            
            createItem("丢失耳机", "在宿舍楼下丢失了AirPods Pro，白色充电盒。耳机对我来说很重要，请拾到者联系我。", 
                      Item.ItemCategory.HEADPHONES, Item.PostType.LOST, "宿舍楼下", "快递柜附近", user2)
        );
        
        for (Item item : testItems) {
            try {
                // 设置物品为已审核状态
                item.setApproved(true);
                item.setStatus(Item.ItemStatus.PENDING_CLAIM);
                
                Item savedItem = itemService.save(item);
                log.info("创建测试物品成功: {} - {}", savedItem.getPostType().getDescription(), savedItem.getTitle());
            } catch (Exception e) {
                log.error("创建测试物品失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 创建物品对象
     */
    private Item createItem(String title, String description, Item.ItemCategory category, 
                          Item.PostType postType, String locationStr, String detailedLocationStr, User owner) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setPostType(postType);
        
        // 将字符串转换为Location枚举
        Location location = convertToLocation(locationStr);
        item.setLocation(location);
        
        // 将字符串转换为DetailedLocation枚举
        DetailedLocation detailedLocation = convertToDetailedLocation(detailedLocationStr);
        item.setDetailedLocation(detailedLocation);
        
        item.setLostFoundTime(LocalDateTime.now().minusDays((int)(Math.random() * 7))); // 随机1-7天前
        item.setOwner(owner);
        item.setStatus(Item.ItemStatus.PENDING_APPROVAL);
        item.setApproved(false);
        return item;
    }
    
    /**
     * 将字符串转换为Location枚举
     */
    private Location convertToLocation(String locationStr) {
        if (locationStr == null) return Location.OTHER;
        
        // 根据字符串内容匹配对应的Location枚举
        if (locationStr.contains("图书馆")) {
            return Location.LIBRARY;
        } else if (locationStr.contains("教学楼A")) {
            return Location.TEACHING_BUILDING_A;
        } else if (locationStr.contains("教学楼B")) {
            return Location.TEACHING_BUILDING_B;
        } else if (locationStr.contains("第一食堂")) {
            return Location.CANTEEN_FIRST;
        } else if (locationStr.contains("第二食堂")) {
            return Location.CANTEEN_SECOND;
        } else if (locationStr.contains("体育场") || locationStr.contains("运动场")) {
            return Location.SPORTS_FIELD;
        } else if (locationStr.contains("宿舍")) {
            return Location.DORMITORY_NORTH; // 默认北区宿舍
        } else if (locationStr.contains("快递柜")) {
            return Location.EXPRESS_CABINET;
        } else {
            return Location.OTHER;
        }
    }
    
    /**
     * 将字符串转换为DetailedLocation枚举
     */
    private DetailedLocation convertToDetailedLocation(String detailedLocationStr) {
        if (detailedLocationStr == null) return DetailedLocation.OTHER_UNKNOWN;
        
        // 根据字符串内容匹配对应的DetailedLocation枚举
        if (detailedLocationStr.contains("自习区")) {
            return DetailedLocation.LIBRARY_MAIN_READING_ROOM;
        } else if (detailedLocationStr.contains("二楼靠窗")) {
            return DetailedLocation.CANTEEN_FIRST_FLOOR_2;
        } else if (detailedLocationStr.contains("篮球场")) {
            return DetailedLocation.SPORTS_FIELD_BASKETBALL;
        } else if (detailedLocationStr.contains("第一排座位")) {
            return DetailedLocation.TEACHING_BUILDING_A_CLASSROOM_101;
        } else if (detailedLocationStr.contains("樱花大道")) {
            return DetailedLocation.GARDEN_PATH;
        } else if (detailedLocationStr.contains("二楼阅览室")) {
            return DetailedLocation.LIBRARY_MAIN_FLOOR_2;
        } else if (detailedLocationStr.contains("一楼收银台")) {
            return DetailedLocation.CANTEEN_SECOND_FLOOR_1;
        } else if (detailedLocationStr.contains("快递柜附近")) {
            return DetailedLocation.EXPRESS_CABINET_NEARBY;
        } else {
            return DetailedLocation.OTHER_UNKNOWN;
        }
    }
    
    /**
     * 创建测试匹配数据
     */
    private void createTestMatches() {
        log.info("开始创建测试匹配数据...");
        
        try {
            // 获取一些已审核的物品
            List<Item> approvedItems = itemRepository.findByApprovedTrueOrderByCreatedAtDesc();
            if (approvedItems.size() < 2) {
                log.warn("已审核物品数量不足，跳过创建匹配数据");
                return;
            }
            
            // 创建一些匹配记录
            List<Item> lostItems = approvedItems.stream()
                    .filter(item -> item.getPostType() == Item.PostType.LOST)
                    .collect(java.util.stream.Collectors.toList());
            
            List<Item> foundItems = approvedItems.stream()
                    .filter(item -> item.getPostType() == Item.PostType.FOUND)
                    .collect(java.util.stream.Collectors.toList());
            
            int matchCount = 0;
            for (int i = 0; i < Math.min(lostItems.size(), foundItems.size()) && i < 3; i++) {
                Item lostItem = lostItems.get(i);
                Item foundItem = foundItems.get(i);
                
                // 检查是否已经匹配过
                if (!itemMatchRepository.existsMatchBetweenItems(lostItem, foundItem)) {
                    ItemMatch match = new ItemMatch();
                    match.setLostItem(lostItem);
                    match.setFoundItem(foundItem);
                    match.setMatchWeight(0.8 + Math.random() * 0.2); // 0.8-1.0的匹配度
                    match.setMatchedAt(LocalDateTime.now().minusDays((int)(Math.random() * 5)));
                    match.setStatus(ItemMatch.MatchStatus.ACTIVE);
                    
                    itemMatchRepository.save(match);
                    matchCount++;
                    log.info("创建匹配记录: {} <-> {} (匹配度: {})", 
                            lostItem.getTitle(), foundItem.getTitle(), match.getMatchWeight());
                }
            }
            
            // 创建一些已完成的匹配记录
            for (int i = 0; i < Math.min(lostItems.size(), foundItems.size()) && i < 2; i++) {
                if (i + 3 < lostItems.size() && i + 3 < foundItems.size()) {
                    Item lostItem = lostItems.get(i + 3);
                    Item foundItem = foundItems.get(i + 3);
                    
                    if (!itemMatchRepository.existsMatchBetweenItems(lostItem, foundItem)) {
                        ItemMatch match = new ItemMatch();
                        match.setLostItem(lostItem);
                        match.setFoundItem(foundItem);
                        match.setMatchWeight(0.9 + Math.random() * 0.1); // 0.9-1.0的匹配度
                        match.setMatchedAt(LocalDateTime.now().minusDays(10 + (int)(Math.random() * 10)));
                        match.setStatus(ItemMatch.MatchStatus.COMPLETED);
                        match.setCompletedAt(LocalDateTime.now().minusDays(1 + (int)(Math.random() * 5)));
                        
                        itemMatchRepository.save(match);
                        matchCount++;
                        log.info("创建已完成匹配记录: {} <-> {} (匹配度: {})", 
                                lostItem.getTitle(), foundItem.getTitle(), match.getMatchWeight());
                    }
                }
            }
            
            log.info("测试匹配数据创建完成，共创建 {} 条匹配记录", matchCount);
        } catch (Exception e) {
            log.error("创建测试匹配数据失败: {}", e.getMessage(), e);
        }
    }
}
