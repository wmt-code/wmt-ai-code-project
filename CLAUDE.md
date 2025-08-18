# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 提供在本代码仓库中工作的指导。

## 项目概览

这是一个基于 Spring Boot 3.5.4 的 AI 代码生成服务应用，使用 LangChain4j 集成 DeepSeek 大模型，支持从自然语言提示词生成
HTML/CSS/JS 代码并保存到本地文件系统。

## 系统架构

### 核心组件

- **AI 代码生成层**: 使用 LangChain4j 集成 DeepSeek 实现流式代码生成
- **代码处理管道**: 使用模版方法模式和策略模式解析和保存生成的代码
- **文件管理**: 将生成的代码保存到本地文件系统的 `tmp/code_output/` 目录
- **用户管理**: 完整的用户注册/登录系统，支持基于角色的访问控制
- **REST API**: Spring MVC 控制器，提供统一响应格式

### 设计模式

- **模版方法模式**: `CodeFileSaveTemplate` 提供不同文件保存策略
- **策略模式**: `CodeParserExecutor` 实现不同代码解析策略
- **执行器模式**: 集中式代码解析和保存操作执行
- **工厂模式**: `AiCodeGeneratorFactory` 创建 AI 服务

### 技术栈

- **框架**: Spring Boot 3.5.4 + Java 21
- **数据库**: MySQL + MyBatis-Flex ORM
- **缓存**: Redis 会话管理
- **AI 集成**: LangChain4j + DeepSeek 模型
- **文档**: Knife4j/Swagger-UI API 文档
- **文件存储**: 腾讯云 COS (对象存储)
- **构建工具**: Maven (包含 mvnw 包装器)

## 开发命令

### 构建与运行

```bash
# 构建项目
./mvnw clean compile

# 运行测试
./mvnw test

# 本地运行应用
./mvnw spring-boot:run

# 构建可执行 JAR
./mvnw clean package
```

### 数据库配置

```bash
# 数据库配置在 application.yml 中
# 默认配置: jdbc:mysql://localhost:3306/ai_code
# 用户名: root
# 密码: qwe@321

# 创建数据库
mysql -u root -p -e "CREATE DATABASE ai_code CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Redis 配置

```bash
# Redis 配置
# 主机: localhost:6379
# 密码: 123456
# 数据库: 0
```

### 测试

```bash
# 运行所有测试
./mvnw test

# 运行指定测试类
./mvnw test -Dtest=AiCodeGeneratorFacadeTest

# 运行单个测试方法
./mvnw test -Dtest=AiCodeGeneratorFacadeTest#generateAndSaveCode
```

## API 接口

### 认证接口

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/get/login` - 获取当前用户
- `GET /api/user/logout` - 用户登出

### 用户管理 (仅管理员)

- `POST /api/user/add` - 添加新用户
- `GET /api/user/get` - 根据ID获取用户
- `POST /api/user/update` - 更新用户
- `POST /api/user/delete` - 删除用户
- `POST /api/user/list/page/vo` - 分页查询用户列表

### 文件操作

- `POST /api/user/uploadAvatar` - 上传头像到 COS

## 代码生成流程

1. **输入**: 通过 API 接收自然语言提示词
2. **AI 处理**: DeepSeek 模型通过 LangChain4j 生成代码
3. **代码解析**: 从 markdown 代码块中提取 HTML/CSS/JS
4. **文件保存**: 保存到 `tmp/code_output/` 下的时间戳目录
5. **响应**: 返回文件路径或流式内容

### 支持的生成类型

- **HTML**: 单文件 HTML 生成
- **MULTI_FILE**: 分离的 HTML、CSS、JS 文件

## 配置文件

- `application.yml` - 主配置文件 (使用 `local` 配置)
- `application-local.yml` - 本地开发覆盖配置
- `prompt/` - AI 代码生成的系统提示词
- `mapper/` - MyBatis XML 映射文件

## 重要说明

- **文件存储**: 生成的文件保存在 `tmp/code_output/[type]_[timestamp]/`
- **COS 集成**: 头像上传使用腾讯云 COS (凭证在 application.yml 中)
- **会话管理**: 使用 Redis 存储会话，超时时间 30 天
- **安全控制**: 通过 `@AuthCheck` 注解实现基于角色的访问控制
- **API 文档**: 访问地址 `http://localhost:8081/api/swagger-ui.html`

## 常见开发任务

### 添加新的代码生成类型

1. 在 `CodeGenTypeEnum` 中添加枚举值
2. 创建新的结果结构模型类
3. 在 `CodeParser` 中添加解析逻辑
4. 在 `CodeFileSaveTemplate` 中添加保存模板
5. 更新 `AiCodeGeneratorFacade` 处理新类型

### 数据库变更

1. 更新 `model/entity/` 中的实体类
2. 更新 `resources/mapper/` 中的 mapper XML 文件
3. 运行测试验证变更

### AI 提示词定制

1. 在 `resources/prompt/` 中添加新的提示词文件
2. 在 `AiCodeGeneratorService` 中使用 `@SystemMessage` 引用
3. 更新服务方法处理新的提示词类型