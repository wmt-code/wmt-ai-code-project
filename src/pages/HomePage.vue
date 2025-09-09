<script setup lang="ts">
import { onMounted, reactive, ref, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, getGoodAppListPage, getMyAppListPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const editorRef = ref<HTMLElement | null>(null)
const creating = ref(false)
const placeholderText = ref('使用 NIHILIST 创建—')
let typewriterTimer: number | null = null

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.innerText = prompt
    }
  })
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await getMyAppListPage({
      current: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await getGoodAppListPage({
      current: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
  startTypewriter()
})

onBeforeUnmount(() => {
  if (typewriterTimer) {
    window.clearInterval(typewriterTimer)
    typewriterTimer = null
  }
})

const startTypewriter = () => {
  const words = ['使用 NIHILIST 创建—', '创建企业官网', '打造艺术电商网站', '生成数据分析平台']
  let wordIndex = 0
  let charIndex = 0
  let direction: 'forward' | 'back' = 'forward'
  const typingSpeed = 100
  const pause = 1200

  const tick = () => {
    const current = words[wordIndex]
    if (direction === 'forward') {
      charIndex++
      placeholderText.value = current.slice(0, charIndex)
      if (charIndex === current.length) {
        direction = 'back'
        window.clearInterval(typewriterTimer as number)
        window.setTimeout(() => {
          typewriterTimer = window.setInterval(tick, typingSpeed) as unknown as number
        }, pause)
      }
    } else {
      charIndex--
      placeholderText.value = current.slice(0, Math.max(1, charIndex))
      if (charIndex <= 1) {
        direction = 'forward'
        wordIndex = (wordIndex + 1) % words.length
      }
    }
  }
  typewriterTimer = window.setInterval(tick, typingSpeed) as unknown as number
}

// 外部修改 userPrompt 时，同步到可编辑区域
watch(userPrompt, (val) => {
  if (editorRef.value && editorRef.value.innerText !== val) {
    editorRef.value.innerText = val
  }
})

// 编辑器按键：Ctrl+Enter 提交
const onEditorKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && (e.key === 'Enter' || e.keyCode === 13)) {
    e.preventDefault()
    createApp()
  }
}
</script>

<template>
  <div id="homePage" class="min-h-screen w-full m-0 p-0 relative overflow-hidden">
    <div class="absolute inset-0 hero-gradient"></div>
    <div class="absolute inset-0 noise-overlay"></div>
    <div class="container-1200 py-5 relative z-10 w-full box-border">
      <!-- 网站标题和描述 -->
      <div class="text-center py-20 md:py-16 mb-7 text-slate-800 relative overflow-hidden">
        <h1
          class="text-5xl md:text-6xl font-bold mb-5 leading-tight bg-gradient-to-tr from-blue-500 via-violet-500 to-emerald-500 bg-clip-text text-transparent tracking-tight relative z-10">
          AI 应用生成平台
        </h1>
        <p class="text-base md:text-lg m-0 opacity-80 text-slate-500 relative z-10">一句话轻松创建网站应用</p>
      </div>

      <!-- 用户提示卡片编辑器（保持原功能） -->
      <div class="relative mx-auto mb-6 max-w-[800px] prompt-surface">
        <div class="prompt-editor" contenteditable="true" :data-empty="!userPrompt" ref="editorRef"
          @input="(e: any) => { userPrompt = (e.target.innerText || '').slice(0, 1000) }" @keydown="onEditorKeydown">
        </div>
        <span v-if="!userPrompt" class="prompt-placeholder">{{ placeholderText }}</span>
        <div class="absolute bottom-3 right-3 flex gap-2 items-center">
          <button class="btn-primary" @click="createApp">开始创建</button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="flex gap-3 justify-center mb-12 flex-wrap">
        <a-button type="default" @click="
          setPrompt(
            '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
          )
          "
          class="rounded-[25px] px-5 py-2 h-auto bg-white/80 border border-blue-500/20 text-slate-600 hover:bg-white/90 hover:border-blue-500/40 hover:text-blue-500 transition-all hover:-translate-y-0.5 shadow-[0_4px_12px_rgba(59,130,246,0.15)]">个人博客网站</a-button>
        <a-button type="default" @click="
          setPrompt(
            '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
          )
          "
          class="rounded-[25px] px-5 py-2 h-auto bg-white/80 border border-blue-500/20 text-slate-600 hover:bg-white/90 hover:border-blue-500/40 hover:text-blue-500 transition-all hover:-translate-y-0.5 shadow-[0_4px_12px_rgba(59,130,246,0.15)]">企业官网</a-button>
        <a-button type="default" @click="
          setPrompt(
            '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
          )
          "
          class="rounded-[25px] px-5 py-2 h-auto bg-white/80 border border-blue-500/20 text-slate-600 hover:bg-white/90 hover:border-blue-500/40 hover:text-blue-500 transition-all hover:-translate-y-0.5 shadow-[0_4px_12px_rgba(59,130,246,0.15)]">在线商城</a-button>
        <a-button type="default" @click="
          setPrompt(
            '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
          )
          "
          class="rounded-[25px] px-5 py-2 h-auto bg-white/80 border border-blue-500/20 text-slate-600 hover:bg-white/90 hover:border-blue-500/40 hover:text-blue-500 transition-all hover:-translate-y-0.5 shadow-[0_4px_12px_rgba(59,130,246,0.15)]">作品展示网站</a-button>
      </div>

      <!-- 我的作品 -->
      <div class="section-wrap">
        <h2 class="section-title">我的作品</h2>
        <div class="grid-cards contain-layout-style">
          <AppCard v-for="app in myApps" :key="app.id" :app="app" @view-chat="viewChat" @view-work="viewWork" />
        </div>
        <div class="pager-wrap">
          <a-pagination v-model:current="myAppsPage.current" v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total" :show-size-changer="false" :show-total="(total) => `共 ${total} 个应用`"
            @change="loadMyApps" />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section-wrap">
        <h2 class="section-title">精选案例</h2>
        <div class="grid-cards contain-layout-style">
          <AppCard v-for="app in featuredApps" :key="app.id" :app="app" :featured="true" @view-chat="viewChat"
            @view-work="viewWork" />
        </div>
        <div class="pager-wrap">
          <a-pagination v-model:current="featuredAppsPage.current" v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total" :show-size-changer="false" :show-total="(total) => `共 ${total} 个案例`"
            @change="loadFeaturedApps" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
