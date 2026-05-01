<template>
  <div class="public-document-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <el-button @click="goBack" circle>
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h1>Markdown Editor</h1>
          <div class="header-actions">
            <template v-if="isAuthenticated">
              <el-dropdown>
                <span class="user-info">
                  {{ user?.nickname || user?.username }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="goToDocuments">我的文档</el-dropdown-item>
                    <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
      </el-header>

      <el-main>
        <div v-if="isLoading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
        <div v-else>
          <div class="author-info">
            <router-link 
              v-if="authorInfo.id" 
              :to="authorInfo.id ? `/user/${authorInfo.id}` : ''" 
              class="author-avatar-link"
              @click.prevent="authorInfo.id ? undefined : () => {}"
            >
              <el-avatar :size="48">{{ authorInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            </router-link>
            <el-avatar v-else :size="48">{{ authorInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="author-details">
              <router-link 
                v-if="authorInfo.id" 
                :to="authorInfo.id ? `/user/${authorInfo.id}` : ''" 
                class="author-name-link"
                @click.prevent="authorInfo.id ? undefined : () => {}"
              >
                <div class="author-name">{{ authorInfo.nickname || authorInfo.username }}</div>
              </router-link>
              <div v-else class="author-name">{{ authorInfo.nickname || authorInfo.username }}</div>
              <div class="author-stats">
                <span class="stat-item">{{ authorInfo.original || 0 }} 原创</span>
                <span class="stat-item">{{ authorInfo.likes || 0 }} 获赞</span>
                <span class="stat-item">{{ authorInfo.collections || 0 }} 收藏</span>
              </div>
            </div>
            <div v-if="isAuthenticated && user?.id !== authorInfo.id" class="author-actions">
              <el-button
                :type="isFollowing ? 'default' : 'primary'"
                @click="handleFollow"
                class="follow-button"
              >
                {{ isFollowing ? '取消关注' : '关注' }}
              </el-button>
            </div>
          </div>

          <div class="document-info">
            <el-tag v-if="document.category">{{ document.category }}</el-tag>
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ document.viewCount }} 次浏览
            </span>
            <span class="update-time">更新于 {{ formatDate(document.updatedAt) }}</span>
          </div>

          <div class="action-buttons">
            <el-button @click="handleLike" class="action-button like-button">
              <el-icon><Star /></el-icon>
              {{ isLiked ? '取消点赞' : '点赞' }} ({{ document.likeCount || 0 }})
            </el-button>
            <el-button @click="handleCollect" class="action-button collect-button">
              <el-icon><Collection /></el-icon>
              收藏
            </el-button>
          </div>

          <div class="document-content">
            <div class="markdown-body" v-html="renderedContent"></div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import api from '@/utils/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isLoading = ref(true)
const document = ref({
  id: null,
  title: '',
  content: '',
  category: '',
  viewCount: 0,
  likeCount: 0,
  userId: null,
  updatedAt: null
})
const authorInfo = ref({
  id: null,
  username: '',
  nickname: '',
  original: 0,
  likes: 0,
  collections: 0
})

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const isFollowing = ref(false)
const isLiked = ref(false)

const goToDocuments = () => {
  router.push('/documents')
}

const logout = () => {
  authStore.logout()
  router.push('/public')
}

const renderer = new marked.Renderer()
renderer.code = (code, language) => {
  if (language && hljs.getLanguage(language)) {
    try {
      return `<pre><code class="hljs language-${language}">${hljs.highlight(code, { language }).value}</code></pre>`
    } catch (err) {
      console.error(err)
    }
  }
  return `<pre><code class="hljs">${DOMPurify.sanitize(code)}</code></pre>`
}

marked.setOptions({
  renderer,
  breaks: true,
  gfm: true
})

const renderedContent = computed(() => {
  if (!document.value.content) return ''
  const rawHtml = marked(document.value.content)
  return DOMPurify.sanitize(rawHtml)
})

const loadDocument = async () => {
  const id = route.params.id
  if (!id) return
  
  isLoading.value = true
  
  try {
    const response = await api.get(`/api/documents/public/${id}`)
    document.value = response
    
    // 加载作者信息
    if (response.userId) {
      loadAuthorInfo(response.userId)
    }
    
    // 检查用户是否已经点赞了该文档
    if (isAuthenticated.value) {
      try {
        const likedResponse = await api.get(`/api/documents/${id}/like/status`)
        console.log('获取点赞状态响应:', likedResponse)
        // 直接使用 likedResponse 作为点赞状态
        isLiked.value = likedResponse
        console.log('isLiked.value:', isLiked.value)
      } catch (error) {
        console.error('检查点赞状态失败:', error)
        isLiked.value = false
        console.log('isLiked.value after error:', isLiked.value)
      }
    } else {
      // 未登录用户默认未点赞
      isLiked.value = false
      console.log('未登录用户，isLiked.value:', isLiked.value)
    }
  } catch (error) {
    // 403错误时不跳转，让ElMessage显示错误信息
    if (error.response?.status !== 403) {
      ElMessage.error('加载文档失败')
      router.push('/public')
    }
  } finally {
    isLoading.value = false
  }
}

const loadAuthorInfo = async (userId) => {
  try {
    const response = await api.get(`/api/users/${userId}/stats`)
    // 将 userId 映射到 id，确保前端代码可以统一使用 authorInfo.value.id
    authorInfo.value = {
      ...response,
      id: response.userId || response.id || userId
    }
    // 检查关注状态
    await checkFollowStatus()
  } catch (error) {
    console.error('加载作者信息失败:', error)
    // 加载失败时使用默认数据
    authorInfo.value = {
      id: userId,
      username: '用户',
      nickname: '用户',
      original: 0,
      likes: 0,
      collections: 0
    }
  }
}

const handleLike = async () => {
  if (!isAuthenticated.value) {
    ElMessageBox.confirm('请先登录后再点赞，是否前往登录页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消登录
    })
    return
  }
  
  console.log('handleLike 被调用，isLiked.value:', isLiked.value)
  
  try {
    if (isLiked.value) {
      // 取消点赞
      console.log('执行取消点赞操作')
      const response = await api.delete(`/api/documents/${document.value.id}/like`)
      console.log('取消点赞响应:', response)
      ElMessage.success('取消点赞成功')
      document.value.likeCount = response
      await nextTick()
      isLiked.value = false
      console.log('取消点赞成功，isLiked.value:', isLiked.value)
    } else {
      // 点赞
      console.log('执行点赞操作')
      const response = await api.post(`/api/documents/${document.value.id}/like`)
      console.log('点赞响应:', response)
      ElMessage.success('点赞成功')
      document.value.likeCount = response
      await nextTick()
      isLiked.value = true
      console.log('点赞成功，isLiked.value:', isLiked.value)
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
    console.error('操作失败:', error)
    // 重新获取点赞状态
    if (isAuthenticated.value) {
      try {
        const likedResponse = await api.get(`/api/documents/${document.value.id}/like/status`)
        console.log('重新获取点赞状态响应:', likedResponse)
        isLiked.value = likedResponse
        console.log('重新获取点赞状态成功，isLiked.value:', isLiked.value)
      } catch (error) {
        console.error('重新获取点赞状态失败:', error)
      }
    }
  }
}

const handleCollect = async () => {
  if (!isAuthenticated.value) {
    ElMessageBox.confirm('请先登录后再收藏，是否前往登录页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消登录
    })
    return
  }
  
  try {
    const response = await api.post(`/api/documents/${document.value.id}/collect`)
    if (response.success) {
      ElMessage.success('收藏成功')
    } else {
      if (response.message === '已经收藏过该文档') {
        ElMessage.info('已经收藏过该文档')
      } else {
        ElMessage.error(response.message || '收藏失败')
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '收藏失败')
  }
}

const checkFollowStatus = async () => {
  if (!isAuthenticated.value || !authorInfo.value.id) return
  
  try {
    const response = await api.get(`/api/users/${authorInfo.value.id}/follow/status`)
    isFollowing.value = Boolean(response.data)
  } catch (error) {
    console.error('检查关注状态失败:', error)
    isFollowing.value = false
  }
}

const handleFollow = async () => {
  if (!isAuthenticated.value) {
    ElMessageBox.confirm('请先登录后再关注，是否前往登录页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消登录
    })
    return
  }
  
  try {
    if (isFollowing.value) {
      const response = await api.delete(`/api/users/${authorInfo.value.id}/follow`)
      if (response.success) {
        isFollowing.value = false
        ElMessage.success('取消关注成功')
      } else {
        ElMessage.error(response.message || '取消关注失败')
      }
    } else {
      const response = await api.post(`/api/users/${authorInfo.value.id}/follow`)
      if (response.success) {
        isFollowing.value = true
        ElMessage.success('关注成功')
      } else {
        ElMessage.error(response.message || '关注失败')
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const goBack = () => {
  router.push('/public')
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadDocument()
})

// 监听路由参数变化，重新加载文档信息和点赞状态
watch(
  () => route.params.id,
  () => {
    loadDocument()
  }
)
</script>

<style scoped lang="scss">
.public-document-container {
  min-height: 100vh;
  
  .el-container {
    min-height: 100vh;
  }
}

.el-header {
  background: #1677ff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  
  .header-content {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
    
    h1 {
      flex: 1;
      margin: 0;
      font-size: 24px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.el-main {
  background: #f5f5f5;
  padding: 20px;
}

.author-info {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  
  .author-avatar-link {
    text-decoration: none;
    color: inherit;
  }
  
  .author-details {
    flex: 1;
    
    .author-name-link {
      text-decoration: none;
      color: inherit;
      
      &:hover {
        color: #667eea;
      }
    }
    
    .author-name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 5px;
    }
    
    .author-stats {
      display: flex;
      gap: 15px;
      font-size: 14px;
      color: #666;
      
      .stat-item {
        display: inline-block;
      }
    }
  }
  
  .author-actions {
    .follow-button {
      border-radius: 6px;
      font-size: 14px;
    }
  }
}

.document-info {
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #666;
  
  .view-count {
    display: flex;
    align-items: center;
    gap: 5px;
  }
}

.action-buttons {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  
  .action-button {
    border-radius: 6px;
    font-size: 14px;
    
    &.like-button {
      &:hover {
        background: #fff7e6;
        border-color: #ffd591;
      }
    }
    
    &.collect-button {
      &:hover {
        background: #e6f7ff;
        border-color: #91d5ff;
      }
    }
  }
}

.document-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  max-width: 900px;
  margin: 0 auto;
}

.markdown-body {
  font-size: 16px;
  line-height: 1.8;
  
  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
  }
  
  :deep(h1) { font-size: 2em; border-bottom: 1px solid #eaecef; padding-bottom: .3em; }
  :deep(h2) { font-size: 1.5em; border-bottom: 1px solid #eaecef; padding-bottom: .3em; }
  :deep(h3) { font-size: 1.25em; }
  :deep(h4) { font-size: 1em; }
  
  :deep(p) { margin-bottom: 16px; }
  
  :deep(code) {
    padding: .2em .4em;
    margin: 0;
    font-size: 85%;
    background-color: rgba(27, 31, 35, .05);
    border-radius: 3px;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  }
  
  :deep(pre) {
    padding: 16px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
    background-color: #f6f8fa;
    border-radius: 6px;
    
    code {
      background: none;
      padding: 0;
    }
  }
  
  :deep(blockquote) {
    padding: 0 1em;
    color: #6a737d;
    border-left: .25em solid #dfe2e5;
    margin: 0 0 16px 0;
  }
  
  :deep(table) {
    border-spacing: 0;
    border-collapse: collapse;
    margin-bottom: 16px;
    width: 100%;
    
    th, td {
      padding: 6px 13px;
      border: 1px solid #dfe2e5;
    }
    
    th {
      font-weight: 600;
      background: #f6f8fa;
    }
    
    tr {
      background-color: #fff;
      border-top: 1px solid #c6cbd1;
      
      &:nth-child(2n) {
        background-color: #f6f8fa;
      }
    }
  }
  
  :deep(img) {
    max-width: 100%;
    box-sizing: content-box;
    background-color: #fff;
  }
  
  :deep(a) {
    color: #0366d6;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 2em;
    margin-bottom: 16px;
  }
  
  :deep(hr) {
    height: .25em;
    padding: 0;
    margin: 24px 0;
    background-color: #e1e4e8;
    border: 0;
  }
}
</style>
