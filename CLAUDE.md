# CLAUDE.md
## 语言规范
- 所有对话和文档都使用中文
- 文档使用 markdown 格式
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview/

This is a Vue 3 + TypeScript + Vite frontend application using Ant Design Vue for UI components and Pinia for state management. The project is set up as a single-page application (SPA) with basic routing and store capabilities.

## Development Commands

### Core Commands
- `pnpm dev` - Start development server with hot reload
- `pnpm build` - Type-check and build for production
- `pnpm preview` - Preview production build locally
- `pnpm lint` - Run ESLint with auto-fix
- `pnpm format` - Format code with Prettier
- `pnpm type-check` - Run TypeScript type checking only

### Build-only Commands
- `pnpm build-only` - Build without type checking
- `pnpm run-p type-check "build-only {@}" --` - Parallel build with type checking

## Project Architecture

### Tech Stack
- **Framework**: Vue 3 with Composition API
- **Language**: TypeScript
- **Build Tool**: Vite
- **UI Library**: Ant Design Vue (v4.x)
- **State Management**: Pinia
- **Routing**: Vue Router v4
- **Code Quality**: ESLint + Prettier + TypeScript

### Directory Structure
```
src/
├── App.vue                 # Root component
├── main.ts                 # Application entry point
├── router/                 # Vue Router configuration
│   └── index.ts           # Router setup (empty routes array)
├── stores/                 # Pinia stores
│   └── counter.ts         # Example counter store
```

### Key Configuration Files
- `vite.config.ts` - Vite configuration with Vue plugin and path aliases
- `tsconfig.json` - TypeScript configuration
- `eslint.config.ts` - ESLint configuration for Vue + TypeScript
- `.prettierrc.json` - Prettier formatting rules

### Path Aliases
- `@` → `src/` directory (configured in vite.config.ts)

### Current State
- Basic Vue 3 setup with Ant Design Vue integration
- Empty router (no routes defined yet)
- Single example Pinia store (counter)
- No custom components or views created
- Ready for feature development

### Development Notes
- Uses Ant Design Vue's reset CSS
- Vue DevTools enabled in development
- Node.js 20.19.0+ or 22.12.0+ required
