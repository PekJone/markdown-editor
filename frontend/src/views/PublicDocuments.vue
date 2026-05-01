<template>
  <div class="public-container">
    <div class="header">
      <h1>Markdown Editor - 公开文档</h1>
      <div class="header-actions">
        <template v-if="isAuthenticated">
          <div class="user-profile">
            <router-link :to="`/user/${user?.id}`" class="user-avatar-link">
              <el-avatar :size="48" :src="user?.avatar">{{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'U' }}</el-avatar>
            </router-link>
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
        </template>
        <template v-else>
          <router-link to="/login">
            <el-button type="primary">登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button>注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
    
    <div class="content">
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索公开文档..."
          prefix-icon="Search"
          @keyup.enter="searchDocuments"
          style="width: 300px"
        />
      </div>
      
      <div class="document-list">
        <div class="document-item" v-for="doc in documents" :key="doc.id" @click="viewDocument(doc.id)">
          <div class="document-card">
            <h3 class="doc-title">{{ doc.title }}</h3>
            <p class="doc-content">{{ getPreview(doc.content) }}</p>
            <div class="doc-footer">
              <span class="category">{{ doc.category || '未分类' }}</span>
              <span class="view-count">{{ doc.viewCount }} 次浏览</span>
              <span class="update-time">{{ formatDate(doc.updatedAt) }}</span>
            </div>
          </div>
        </div>
        <div v-if="documents.length === 0" class="no-documents">
          暂无公开文档
        </div>
      </div>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[9, 18, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const documents = ref([])
const currentPage = ref(1)
const pageSize = ref(9)
const total = ref(0)
const searchKeyword = ref('')
const userStats = ref({ original: 0, likes: 0, collections: 0 })
const unreadMessageCount = ref(0)

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  } else if (command === 'documents') {
    router.push('/documents')
  } else if (command === 'public') {
    router.push('/public')
  } else if (command === 'messages') {
    router.push('/messages')
  } else if (command === 'content-management') {
    router.push('/content-management')
  }
}

const loadUserStats = async () => {
  if (!isAuthenticated.value) return
  
  try {
    const response = await api.get('/api/users/stats')
    if (response) {
      userStats.value = response
    }
  } catch (error) {
    console.error('Failed to load user stats:', error)
  }
}

const loadUnreadMessageCount = async () => {
  if (!isAuthenticated.value) return
  
  try {
    const response = await api.get('/api/messages/unread/count')
    if (response) {
      unreadMessageCount.value = response
    }
  } catch (error) {
    console.error('Failed to load unread message count:', error)
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadDocuments()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadDocuments()
}

const loadDocuments = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await api.get('/api/documents/public', { params })
    if (response && response.records) {
      documents.value = response.records
      total.value = response.total || 0
    } else {
      documents.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('Failed to load public documents:', error)
    documents.value = []
    total.value = 0
  }
}

const searchDocuments = async () => {
  const trimmedKeyword = searchKeyword.value.trim()
  if (!trimmedKeyword) {
    loadDocuments()
    return
  }
  
  try {
    const params = {
      keyword: trimmedKeyword,
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await api.get('/api/documents/public/search', { params })
    if (response && response.records) {
      documents.value = response.records
      total.value = response.total || 0
    } else {
      documents.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('Failed to search documents:', error)
    documents.value = []
    total.value = 0
  }
}

const viewDocument = (id) => {
  if (isAuthenticated.value) {
    router.push(`/document/${id}`)
  } else {
    router.push(`/public/${id}`)
  }
}

const getPreview = (content) => {
  if (!content) return ''
  return content.substring(0, 150) + (content.length > 150 ? '...' : '')
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadDocuments()
  loadUserStats()
  loadUnreadMessageCount()
})
</script>

<style scoped lang="scss">
.public-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 60px;
  height: auto;
  
  h1 {
    margin: 0;
    font-size: 24px;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap;
  }
  
  .user-profile {
    display: flex;
    align-items: center;
    gap: 15px;
    color: white;
    background: transparent;
    
    .user-avatar-link {
      text-decoration: none;
      color: inherit;
      background: transparent;
    }
    
    .user-stats {
      display: flex;
      gap: 20px;
      background: transparent;
      
      .stat-item {
        text-align: center;
        background: transparent;
        
        .stat-value {
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: white;
          background: transparent;
        }
        
        .stat-label {
          display: block;
          font-size: 12px;
          color: rgba(255, 255, 255, 0.8);
          background: transparent;
        }
      }
    }
    
    .user-menu {
      background: transparent;
      
      .user-menu-trigger {
        display: flex;
        align-items: center;
        cursor: pointer;
        color: white;
        background: transparent;
        
        .el-icon {
          font-size: 16px;
          background: transparent;
        }
      }
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    span {
      font-size: 14px;
    }
  }
}

.content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.toolbar {
  margin-bottom: 20px;
}

.document-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  flex: 1;
  min-height: 0;
}

.el-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  flex-shrink: 0;
}

.document-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  .doc-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 12px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .doc-content {
    font-size: 14px;
    line-height: 1.6;
    color: #666;
    margin: 0 0 16px 0;
    height: 60px;
    overflow: hidden;
  }
  
  .doc-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #999;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    
    .category {
      background: #f0f9eb;
      color: #67c23a;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

.no-documents {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

.el-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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
