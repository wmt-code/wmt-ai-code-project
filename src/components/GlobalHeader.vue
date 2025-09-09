<template>
  <a-layout-header
    class="sticky top-0 z-[1000] h-16 leading-[64px] shadow-md bg-gradient-to-br from-slate-300 to-violet-300 p-0">
    <div class="flex items-center justify-between container-1200 h-full">
      <div class="header-left">
        <router-link to="/">
          <div class="flex items-center">
            <img src="@/assets/logo.webp" alt="Logo" class="h-42px w-42px mr-3 rounded object-contain"
              @error="handleLogoError" />
            <span class="text-white text-[18px] font-semibold whitespace-nowrap">WMT AI 零代码平台</span>
          </div>
        </router-link>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" mode="horizontal" theme="dark"
        class="flex-1 border-b-0 bg-transparent !m-0 md:!mx-12 hidden md:block" :items="menuItems"
        @click="handleMenuClick" />
      <div class="flex items-center">
        <template v-if="loginUserStore.loginUser.id">
          <a-dropdown :trigger="['hover']" placement="bottomRight">
            <div
              class="flex items-center cursor-pointer px-3 py-2 rounded-[20px] hover:bg-white/20 ml-4 transition-colors">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="36"
                class="mr-2 bg-blue-500 text-white border-2 border-white/30">
                <template v-if="!loginUserStore.loginUser.userAvatar">
                  {{ getAvatarText() }}
                </template>
              </a-avatar>
              <span class="mr-2 font-medium text-white text-sm">{{
                loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount
                }}</span>
              <DownOutlined class="text-xs text-white" />
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
  {
    key: '/admin/chatManage',
    label: '对话管理',
    title: '对话管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (!menu || typeof menu !== 'object') return false
    const key = String((menu as any).key ?? '')
    if (key.startsWith('/admin')) {
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

<style scoped></style>
