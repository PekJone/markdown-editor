<template>
  <div class="documents-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>Markdown Editor</h1>
          <div class="header-actions">
            <el-button type="primary" @click="createNewDocument">
              <el-icon><Plus /></el-icon>
              新建文档
            </el-button>
            <div class="user-profile">
              <router-link v-if="authStore.user?.id" :to="`/user/${authStore.user?.id}`" class="user-avatar-link">
                <el-avatar :size="48" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              </router-link>
              <el-avatar v-else :size="48">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.original || 0 }}</span>
                  <span class="stat-label">原创</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.likes || 0 }}</span>
                  <span class="stat-label">点赞</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.collections || 0 }}</span>
                  <span class="stat-label">收藏</span>
                </div>
              </div>
              <el-dropdown @command="handleCommand" class="user-menu">
                <span class="user-menu-trigger">
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                  <el-dropdown-item command="documents">我的文档</el-dropdown-item>
                  <el-dropdown-item command="public">公开文档</el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <span class="dropdown-item-with-badge">
                      <span>消息中心</span>
                      <el-badge v-if="unreadMessageCount > 0" :value="unreadMessageCount > 99 ? '99+' : unreadMessageCount" class="message-badge" />
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item command="content-management" divided>内容管理</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px" class="category-sidebar">
          <div class="sidebar-header">
            <h3>分类</h3>
          </div>
          <div class="category-list">
            <el-menu :default-active="selectedCategory || 'all'" @select="handleCategorySelect">
              <el-menu-item index="my">
                <el-icon><User /></el-icon>
                <span>我的</span>
              </el-menu-item>
              <el-menu-item index="all">
                <el-icon><Menu /></el-icon>
                <span>全部</span>
              </el-menu-item>
              <el-menu-item v-for="category in categories" :key="category.dictCode" :index="category.dictCode">
                <el-icon><Document /></el-icon>
                <span>{{ category.dictLabel }}</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-aside>

        <el-main>
          <div class="toolbar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文档..."
              prefix-icon="Search"
              @keyup.enter="searchDocuments"
              style="width: 300px"
            />
          </div>

          <el-table :data="documents" v-loading="loading" style="width: 100%" empty-text="暂无数据">
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <router-link :to="`/document/${row.id}`" class="doc-link">
                  {{ row.title }}
                </router-link>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="120">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ getCategoryLabel(row.category) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="tags" label="标签" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.tags" size="small">{{ row.tags }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="permission" label="权限" width="150">
              <template #default="{ row }">
                <el-tag 
                  :type="row.permission === 'public' ? 'success' : row.permission === 'private' ? 'info' : 'danger'" 
                  size="small"
                >
                  {{ 
                    row.permission === 'public' ? '公开' : 
                    row.permission === 'private' ? '私密' : '绝密' 
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.userId === userInfo.id" 
                  link 
                  type="primary" 
                  @click="editDocument(row.id)"
                >
                  编辑
                </el-button>
                <el-button link type="danger" @click="deleteDocument(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadDocuments"
            @current-change="loadDocuments"
            style="margin-top: 20px; justify-content: center"
          />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api, { avatarApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'



const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const documents = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const selectedCategory = ref('')
const userStats = ref({
  original: 0,
  likes: 0,
  collections: 0
})
const categories = ref([])
const userInfo = ref({ id: null })
const unreadMessageCount = ref(0)

// 计算属性：获取分类的中文标签
const getCategoryLabel = (categoryCode) => {
  if (!categoryCode || categories.value.length === 0) {
    return categoryCode
  }
  const category = categories.value.find(cat => cat.dictCode === categoryCode)
  return category ? category.dictLabel : categoryCode
}

const loadDocuments = async () => {
  loading.value = true
  try {
    if (selectedCategory.value === 'my') {
      // 只显示当前用户的文档
      const params = {
        page: currentPage.value - 1,
        size: pageSize.value
      }
      const response = await api.get('/api/documents/my', { params })
      documents.value = response && response.records ? response.records : []
      total.value = response && response.total ? response.total : 0
    } else if (selectedCategory.value) {
      // 为分类查询添加分页参数
      const params = {
        page: currentPage.value - 1,
        size: pageSize.value
      }
      const response = await api.get(`/api/documents/category/${selectedCategory.value}`, { params })
      documents.value = response && response.records ? response.records : []
      total.value = response && response.total ? response.total : 0
    } else {
      const params = {
        page: currentPage.value - 1,
        size: pageSize.value
      }
      const response = await api.get('/api/documents', { params })
      documents.value = response && response.records ? response.records : []
      total.value = response && response.total ? response.total : 0
    }
  } catch (error) {
    console.error('Failed to load documents:', error)
  } finally {
    loading.value = false
  }
}

const createNewDocument = () => {
  router.push('/editor')
}

const editDocument = (id) => {
  router.push(`/editor/${id}`)
}

const deleteDocument = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个文档吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.delete(`/api/documents/${id}`)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete document:', error)
    }
  }
}

const searchDocuments = async () => {
  const trimmedKeyword = searchKeyword.value.trim()
  if (!trimmedKeyword) {
    loadDocuments()
    return
  }
  
  loading.value = true
  try {
    const params = {
      keyword: trimmedKeyword,
      page: currentPage.value - 1,
      size: pageSize.value,
      scope: selectedCategory.value === 'my' ? 'my' : 'all',
      category: selectedCategory.value === 'my' || !selectedCategory.value ? undefined : selectedCategory.value
    }
    
    if (selectedCategory.value === 'my') {
      // 搜索用户自己的文档
      const response = await api.get('/api/documents/search', { params })
      documents.value = response && response.records ? response.records : []
      total.value = response && response.total ? response.total : 0
    } else if (selectedCategory.value) {
      // 如果选择了其他分类，使用后端搜索API
      const response = await api.get('/api/documents/search', { params })
      documents.value = response && response.records ? response.records : []
      total.value = response && response.total ? response.total : 0
    } else {
      // 如果没有选择分类，使用后端搜索API
      const response = await api.get('/api/documents/search', { params })
      documents.value = response && response.records ? response.records : []
      total.value = response && response.total ? response.total : 0
    }
  } catch (error) {
    console.error('Failed to search documents:', error)
  } finally {
    loading.value = false
  }
}

const filterByCategory = () => {
  if (!selectedCategory.value) {
    loadDocuments()
    return
  }
  
  loading.value = true
  try {
    api.get(`/api/documents/category/${selectedCategory.value}`)
      .then(response => {
        documents.value = Array.isArray(response) ? response : []
        total.value = Array.isArray(response) ? response.length : 0
      })
      .finally(() => {
        loading.value = false
      })
  } catch (error) {
    console.error('Failed to filter documents:', error)
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  } else if (command === 'public') {
    router.push('/public')
  } else if (command === 'documents') {
    router.push('/documents')
  } else if (command === 'messages') {
    router.push('/messages')
  } else if (command === 'content-management') {
    router.push('/content-management')
  }
}

const handleCategorySelect = (key) => {
  if (key === 'all') {
    selectedCategory.value = ''
  } else {
    selectedCategory.value = key
  }
  loadDocuments()
}

const loadUserStats = async () => {
  try {
    const response = await api.get('/api/users/stats')
    userStats.value = response || {
      original: 0,
      likes: 0,
      collections: 0
    }
  } catch (error) {
    console.error('Failed to load user stats:', error)
    // 加载失败时使用模拟数据
    userStats.value = {
      original: 96,
      likes: 1652,
      collections: 1326
    }
  }
}

const loadCategories = async () => {
  try {
    const response = await api.get('/api/dictionary/type/article_category')
    categories.value = Array.isArray(response) ? response : []
  } catch (error) {
    console.error('Failed to load categories:', error)
    // 加载失败时使用默认分类
    categories.value = [
      { dictLabel: '技术' },
      { dictLabel: '生活' },
      { dictLabel: '工作' },
      { dictLabel: '娱乐' },
      { dictLabel: '其他' }
    ]
  }
}

const loadUserInfo = async () => {
  try {
    const response = await api.get('/api/users/info')
    userInfo.value = response || {}
    // 同时更新authStore中的用户信息，包括头像
    if (response && authStore.user) {
      authStore.user.avatar = response.avatar
    }
  } catch (error) {
    console.error('Failed to load user info:', error)
  }
}

const loadUnreadMessageCount = async () => {
  try {
    const response = await api.get('/api/messages/unread/count')
    unreadMessageCount.value = response || 0
  } catch (error) {
    console.error('Failed to load unread message count:', error)
    unreadMessageCount.value = 0
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadUserInfo()
  loadDocuments()
  loadUserStats()
  loadCategories()
  loadUnreadMessageCount()
})
</script>

<style scoped lang="scss">
.documents-container {
  height: 100vh;
  
  .el-container {
    height: 100%;
  }
  
  .el-container:nth-child(2) {
    height: calc(100vh - 60px);
  }
}

.el-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  
  .header-content {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h1 {
      margin: 0;
      font-size: 24px;
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: 20px;
    }
  }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 15px;
  color: white;
  
  .user-avatar-link {
    text-decoration: none;
    color: inherit;
  }
  
  .user-stats {
    display: flex;
    gap: 20px;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        display: block;
        font-size: 16px;
        font-weight: 600;
        color: white;
      }
      
      .stat-label {
        display: block;
        font-size: 12px;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
  
  .user-menu {
    .user-menu-trigger {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: white;
      
      .el-icon {
        font-size: 16px;
      }
    }
  }
}

.el-aside {
  background: white;
  border-right: 1px solid #e0e0e0;
  padding: 20px 0;
  
  .sidebar-header {
    padding: 0 20px 15px;
    border-bottom: 1px solid #f0f0f0;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .category-list {
    margin-top: 15px;
    
    .el-menu {
      border-right: none;
      
      .el-menu-item {
        height: 48px;
        line-height: 48px;
        margin: 0 10px;
        border-radius: 8px;
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        &.is-active {
          background-color: #e6f7ff;
          color: #1890ff;
        }
      }
    }
  }
}

.el-main {
  background: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}

.toolbar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.doc-link {
  color: #667eea;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

.dropdown-item-with-badge {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.message-badge {
  margin-left: 8px;
}
</style>
