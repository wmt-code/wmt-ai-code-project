<template>
  <div class="login-container">
    <div class="login-split-container">
      <!-- 左侧背景区域 -->
      <div class="login-background-section">
        <div ref="vantaRef" class="vanta-bg"></div>
        <div class="background-content">
          <h1 class="background-title">WMT AI零代码平台</h1>
          <p class="background-subtitle">智能编程，从这里开始</p>
        </div>
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="login-form-section">
        <div class="login-form-container">
          <div class="login-form">
            <div class="login-header">
              <h2>欢迎回来</h2>
              <p>登录您的账号继续您的编程之旅</p>
            </div>

            <a-form
              :model="formState"
              name="login"
              @finish="handleLogin"
              @finishFailed="onFinishFailed"
              layout="vertical"
              class="login-form-content"
            >
              <a-form-item
                label="用户账号"
                name="userAccount"
                :rules="[
                  { required: true, message: '请输入用户账号!' },
                  { min: 4, max: 20, message: '用户账号长度必须在4-20位之间' },
                ]"
              >
                <a-input
                  v-model:value="formState.userAccount"
                  placeholder="请输入用户账号"
                  size="large"
                  class="login-input"
                >
                  <template #prefix>
                    <UserOutlined />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item
                label="密码"
                name="userPassword"
                :rules="[
                  { required: true, message: '请输入密码!' },
                  { min: 6, max: 32, message: '密码长度必须在6-32位之间' },
                ]"
              >
                <a-input-password
                  v-model:value="formState.userPassword"
                  placeholder="请输入密码"
                  size="large"
                  class="login-input"
                >
                  <template #prefix>
                    <LockOutlined />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item>
                <a-button
                  type="primary"
                  html-type="submit"
                  size="large"
                  block
                  :loading="loading"
                  class="login-button"
                >
                  登录
                </a-button>
              </a-form-item>

              <div class="login-footer">
                <span>还没有账号？</span>
                <a @click="goToRegister" class="register-link">立即注册</a>
              </div>
            </a-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import * as THREE from 'three'
import HALO from 'vanta/dist/vanta.halo.min'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const vantaRef = ref<HTMLElement>()
let vantaEffect: any = null

const loading = ref(false)

const formState = reactive({
  userAccount: '',
  userPassword: '',
})

onMounted(() => {
  if (vantaRef.value) {
    vantaEffect = HALO({
      el: vantaRef.value,
      THREE: THREE,
      mouseControls: true,
      touchControls: true,
      gyroControls: false,
      minHeight: 200.0,
      minWidth: 200.0,
      scale: 1.0,
      scaleMobile: 1.0,
      size: 1.5,
      amplitudeFactor: 1.0,
      xOffset: 0.05,
      yOffset: 0.05,
      backgroundColor: 0x0a0e27,
      baseColor: 0x3f87ff,
      color: 0x3f87ff,
    })
  }
})

onUnmounted(() => {
  if (vantaEffect) {
    vantaEffect.destroy()
  }
})

const handleLogin = async (values: any) => {
  loading.value = true
  try {
    const res = await userLogin({
      userAccount: values.userAccount,
      userPassword: values.userPassword,
    })

    if (res.data.code == 0 && res.data.data) {
      message.success('登录成功！')
      console.log(res.data.data)
      loginUserStore.setLoginUser(res.data.data)
      await router.push('/')
    } else {
      message.error(res.message || '登录失败')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}

const goToRegister = () => {
  router.push('/user/register')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  margin: 0;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
}

.login-split-container {
  display: flex;
  height: 100%;
}

.login-background-section {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #0a0e27 0%, #1a237e 100%);
}

.vanta-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.background-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
  z-index: 2;
  padding: 0 40px;
}

.background-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.background-subtitle {
  font-size: 20px;
  opacity: 0.9;
  font-weight: 300;
}

.login-form-section {
  width: 500px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  box-sizing: border-box;
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.login-form {
  width: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 48px;
}

.login-header h2 {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 16px;
  color: #666;
}

.login-input {
  border-radius: 8px;
}

.login-button {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.register-link {
  color: #1890ff;
  cursor: pointer;
  margin-left: 4px;
}

.register-link:hover {
  color: #40a9ff;
}

@media (max-width: 768px) {
  .login-split-container {
    flex-direction: column;
  }

  .login-background-section {
    display: none;
  }

  .login-form-section {
    width: 100%;
    padding: 20px;
  }

  .background-title {
    font-size: 36px;
  }

  .background-subtitle {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .login-form-section {
    padding: 40px 20px;
  }

  .background-title {
    font-size: 28px;
  }

  .background-subtitle {
    font-size: 16px;
  }
}
</style>
