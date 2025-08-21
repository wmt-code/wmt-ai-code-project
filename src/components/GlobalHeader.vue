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
        <template v-if="loginUserStore.loginUser.id">
          <a-dropdown :trigger="['hover']" placement="bottomRight">
            <div class="user-avatar-wrapper">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="36" class="user-avatar">
                <template v-if="!loginUserStore.loginUser.userAvatar">
                  {{ getAvatarText() }}
                </template>
              </a-avatar>
              <span class="username">{{
                loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount
              }}</span>
              <DownOutlined class="dropdown-icon" />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="goToProfile">
                  <UserOutlined />
                  个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <router-link to="/user/login">
            <a-button type="primary">登录</a-button>
          </router-link>
        </template>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LogoutOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { logout } from '@/api/userController'
import { message } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const selectedKeys = ref(['home'])

const originItems = [
  {
    key: '/',
    label: '首页',
    title: '首页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

const getAvatarText = () => {
  const userName = loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || 'U'
  return userName.charAt(0).toUpperCase()
}

const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const goToProfile = () => {
  router.push('/user/profile')
}

const handleLogout = async () => {
  try {
    const res = await logout()
    if (res.data.code == 0 && res.data.data) {
      loginUserStore.setLoginUser({})
      message.success('已退出登录')
      await router.push('/')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '退出登录失败')
  }
}

const handleLogoError = (event: Event) => {
  console.warn('Logo图片加载失败，使用默认样式')
  const target = event.target as HTMLImageElement
  target.style.display = 'none'
}

router.afterEach((to) => {
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

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 20px;
  transition: background-color 0.3s;
  background-color: rgba(255, 255, 255, 0.1);
  margin-left: 16px;
}

.user-avatar-wrapper:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-avatar {
  margin-right: 8px;
  background-color: #1890ff;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.username {
  margin-right: 8px;
  font-weight: 500;
  color: #fff;
  font-size: 14px;
}

.dropdown-icon {
  font-size: 12px;
  color: #fff;
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
