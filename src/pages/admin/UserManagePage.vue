<template>
  <div class="user-manage-container">
    <a-card class="user-manage-card">
      <template #title>
        <div class="card-title">
          <UserOutlined />
          <span>用户管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-section">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.id"
              placeholder="用户ID"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.userName"
              placeholder="用户名称"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.userAccount"
              placeholder="用户账号"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-select
              v-model:value="searchForm.userRole"
              placeholder="用户角色"
              allow-clear
              style="width: 100%"
            >
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </a-col>
        </a-row>
        <a-row :gutter="[16, 16]" style="margin-top: 16px">
          <a-col :span="24">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><RedoOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-section">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          添加用户
        </a-button>
      </div>

      <!-- 表格区域 -->
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="{
          current: paginationConfig.current,
          pageSize: paginationConfig.pageSize,
          total: paginationConfig.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条记录`,
          pageSizeOptions: ['10', '20', '50', '100'],
          onChange: (page: number, pageSize: number) => {
            paginationConfig.current = page
            paginationConfig.pageSize = pageSize
            fetchUserList()
          },
          onShowSizeChange: (current: number, size: number) => {
            paginationConfig.current = 1
            paginationConfig.pageSize = size
            fetchUserList()
          },
        }"
        :scroll="{ x: 1200 }"
        bordered
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <!-- 头像列 -->
          <template v-if="column.key === 'userAvatar'">
            <a-avatar :src="record.userAvatar || defaultAvatar" :size="40">
              <template v-if="!record.userAvatar">
                {{ getAvatarText(record.userName || record.userAccount) }}
              </template>
            </a-avatar>
          </template>
          <!-- 角色列 -->
          <template v-if="column.key === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'red' : 'blue'">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>

          <!-- 创建时间列 -->
          <template v-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <!-- 操作列 -->
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <EditOutlined />
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除该用户吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>
                  <DeleteOutlined />
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 编辑用户对话框 -->
      <a-modal
        v-model:open="editModalVisible"
        title="编辑用户"
        :confirm-loading="editLoading"
        @ok="handleEditSubmit"
        @cancel="handleEditCancel"
      >
        <a-form :model="editForm" :rules="editRules" layout="vertical" ref="editFormRef">
          <a-form-item label="用户ID" name="id">
            <a-input v-model:value="editForm.id" disabled />
          </a-form-item>

          <a-form-item label="用户账号" name="userAccount">
            <a-input v-model:value="editForm.userAccount" disabled />
          </a-form-item>

          <a-form-item label="用户名称" name="userName">
            <a-input v-model:value="editForm.userName" placeholder="请输入用户名称" />
          </a-form-item>

          <a-form-item label="用户角色" name="userRole">
            <a-select v-model:value="editForm.userRole" placeholder="请选择用户角色">
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="个人简介" name="userProfile">
            <a-textarea
              v-model:value="editForm.userProfile"
              placeholder="请输入个人简介"
              :rows="3"
              :maxlength="200"
              show-count
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 添加用户对话框 -->
      <a-modal
        v-model:open="addModalVisible"
        title="添加用户"
        :confirm-loading="addLoading"
        @ok="handleAddSubmit"
        @cancel="handleAddCancel"
      >
        <a-form :model="addForm" :rules="addRules" layout="vertical" ref="addFormRef">
          <a-form-item label="用户账号" name="userAccount">
            <a-input v-model:value="addForm.userAccount" placeholder="请输入用户账号" />
          </a-form-item>

          <a-form-item label="用户名称" name="userName">
            <a-input v-model:value="addForm.userName" placeholder="请输入用户名称" />
          </a-form-item>
          <a-form-item label="用户角色" name="userRole">
            <a-select v-model:value="addForm.userRole" placeholder="请选择用户角色">
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="个人简介" name="userProfile">
            <a-textarea
              v-model:value="addForm.userProfile"
              placeholder="请输入个人简介"
              :rows="3"
              :maxlength="200"
              show-count
            />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  RedoOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { addUser, deleteUserById, listUserVoByPage, updateUserById } from '@/api/userController'

// 默认头像
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=user'

// 搜索表单
const searchForm = reactive({
  id: undefined,
  userName: '',
  userAccount: '',
  userRole: undefined,
})

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    fixed: 'left' as const,
  },
  {
    title: '头像',
    key: 'userAvatar',
    width: 80,
    align: 'center' as const,
  },
  {
    title: '用户账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    width: 120,
  },
  {
    title: '用户名称',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
  },
  {
    title: '用户角色',
    key: 'userRole',
    width: 100,
    align: 'center' as const,
  },
  {
    title: '个人简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true,
    width: 200,
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
    align: 'center' as const,
  },
]

// 数据列表
const userList = ref<API.UserVo[]>([])
const loading = ref(false)

// 分页配置
const paginationConfig = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
  pageSizeOptions: ['10', '20', '50', '100'],
})

// 编辑相关
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: 0,
  userAccount: '',
  userName: '',
  userRole: 'user',
  userStatus: 0,
  userProfile: '',
})
const editFormRef = ref<FormInstance>()

// 添加相关
const addModalVisible = ref(false)
const addLoading = ref(false)
const addForm = reactive({
  userAccount: '',
  userName: '',
  userPassword: '',
  userRole: 'user',
  userProfile: '',
})
const addFormRef = ref<FormInstance>()

// 表单验证规则
const editRules = {
  userName: [
    { required: true, message: '请输入用户名称' },
    { min: 2, max: 20, message: '用户名称长度必须在2-20位之间' },
  ],
  userRole: [{ required: true, message: '请选择用户角色' }],
  userProfile: [{ max: 200, message: '个人简介最多200个字符' }],
}

const addRules = {
  userAccount: [
    { required: true, message: '请输入用户账号' },
    { min: 4, max: 20, message: '用户账号长度必须在4-20位之间' },
  ],
  userName: [
    { required: true, message: '请输入用户名称' },
    { min: 2, max: 20, message: '用户名称长度必须在2-20位之间' },
  ],
  userRole: [{ required: true, message: '请选择用户角色' }],
  userProfile: [{ max: 200, message: '个人简介最多200个字符' }],
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      id: searchForm.id,
      userName: searchForm.userName || undefined,
      userAccount: searchForm.userAccount || undefined,
      userRole: searchForm.userRole || undefined,
      current: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
    }

    const res = await listUserVoByPage(params)
    if (res.data.code === 0 && res.data.data) {
      userList.value = res.data.data.records || []
      paginationConfig.total = res.data.data.totalRow || 0
    } else {
      message.error(res.message || '获取用户列表失败')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  paginationConfig.current = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  searchForm.id = undefined
  searchForm.userName = ''
  searchForm.userAccount = ''
  searchForm.userRole = undefined
  paginationConfig.current = 1
  fetchUserList()
}

// 获取头像文字
const getAvatarText = (name: string) => {
  return name.charAt(0).toUpperCase()
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 编辑用户
const handleEdit = (record: API.UserVo) => {
  editForm.id = record.id!
  editForm.userAccount = record.userAccount!
  editForm.userName = record.userName || ''
  editForm.userRole = record.userRole || 'user'
  editForm.userProfile = record.userProfile || ''
  editModalVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  try {
    await editFormRef.value?.validate()
    editLoading.value = true

    const res = await updateUserById({
      id: editForm.id,
      userName: editForm.userName,
      userRole: editForm.userRole,
      userProfile: editForm.userProfile,
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('用户信息更新成功')
      editModalVisible.value = false
      fetchUserList()
    } else {
      message.error(res.message || '更新失败')
    }
  } catch (error: any) {
    if (error.response) {
      message.error(error.response.data?.message || '更新失败')
    }
  } finally {
    editLoading.value = false
  }
}

// 取消编辑
const handleEditCancel = () => {
  editFormRef.value?.resetFields()
  editModalVisible.value = false
}

// 删除用户
const handleDelete = async (id: number) => {
  try {
    const res = await deleteUserById({ id })
    if (res.data.code === 0 && res.data.data) {
      message.success('用户删除成功')
      fetchUserList()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '删除失败')
  }
}

// 添加用户
const handleAdd = () => {
  addFormRef.value?.resetFields()
  addForm.userAccount = ''
  addForm.userName = ''
  addForm.userPassword = ''
  addForm.userRole = 'user'
  addForm.userProfile = ''
  addModalVisible.value = true
}

// 提交添加
const handleAddSubmit = async () => {
  try {
    await addFormRef.value?.validate()
    addLoading.value = true

    const res = await addUser({
      userAccount: addForm.userAccount,
      userName: addForm.userName,
      userRole: addForm.userRole,
      userProfile: addForm.userProfile,
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('用户添加成功')
      addModalVisible.value = false
      await fetchUserList()
    } else {
      message.error(res.message || '添加失败')
    }
  } catch (error: any) {
    if (error.response) {
      message.error(error.response.data?.message || '添加失败')
    }
  } finally {
    addLoading.value = false
  }
}

// 取消添加
const handleAddCancel = () => {
  addFormRef.value?.resetFields()
  addModalVisible.value = false
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.user-manage-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.search-section {
  margin-bottom: 24px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.action-section {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .user-manage-container {
    padding: 16px;
  }

  .search-section {
    padding: 16px;
  }
}
</style>
