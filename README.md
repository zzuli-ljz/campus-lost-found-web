# 校园失物招领系统 - 网页版

基于Spring Boot + Thymeleaf + Bootstrap构建的现代化校园失物招领系统，提供美观的用户界面和丰富的动态效果。

## 🚀 功能特色

### 核心功能
- **用户管理**：用户注册、登录、个人资料管理
- **物品发布**：发布失物/拾获信息，支持图片上传
- **智能搜索**：关键词搜索、分类筛选、高级搜索
- **匹配系统**：AI智能匹配相似物品
- **认领流程**：完整的认领申请和审核流程
- **实时通知**：系统通知、匹配提醒、审核结果通知
- **聊天系统**：失主和拾获者实时沟通
- **管理后台**：管理员审核、统计分析、用户管理

### 技术特色
- **响应式设计**：完美适配桌面端和移动端
- **动态效果**：丰富的CSS动画和JavaScript交互
- **现代化UI**：基于Bootstrap 5的现代化界面设计
- **安全可靠**：Spring Security安全框架保护
- **数据持久化**：JPA + H2数据库

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 3.2.0** - 主框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问层
- **Thymeleaf** - 模板引擎
- **H2 Database** - 内存数据库
- **Maven** - 项目管理工具

### 前端技术
- **Bootstrap 5.3.0** - UI框架
- **Bootstrap Icons** - 图标库
- **Animate.css** - CSS动画库
- **Google Fonts** - 字体库
- **JavaScript ES6+** - 交互逻辑

## 📦 项目结构

```
campus-lost-found-web/
├── src/
│   ├── main/
│   │   ├── java/com/campus/lostfound/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   └── CampusLostFoundApplication.java
│   │   └── resources/
│   │       ├── static/          # 静态资源
│   │       │   ├── css/         # 样式文件
│   │       │   ├── js/          # JavaScript文件
│   │       │   └── images/      # 图片资源
│   │       ├── templates/       # Thymeleaf模板
│   │       └── application.yml  # 配置文件
│   └── test/                    # 测试代码
├── pom.xml                      # Maven配置
└── README.md                    # 项目说明
```

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- 现代浏览器（Chrome、Firefox、Safari、Edge）

### 安装运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd campus-lost-found-web
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

4. **访问应用**
   打开浏览器访问：http://localhost:8080/campus-lost-found

### 默认账户

#### 管理员账户
- 用户名：`admin`
- 密码：`admin123`

#### 普通用户账户
- 用户名：`user`
- 密码：`user123`

## 🎨 界面预览

### 主要页面
- **首页**：展示系统统计信息和最新发布
- **登录页**：美观的登录界面，支持快速体验
- **搜索页**：强大的搜索功能，支持多种筛选条件
- **发布页**：便捷的物品发布表单
- **个人中心**：用户个人信息和物品管理
- **管理后台**：管理员专用功能页面

### 设计特色
- **渐变背景**：现代化的渐变色彩搭配
- **卡片设计**：清晰的信息层次结构
- **动画效果**：流畅的页面过渡和交互反馈
- **响应式布局**：完美适配各种屏幕尺寸
- **图标系统**：统一的Bootstrap Icons图标库

## 🔧 配置说明

### 数据库配置
系统默认使用H2内存数据库，配置在`application.yml`中：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:lostfounddb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 文件上传配置
```yaml
app:
  upload:
    path: uploads/
    max-size: 10485760  # 10MB
```

### 安全配置
- 登录页面：`/login`
- 退出登录：`/logout`
- 静态资源：无需认证
- 管理页面：需要ADMIN角色

## 📱 响应式设计

系统采用Bootstrap 5的响应式网格系统，支持以下断点：
- **xs**: < 576px (手机)
- **sm**: ≥ 576px (大手机)
- **md**: ≥ 768px (平板)
- **lg**: ≥ 992px (桌面)
- **xl**: ≥ 1200px (大桌面)
- **xxl**: ≥ 1400px (超大桌面)

## 🎭 动画效果

### CSS动画
- **淡入动画**：页面元素渐显效果
- **滑动动画**：元素从不同方向滑入
- **缩放动画**：按钮点击反馈效果
- **浮动动画**：装饰元素浮动效果
- **脉冲动画**：重要信息提醒效果

### JavaScript交互
- **表单验证**：实时验证用户输入
- **图片预览**：点击图片放大查看
- **返回顶部**：平滑滚动到页面顶部
- **加载动画**：页面加载和提交反馈
- **通知系统**：操作成功/失败提示

## 🔐 安全特性

- **密码加密**：BCrypt密码哈希加密
- **会话管理**：安全的用户会话控制
- **CSRF保护**：跨站请求伪造防护
- **输入验证**：服务端数据验证
- **权限控制**：基于角色的访问控制

## 📊 数据模型

### 主要实体
- **User**：用户信息
- **Item**：物品信息
- **ItemImage**：物品图片
- **Claim**：认领申请
- **Notification**：通知消息
- **ChatThread**：聊天线程
- **ChatMessage**：聊天消息

### 关系设计
- 用户与物品：一对多关系
- 物品与图片：一对多关系
- 物品与认领：一对多关系
- 用户与通知：一对多关系

## 🚀 部署说明

### 开发环境
```bash
mvn spring-boot:run
```

### 生产环境
1. **打包应用**
   ```bash
   mvn clean package
   ```

2. **运行JAR包**
   ```bash
   java -jar target/campus-lost-found-web-1.0.0.jar
   ```

3. **配置生产数据库**
   修改`application.yml`中的数据库配置

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- 邮箱：contact@campus.edu
- 电话：400-123-4567
- 网站：https://campus.edu

## 🙏 致谢

感谢以下开源项目的支持：
- Spring Boot
- Bootstrap
- Thymeleaf
- H2 Database
- Bootstrap Icons
- Animate.css

---

**校园失物招领系统** - 让失物招领变得更加简单高效！ 🎓✨
