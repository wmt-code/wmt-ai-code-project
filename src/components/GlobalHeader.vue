<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <div class="header-left">
        <router-link to="/">
          <div class="logo-container">
            <img src="@/assets/logo.webp" alt="Logo" class="logo" @error="handleLogoError" />
            <span class="site-title">WMT AI 零代码平台</span>
          </div>
        </router-link>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        theme="dark"
        class="navigation-menu"
        :items="menuItems"
        @click="handleMenuClick"
      />
      <div class="header-right">
        <a-button type="primary" @click="handleLogin">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { health } from '@/api/healthController.ts'
const router = useRouter()
const selectedKeys = ref(['home'])

const menuItems = [
  {
    key: '/',
    label: '首页',
    title: '首页',
  },
  {
    key: 'projects',
    label: '项目',
    title: '项目',
  },
  {
    key: 'ai-tools',
    label: 'AI工具',
    title: 'AI工具',
  },
  {
    key: 'settings',
    label: '设置',
    title: '设置',
  },
]
// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const handleLogin = () => {
  console.log('点击登录按钮')
  // 这里可以跳转到登录页面
  // router.push('/login')
}

const handleLogoError = (event: Event) => {
  console.warn('Logo图片加载失败，使用默认样式')
  const target = event.target as HTMLImageElement
  target.style.display = 'none'
}
//监听路由变化，更新选中菜单项
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})
</script>

<style scoped>
.global-header {
  background: linear-gradient(135deg, #b6bacc 0%, #a186bd 100%);
  padding: 0;
  height: 64px;
  line-height: 64px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  height: 42px;
  width: 42px;
  margin-right: 12px;
  border-radius: 4px;
  object-fit: contain;
}

.site-title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.navigation-menu {
  flex: 1;
  border-bottom: none;
  background: transparent !important;
  margin: 0 48px;
}

.navigation-menu :deep(.ant-menu) {
  background: transparent !important;
}

.navigation-menu :deep(.ant-menu-dark) {
  background: transparent !important;
}

.navigation-menu :deep(.ant-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
}

.navigation-menu :deep(.ant-menu-item:hover) {
  color: #fff !important;
}

.navigation-menu :deep(.ant-menu-item-selected) {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.2) !important;
}

.header-right {
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .logo-container {
    margin-right: 24px;
  }

  .site-title {
    font-size: 16px;
  }

  .navigation-menu {
    display: none;
  }
}
</style>
