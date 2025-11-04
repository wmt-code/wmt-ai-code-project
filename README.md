# WMT AI Code Backend

一个基于 Spring Boot 3.5.4 + Java 21 的 AI 代码生成与工作流服务，集成 DeepSeek、LangChain4j、LangGraph4j、Redis、MySQL、COS 等组件，为前端代码生成、项目打包和多模态工具调用提供后端支撑。

## 目录
- [项目概述](#项目概述)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [目录结构](#目录结构)
- [环境依赖](#环境依赖)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [数据库与缓存](#数据库与缓存)
- [运行与测试](#运行与测试)
- [API 速览](#api-速览)
- [AI 工作流说明](#ai-工作流说明)
- [文件与资源说明](#文件与资源说明)
- [常见问题](#常见问题)
- [许可协议](#许可协议)

## 项目概述
WMT AI Code Backend 提供了一套完整的 “AI 生成 + 模板解析 + 文件落地 + 项目打包 + 质量校验 + 静态资源服务” 后端解决方案。系统支持：
- 按提示词生成多文件前端项目（HTML/CSS/JS/Vue）。
- 通过工作流协调提示强化、代码生成、图片素材抓取、质量检测等环节。
- 将生成产物保存到本地/对象存储并提供下载、截图。
- 提供用户、应用、会话管理及速率控制能力。

## 核心功能
- **AI 代码生成**：LangChain4j + DeepSeek Chat / DeepSeek Reasoner，支持流式响应与多轮上下文。
- **LangGraph4j 工作流**：Prompt 增强 → 代码生成 → 代码质量校验 → 项目构建 → 结果汇总，可扩展节点。
- **文件管道**：模板方法 + 策略模式解析 Markdown 代码块，输出到 `tmp/code_output/` 等目录。
- **项目打包与下载**：支持 Vue 项目构建、批量打包与 COS/本地存储下载。
- **截图服务**：Selenium + WebDriverManager，对生成页面进行截图保存到 `tmp/screenshots/`。
- **用户与会话管理**：注册、登录、角色鉴权、Redis Session、自定义 `@AuthCheck` 注解。
- **应用与对话历史**：应用 CRUD、对话记录查询、速率限制、静态资源托管。
- **工具生态**：文件读写/修改工具、图片素材（Pexels）、Logo 生成、Mermaid 图自动化。

## 技术栈
- **语言 / 框架**：Java 21、Spring Boot 3.5.4、Spring WebFlux (SSE)、Spring Session。
- **AI & 工作流**：LangChain4j、LangGraph4j、DeepSeek、阿里 DashScope。
- **数据访问**：MyBatis-Flex、MySQL、Redis、Redisson、本地 Caffeine 缓存。
- **对象存储**：腾讯云 COS。
- **自动化工具**：Selenium、WebDriverManager。
- **文档与运维**：Knife4j/Swagger、Lombok、HikariCP、Maven Wrapper。

## 目录结构
```text
.
├── pom.xml
├── src
│   ├── main
│   │   ├── java/com/wmt/wmtaicode
│   │   │   ├── controller        # App、User、ChatHistory、静态资源等接口
│   │   │   ├── ai                # LangChain4j 集成、模型守护、工具
│   │   │   ├── core              # 代码解析/保存/构建核心管道
│   │   │   ├── langgraph4j       # 工作流节点定义及状态管理
│   │   │   ├── service           # 业务服务接口及实现
│   │   │   ├── config            # Redis、AI 模型、CORS、COS 等配置
│   │   │   ├── common/utils/...  # 通用响应、异常、常量
│   │   │   └── model             # DTO/VO/实体与枚举
│   │   └── resources
│   │       ├── application*.yml  # 多环境配置
│   │       ├── mapper            # MyBatis-Flex XML
│   │       ├── prompt            # AI 系统提示词
│   │       ├── static/templates  # 静态资源 & 模板
│   │       └── chromedriver-*    # 预置的 ChromeDriver
│   └── test/java/com/wmt/wmtaicode
│       └── ...                   # 工作流、AI、并发等单元测试
├── tmp
│   ├── code_output               # 生成代码输出
│   ├── code_deploy               # 部署/打包成果
│   ├── logos                     # Logo 生成产物
│   └── screenshots               # 页面截图
└── mvnw / mvnw.cmd               # Maven Wrapper
```

## 环境依赖
- JDK 21+
- Maven 3.9+（已提供 `./mvnw`)
- MySQL 8+
- Redis 6+
- Chrome 版本与 `resources/chromedriver-*` 匹配或自备驱动
- 腾讯云 COS 凭证（用于头像 & 项目存储，可按需替换）
- DeepSeek API Key（可同时配置推理模型、路由模型）
- Pexels 与 DashScope API Key（如需图片与多模态能力）

## 快速开始
1. 克隆或下载项目代码。
2. 复制 `src/main/resources/application-local.yml` 为自定义配置文件（如 `application-dev.yml`），并调整数据库、Redis、COS、AI Key 等值。
3. 在 `application.yml` 中将 `spring.profiles.active` 切换为对应环境（例如 `local`）。
4. 确保 MySQL、Redis 服务已启动且账户拥有库表权限。
5. 首次运行推荐执行依赖下载与编译：
   ```bash
   ./mvnw clean compile
   ```
6. 启动应用：
   ```bash
   ./mvnw spring-boot:run
   ```
   服务默认监听 `http://localhost:8081/api`。
7. 访问 `http://localhost:8081/api/swagger-ui.html` 查看在线 API 文档。

## 配置说明
`src/main/resources/application.yml` 中的关键占位符如下：

| 配置项 | 示例占位符 | 说明 |
| ------ | ---------- | ---- |
| MySQL  | `[DB_HOST]:[DB_PORT]`, `[DB_USERNAME]`, `[DB_PASSWORD]` | 默认库名 `ai_code` |
| Redis  | `[REDIS_HOST]`, `[REDIS_PORT]`, `[REDIS_PASSWORD]` | Session 与缓存 |
| COS    | `[COS_HOST]`, `[COS_SECRET_ID]`, `[COS_SECRET_KEY]` | 用于文件上传，可替换为本地存储策略 |
| LangChain4j | `{apiKey}` | DeepSeek Chat / Reasoner / Routing 模型各自的 Key |
| Pexels | `{apiKey}` | 图片检索工具 |
| DashScope | `{apiKey}` | 生成式图片等能力 |

> 建议将敏感密钥通过环境变量或外部配置中心注入，避免提交至版本库。

## 数据库与缓存
- 建议手动创建数据库：
  ```sql
  CREATE DATABASE ai_code CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
- Mapper 位于 `resources/mapper/`，MyBatis-Flex 会自动加载。
- Redis 用于 Session & 缓存，需要与 `spring.session.redis.namespace` 保持一致。
- 可按需调整 `spring.datasource.hikari.*` 连接池参数与 `spring.data.redis.ttl`。

## 运行与测试
```bash
# 编译
./mvnw clean compile

# 运行全部测试
./mvnw test

# 启动应用（开发模式）
./mvnw spring-boot:run

# 构建可执行 JAR
./mvnw clean package

# 指定测试类/方法
./mvnw test -Dtest=AiCodeGeneratorFacadeTest
./mvnw test -Dtest=AiCodeGeneratorFacadeTest#generateAndSaveCode
```

## API 速览
服务根路径为 `/api`，核心接口包括：
- `/user/**`：注册、登录、获取当前用户、管理员增删改查、头像上传等。
- `/app/**`：应用创建、更新、删除、分页查询、打包下载、流式对话等。
- `/chatHistory/**`：对话记录新增、查询。
- `/project/**`：项目打包、下载、生成快照。
- `/static/**`：生成产物的静态资源访问。
- `/health/**`：心跳健康检查。

详细参数请参考 `Swagger UI` 或 `CLAUDE.md` 中的补充说明。

## AI 工作流说明
- `core` 包含代码解析、保存的模板与执行器（`CodeParserExecutor`、`CodeFileSaveExecutor`）。
- `langgraph4j` 定义了完整的工作流节点（Prompt 增强、代码生成、质量检测、项目构建、路由分发等）与状态管理。
- `ai/model` 中定义了多文件输出结构与流式消息类型。
- `ai/guardrail` 实现输入/输出守护策略，确保提示词安全与模型重试。
- 流式响应通过 `ProjectDownloadService`、`ServerSentEvent` 形式发送。

## 文件与资源说明
- 生成代码：`tmp/code_output/<timestamp>/...`
- 部署包：`tmp/code_deploy/`
- 截图：`tmp/screenshots/`
- Logo 与素材：`tmp/logos/`
- 提示词模板：`src/main/resources/prompt/`
- Selenium Driver：`src/main/resources/chromedriver-*`（根据运行平台选择或替换）

## 常见问题
1. **ChromeDriver 兼容性**：确保本地 Chrome 版本与 `chromedriver` 匹配；在服务器环境可通过 WebDriverManager 自动下载。
2. **AI Key 泄露**：请勿将 `{apiKey}` 字面值提交到仓库，使用环境变量或密钥管理服务。
3. **Redis 未配置**：默认会话存储在 Redis，若只本地测试可切换至 `spring.session.store-type=none`。
4. **COS 可替换**：如无 COS，可在 `FileServiceImpl` 中实现本地存储策略，或配置其他对象存储。
5. **工作流扩展**：新增节点需在 `langgraph4j` 的 `WorkflowApp` 中注册，并补充测试。

## 许可协议
项目当前未在仓库中声明开源协议。如需对外发布，请补充许可证信息。

---

> 本 README 由 AI 根据项目源码自动整理，若有遗漏或配置变更，请以实际代码为准并及时更新文档。
