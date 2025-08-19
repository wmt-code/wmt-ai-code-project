# CLAUDE.md
## 项目概述
这是一个基于 Vue 3 + TypeScript 的前端项目，使用 Vite 构建，主要功能包括用户认证、用户管理和个人中心等。

## 技术栈
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **代码生成**: OpenAPI TypeScript

## 常用命令

### 开发环境
```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 类型检查
pnpm type-check

# 代码格式化
pnpm format

# 代码检查与修复
pnpm lint

# 构建生产版本
pnpm build

# 预览构建结果
pnpm preview

# 根据 OpenAPI 文档生成 TypeScript 类型
pnpm openapi2ts
```

## 项目结构

```
src/
├── api/              # API 接口定义（自动生成 + 手动定义）
├── assets/           # 静态资源
├── components/       # 通用组件
├── layouts/          # 布局组件
├── pages/            # 页面组件
│   ├── admin/        # 管理员相关页面
│   └── user/         # 用户相关页面
├── router/           # 路由配置
├── stores/           # Pinia 状态管理
├── App.vue           # 根组件
├── main.ts           # 应用入口
├── access.ts         # 路由权限控制
└── request.ts        # Axios 请求配置
```

## 核心架构说明

### 1. 认证系统
- **权限控制**: 在 `access.ts` 中通过路由守卫实现
- **用户状态**: 使用 Pinia 的 `loginUser` store 管理
- **登录状态持久化**: 通过 `/user/get/login` 接口获取当前登录用户信息

### 2. API 管理
- **自动生成**: 使用 `@umijs/openapi` 根据后端 Swagger 文档生成 TypeScript 接口
- **配置**: `openapi2ts.config.ts` 中配置生成规则
- **手动调整**: 生成的文件位于 `src/api/` 目录，包含完整的 CRUD 操作

### 3. 路由结构
- **公开路由**: `/` (首页)
- **用户路由**: `/user/login`, `/user/register`, `/user/profile`
- **管理员路由**: `/admin/userManage` (需要 admin 权限)

### 4. 请求处理
- **基础配置**: `request.ts` 中配置 Axios 实例
- **拦截器**: 
  - 请求拦截器：添加认证信息
  - 响应拦截器：统一错误处理，401 未登录跳转
- **API 封装**: 所有 API 调用都通过 `src/api/` 目录下的文件

### 5. 状态管理
- **用户状态**: `stores/loginUser.ts` 管理当前登录用户信息
- **计数器示例**: `stores/counter.ts` 提供基础状态管理示例

## 开发注意事项

### 1. 环境配置
- 后端 API 地址在 `request.ts` 中配置为 `http://localhost:8081/api`
- 需要后端服务正常运行才能完整测试功能

### 2. 权限管理
- 路由守卫会自动检查 `/admin` 前缀的路径需要 admin 权限
- 权限不足会自动跳转到登录页面

### 3. 代码规范
- 使用 ESLint + Prettier 进行代码规范检查
- TypeScript 严格模式已启用
- Vue 组件使用 Composition API

### 4. 构建配置
- Vite 配置支持 `@` 别名指向 `src` 目录
- 生产构建包含类型检查和代码压缩
