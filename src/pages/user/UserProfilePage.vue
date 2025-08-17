<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <template #title>
        <div class="card-title">
          <UserOutlined />
          <span>个人资料</span>
        </div>
      </template>

      <a-row :gutter="[24, 24]">
        <!-- 左侧头像区域 -->
        <a-col :xs="24" :sm="24" :md="8" :lg="6">
          <div class="avatar-section">
            <div class="avatar-upload">
              <a-upload
                :custom-request="handleAvatarUpload"
                :show-upload-list="false"
                accept="image/*"
                :before-upload="beforeUpload"
                @change="handleAvatarChange"
              >
                <a-avatar
                  :src="formState.userAvatar || defaultAvatar"
                  :size="120"
                  class="avatar-preview"
                >
                  <template v-if="!formState.userAvatar">
                    {{ getAvatarText() }}
                  </template>
                </a-avatar>
                <div class="upload-overlay">
                  <UploadOutlined />
                  <span>更换头像</span>
                </div>
              </a-upload>
            </div>
            <div class="user-info">
              <h3>
                {{ loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount }}
              </h3>
              <p>{{ loginUserStore.loginUser.userAccount }}</p>
            </div>
          </div>
        </a-col>

        <!-- 右侧表单区域 -->
        <a-col :xs="24" :sm="24" :md="16" :lg="18">
          <a-form :model="formState" layout="vertical" @finish="handleSubmit" class="profile-form">
            <a-form-item label="用户账号" name="userAccount">
              <a-input v-model:value="formState.userAccount" disabled class="readonly-input" />
            </a-form-item>

            <a-form-item
              label="用户名称"
              name="userName"
              :rules="[
                { required: true, message: '请输入用户名称' },
                { min: 2, max: 20, message: '用户名称长度必须在2-20位之间' },
              ]"
            >
              <a-input
                v-model:value="formState.userName"
                placeholder="请输入用户名称"
                size="large"
              />
            </a-form-item>

            <a-form-item>
              <a-button type="default" @click="showPasswordModal" block> 修改密码 </a-button>
            </a-form-item>

            <a-form-item
              label="个人简介"
              name="userProfile"
              :rules="[{ max: 200, message: '个人简介最多200个字符' }]"
            >
              <a-textarea
                v-model:value="formState.userProfile"
                placeholder="介绍一下自己吧..."
                :rows="4"
                :maxlength="200"
                show-count
              />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" :loading="loading" block>
                保存修改
              </a-button>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
    </a-card>
  </div>

  <!-- 修改密码对话框 -->
  <a-modal
    v-model:open="passwordModalVisible"
    title="修改密码"
    :confirm-loading="passwordLoading"
    @ok="handleChangePassword"
    @cancel="handlePasswordCancel"
  >
    <a-form :model="passwordForm" :rules="passwordRules" layout="vertical" ref="passwordFormRef">
      <a-form-item label="原密码" name="oldPassword">
        <a-input-password
          v-model:value="passwordForm.oldPassword"
          placeholder="请输入原密码（可选）"
          size="large"
        />
      </a-form-item>

      <a-form-item label="新密码" name="newPassword">
        <a-input-password
          v-model:value="passwordForm.newPassword"
          placeholder="请输入新密码（6-32位）"
          size="large"
        />
      </a-form-item>

      <a-form-item label="确认密码" name="confirmPassword">
        <a-input-password
          v-model:value="passwordForm.confirmPassword"
          placeholder="请确认新密码"
          size="large"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { updateSelf } from '@/api/userController'
import { uploadAvatar } from '@/api/userController'
import type { FormInstance } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()
const loading = ref(false)

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=user'

const formState = reactive({
  userAccount: '',
  userName: '',
  userProfile: '',
  userAvatar: '',
})

// 修改密码相关
const passwordModalVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = {
  newPassword: [{ min: 6, max: 32, message: '密码长度必须在6-32位之间' }],
  confirmPassword: [
    {
      validator: (_: any, value: string) => {
        if (!value || passwordForm.newPassword === value) {
          return Promise.resolve()
        }
        return Promise.reject(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur',
    },
  ],
}

const getAvatarText = () => {
  const userName = formState.userName || formState.userAccount || 'U'
  return userName.charAt(0).toUpperCase()
}

const initFormData = () => {
  const user = loginUserStore.loginUser
  formState.userAccount = user.userAccount || ''
  formState.userName = user.userName || ''
  formState.userProfile = user.userProfile || ''
  formState.userAvatar = user.userAvatar || ''
}

// 显示修改密码对话框
const showPasswordModal = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordModalVisible.value = true
}

// 处理修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate()

    // 验证新密码和确认密码
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      message.error('两次输入的新密码不一致')
      return
    }

    if (passwordForm.newPassword && passwordForm.newPassword.length < 6) {
      message.error('新密码长度必须在6-32位之间')
      return
    }

    passwordLoading.value = true
    // 使用 updateSelf 方法更新密码
    const res = await updateSelf(passwordForm)

    if (res.data.code === 0 && res.data.data) {
      message.success('密码修改成功')
      passwordModalVisible.value = false
      // 清空表单
      passwordFormRef.value?.resetFields()
    } else {
      message.error(res.data.message || '密码修改失败')
    }
  } catch (error: any) {
    if (error.response) {
      message.error(error.response.data?.message || '密码修改失败')
    }
  } finally {
    passwordLoading.value = false
  }
}

// 取消修改密码
const handlePasswordCancel = () => {
  passwordFormRef.value?.resetFields()
  passwordModalVisible.value = false
}

const handleAvatarUpload = async ({ file }: any) => {
  try {
    loading.value = true
    const res = await uploadAvatar({}, file as File)
    if (res.data.code == 0 && res.data.data) {
      formState.userAvatar = res.data.data
      message.success('头像上传成功')
    } else {
      message.error(result.message || '头像上传失败')
    }
  } catch (error: any) {
    message.error(error.message || '头像上传失败')
  } finally {
    loading.value = false
  }
}

const handleAvatarChange = (info: any) => {
  if (info.file.status === 'done') {
    message.success(`${info.file.name} 上传成功`)
  } else if (info.file.status === 'error') {
    message.error(`${info.file.name} 上传失败`)
  }
}

const handleSubmit = async (values: any) => {
  try {
    loading.value = true
    const res = await updateSelf({
      userName: values.userName,
      userProfile: values.userProfile,
      userAvatar: formState.userAvatar,
    })

    if (res.data.code === 0) {
      message.success('个人信息更新成功')
      // 更新本地用户信息
      loginUserStore.setLoginUser({
        ...loginUserStore.loginUser,
        userName: values.userName,
        userProfile: values.userProfile,
        userAvatar: formState.userAvatar,
      })
    } else {
      message.error(res.message || '更新失败')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '更新失败')
  } finally {
    loading.value = false
  }
}
/**
 * 上传前校验
 * @param file
 */
const beforeUpload = (file: any) => {
  const isJpgOrPng =
    file.type === 'image/jpeg' ||
    file.type === 'image/png' ||
    file.type === 'image/webp' ||
    file.type === 'image/bmp' ||
    file.type === 'image/gif'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('不能上传超过 2M 的图片')
  }
  return isJpgOrPng && isLt2M
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.profile-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.avatar-section {
  text-align: center;
  padding: 24px;
}

.avatar-upload {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar-preview {
  border: 2px solid #f0f0f0;
  transition: all 0.3s;
}

.avatar-preview:hover {
  border-color: #1890ff;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-upload:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay :deep(.anticon) {
  font-size: 24px;
  margin-bottom: 4px;
}

.user-info h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.user-info p {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.profile-form {
  padding: 24px 0;
}

.readonly-input {
  background-color: #f5f5f5;
  color: #666;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .avatar-section {
    padding: 16px;
  }

  .profile-form {
    padding: 16px 0;
  }
}
</style>
