# Markdown Editor - 在线Markdown编辑器

一个功能完善的在线Markdown编辑器，类似Typora，支持所见即所得的编辑体验。

## 功能特性

### 核心功能
- ✅ **实时预览**：所见即所得的Markdown编辑体验
- ✅ **用户认证**：注册、登录、JWT认证
- ✅ **文档管理**：创建、编辑、删除、搜索文档
- ✅ **分类标签**：支持文档分类和标签管理
- ✅ **公开分享**：支持将文档设置为公开，分享给他人
- ✅ **文件上传**：支持图片和文件上传
- ✅ **用户关注**：用户之间可以互相关注
- ✅ **消息中心**：支持评论、点赞、收藏、关注的消息提醒
- ✅ **用户聊天**：用户之间可以发送私信，未关注用户最多只能发送两条消息

### 编辑器功能
- ✅ **格式化工具**：加粗、斜体、删除线、代码等
- ✅ **标题管理**：支持1-4级标题
- ✅ **列表支持**：有序列表、无序列表
- ✅ **代码高亮**：支持多种编程语言语法高亮
- ✅ **表格支持**：支持GFM表格
- ✅ **数学公式**：支持KaTeX数学公式

### 导出功能
- ✅ **HTML导出**：导出为格式化的HTML文件
- ✅ **Markdown导出**：导出原始Markdown文件
- ✅ **PDF导出**：导出为PDF文件（需要后端支持）

### 部署支持
- ✅ **Docker部署**：完整的Docker和Docker Compose配置
- ✅ **一键部署**：支持一键启动所有服务

## 技术栈

### 后端
- Spring Boot 2.7.14
- Spring Security + JWT
- MyBatis Plus (数据访问层)
- MySQL 8.0
- CommonMark (Markdown解析)

### 前端
- Vue 3 + Vite
- Element Plus
- Pinia (状态管理)
- Vue Router
- Marked (Markdown渲染)
- Highlight.js (代码高亮)
- KaTeX (数学公式)

## 项目结构

```
markdown-editor/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/markdown/editor/
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       ├── mapper/        # MyBatis Plus 数据访问层
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── service/impl/  # 服务层实现
│   │   │   │       ├── security/      # 安全配置
│   │   │   │       ├── dto/           # 数据传输对象
│   │   │   │       ├── filter/        # 过滤器
│   │   │   │       ├── config/        # 配置类
│   │   │   │       └── exception/     # 异常处理
│   │   │   └── resources/
│   │   │       ├── application.yml    # 配置文件
│   │   │       └── mapper/            # MyBatis Plus XML 映射文件
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── stores/         # Pinia状态管理
│   │   ├── router/         # 路由配置
│   │   ├── utils/          # 工具函数
│   │   ├── styles/         # 样式文件
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── database/                # 数据库相关文件
│   └── sql/                # SQL 脚本
├── docker-compose.yml       # Docker Compose配置
└── README.md               # 项目说明文档
```

## 快速开始

### 前置要求
- Docker
- Docker Compose
- Node.js 18+ (本地开发)
- JDK 8+ (本地开发)
- MySQL 8.0 (本地开发)

### Docker部署（推荐）

1. 克隆项目
```bash
git clone https://github.com/yourusername/markdown-editor.git
cd markdown-editor
```

2. 构建并启动服务
```bash
docker-compose up -d --build
```

3. 访问应用
- 前端：http://localhost
- 后端API：http://localhost:8080

### 本地开发

#### 后端启动

1. 进入后端目录
```bash
cd backend
```

2. 创建数据库
```sql
CREATE DATABASE markdown_editor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改配置文件 `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/markdown_editor?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

4. 构建并运行
```bash
mvn clean package
java -jar target/editor-backend-1.0.0.jar
```

#### 前端启动

1. 进入前端目录
```bash
cd frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

4. 访问 http://localhost:3000

## API文档

### 认证接口

#### 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "test",
  "password": "123456",
  "email": "test@example.com",
  "nickname": "Test User"
}
```

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

### 文档接口

#### 获取文档列表
```
GET /api/documents?page=0&size=10
Authorization: Bearer {token}
```

#### 创建文档
```
POST /api/documents
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "我的文档",
  "content": "# Hello World",
  "category": "技术",
  "tags": "Vue,Markdown",
  "isPublic": false
}
```

#### 更新文档
```
PUT /api/documents/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "更新后的文档",
  "content": "# Updated Content",
  "category": "技术",
  "tags": "Vue,Markdown",
  "isPublic": true
}
```

#### 删除文档
```
DELETE /api/documents/{id}
Authorization: Bearer {token}
```

#### 搜索文档
```
GET /api/documents/search?keyword=关键词
Authorization: Bearer {token}
```

### 公开文档接口

#### 获取公开文档列表
```
GET /api/documents/public?page=0&size=10
```

#### 获取公开文档详情
```
GET /api/documents/public/{id}
```

### 文件上传接口

#### 上传单个文件
```
POST /api/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [文件]
```

#### 上传多个文件
```
POST /api/upload/multiple
Authorization: Bearer {token}
Content-Type: multipart/form-data

files: [文件1, 文件2, ...]
```

### 用户关注接口

#### 关注用户
```
POST /api/users/{id}/follow
Authorization: Bearer {token}
```

#### 取消关注用户
```
DELETE /api/users/{id}/follow
Authorization: Bearer {token}
```

#### 检查关注状态
```
GET /api/users/{id}/follow/status
Authorization: Bearer {token}
```

### 消息接口

#### 获取消息列表
```
GET /api/messages
Authorization: Bearer {token}
```

#### 根据消息类型获取消息
```
GET /api/messages/type/{type}
Authorization: Bearer {token}
```

#### 获取未读消息数量
```
GET /api/messages/unread/count
Authorization: Bearer {token}
```

#### 标记消息为已读
```
PUT /api/messages/read
Authorization: Bearer {token}
```

#### 删除消息
```
DELETE /api/messages/{id}
Authorization: Bearer {token}
```

### 聊天接口

#### 获取聊天用户列表
```
GET /api/chat/messages/list
Authorization: Bearer {token}
```

#### 获取聊天记录
```
GET /api/chat/messages?receiverId={receiverId}
Authorization: Bearer {token}
```

#### 发送聊天消息
```
POST /api/chat
Authorization: Bearer {token}
Content-Type: application/json

{
  "receiverId": 1,
  "content": "你好！"
}
```

## 配置说明

### 后端配置

在 `application.yml` 中配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/markdown_editor
    username: root
    password: root
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

jwt:
  secret: your-secret-key
  expiration: 86400000

file:
  upload-dir: ./uploads
```

### 前端配置

在 `vite.config.js` 中配置API代理：

```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 使用说明

### 创建文档
1. 登录后点击"新建文档"按钮
2. 输入文档标题
3. 在编辑器中编写Markdown内容
4. 使用工具栏快速插入格式
5. 选择分类和标签
6. 设置是否公开
7. 点击"保存"按钮

### 编辑文档
1. 在文档列表中点击文档标题
2. 进入编辑器修改内容
3. 系统会自动保存（2秒延迟）
4. 或手动点击"保存"按钮

### 导出文档
1. 在编辑器中点击"导出"按钮
2. 选择导出格式（HTML/PDF/Markdown）
3. 文件会自动下载到本地

### 分享文档
1. 在编辑器中将文档设置为"公开"
2. 复制文档链接分享给他人
3. 其他人可以在"公开文档"页面查看

### 用户关注
1. 在用户主页或文章页面点击"关注"按钮
2. 关注后，你将收到该用户的新文章通知
3. 可以在个人主页查看关注列表和粉丝列表

### 消息中心
1. 点击顶部导航栏的"消息中心"链接
2. 在消息中心查看评论、点赞、收藏、关注的消息
3. 点击相应标签查看对应类型的消息
4. 未读消息会在标签上显示小红点

### 发送私信
1. 在用户主页或文章页面点击"私信"按钮
2. 进入聊天界面，输入消息内容
3. 点击"发送"按钮发送消息
4. 未关注用户最多只能发送两条消息

## 常见问题

### 1. Docker启动失败
- 检查端口是否被占用（80, 8080, 3306）
- 检查Docker是否正常运行
- 查看容器日志：`docker-compose logs`

### 2. 数据库连接失败
- 检查MySQL是否正常运行
- 检查数据库配置是否正确
- 确认数据库已创建

### 3. 文件上传失败
- 检查上传目录权限
- 检查文件大小是否超过限制
- 查看后端日志

## 开发计划

- [ ] 支持更多Markdown语法
- [ ] 添加文档版本控制
- [ ] 支持协作编辑
- [ ] 添加文档评论功能
- [ ] 支持更多导出格式
- [ ] 添加文档模板
- [ ] 支持文档目录生成
- [ ] 添加快捷键支持

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目地址：https://github.com/yourusername/markdown-editor
- 问题反馈：https://github.com/yourusername/markdown-editor/issues

## 致谢

- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Marked](https://marked.js.org/)
- [Highlight.js](https://highlightjs.org/)
- [KaTeX](https://katex.org/)
