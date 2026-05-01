<template>
  <div class="user-profile-container">
    <!-- 顶部导航栏 -->
    <div class="main-header">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="goBack" circle class="back-button">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h1 class="site-title">Markdown Editor</h1>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" class="user-dropdown" @command="handleCommand">
            <span v-if="authStore.user?.id" class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="username-with-badge">
                <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
                <el-badge v-if="unreadMessageCount > 0" is-dot class="username-badge" />
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <span v-else class="user-info">
              <el-avatar :size="32">U</el-avatar>
              <div class="username-with-badge">
                <span class="username">未登录</span>
                <el-badge v-if="unreadMessageCount > 0" is-dot class="username-badge" />
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
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

    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="container">
        <!-- 左侧内容区 -->
        <div class="content-left">
          <!-- 加载中 -->
          <div v-if="isLoading" class="loading-container">
            <el-skeleton :rows="10" animated />
          </div>
          
          <!-- 错误提示 -->
          <div v-else-if="error" class="error-container">
            <el-empty :description="error">
              <el-button type="primary" @click="goBack">返回</el-button>
            </el-empty>
          </div>
          
          <!-- 用户信息卡片 -->
          <div v-else class="user-info-card">
            <div class="user-header">
              <div class="user-avatar">
                <router-link :to="`/user/${userId}`" class="avatar-link" @click="handleProfileClick">
                  <el-avatar :size="80" :src="userInfo?.avatar">{{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) || 'U' }}</el-avatar>
                </router-link>
              </div>
              <div class="user-details">
                <router-link :to="`/user/${userId}`" class="username-link" @click="handleProfileClick">
                  <h2 class="username">{{ userInfo?.nickname || userInfo?.username || '未知用户' }}</h2>
                </router-link>
                <div class="user-stats-container">
                  <p class="user-stats">
                    <span class="stat-item">{{ userStats?.original || 0 }} 文章</span>
                    <span class="stat-item">{{ userStats?.likes || 0 }} 获赞</span>
                    <span class="stat-item">{{ userStats?.collections || 0 }} 收藏</span>
                  </p>
                  <p class="user-follow-stats">
                    <span class="stat-item">{{ followStats?.following || 0 }} 关注</span>
                    <span class="stat-item">{{ followStats?.followers || 0 }} 粉丝</span>
                    <span class="stat-item" v-if="userInfo?.createdAt">加入于 {{ formatDate(userInfo.createdAt) }}</span>
                  </p>
                  <div class="user-actions">
                    <el-button
                      v-if="authStore.isAuthenticated && authStore.user?.id !== userId"
                      :type="isFollowing ? 'default' : 'primary'"
                      @click="handleFollow"
                      class="follow-button"
                    >
                      {{ isFollowing ? '取消关注' : '关注' }}
                    </el-button>
                    <el-button
                      v-if="authStore.isAuthenticated && authStore.user?.id !== userId"
                      type="primary"
                      @click="handleMessage"
                      class="message-button"
                    >
                      私信
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 文章标签页 -->
          <el-card class="articles-card">
            <template #header>
              <div class="card-header">
                <div class="tabs">
                  <div 
                      v-for="tab in tabs" 
                      :key="tab.name"
                      class="tab-item"
                      :class="{ active: activeTab === tab.name }"
                      @click="handleTabChange(tab)"
                    >
                      {{ tab.label }}<span v-if="tab.count > 0" class="tab-count">({{ tab.count }})</span>
                    </div>
                </div>
              </div>
            </template>
            
            <!-- 文章标签页内容 -->
            <div v-if="activeTab === 'articles'" class="tab-content">
              <div class="articles-list">
                <div v-if="articles.length > 0">
                  <div v-for="article in recentArticles" :key="article.id" class="article-item">
                    <a :href="`/document/${article.id}`" class="article-title">{{ article.title }}</a>
                    <div class="article-meta">
                      <span class="meta-item">{{ formatDate(article.updatedAt) }}</span>
                      <el-tag :type="getPermissionType(article.permission)" size="small">{{ getPermissionText(article.permission) }}</el-tag>
                      <span class="meta-item">{{ article.viewCount || 0 }} 浏览</span>
                      <span class="meta-item">{{ article.likeCount || 0 }} 点赞</span>
                    </div>
                  </div>
                </div>
                <div v-else class="no-articles">
                  暂无文章
                </div>
              </div>
              
              <!-- 分页组件 -->
              <div class="pagination-container" v-if="total > 0">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="total"
                  @size-change="handlePageSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
            
            <!-- 分类标签页内容 -->
            <div v-else class="tab-content">
              <div class="articles-list">
                <div v-if="getCategoryArticles(tabs.find(tab => tab.name === activeTab)?.category).length > 0">
                  <div v-for="article in getCategoryArticles(tabs.find(tab => tab.name === activeTab)?.category)" :key="article.id" class="article-item">
                    <a :href="`/document/${article.id}`" class="article-title">{{ article.title }}</a>
                    <div class="article-meta">
                      <span class="meta-item">{{ formatDate(article.updatedAt) }}</span>
                      <el-tag :type="getPermissionType(article.permission)" size="small">{{ getPermissionText(article.permission) }}</el-tag>
                      <span class="meta-item">{{ article.viewCount || 0 }} 浏览</span>
                      <span class="meta-item">{{ article.likeCount || 0 }} 点赞</span>
                    </div>
                  </div>
                </div>
                <div v-else class="no-articles">
                  暂无该分类的文章
                </div>
              </div>
              
              <!-- 分类文章分页组件 -->
              <div class="pagination-container" v-if="(categoryTotal[tabs.find(tab => tab.name === activeTab)?.category || ''] || 0) > 0">
                <el-pagination
                  v-model:current-page="categoryCurrentPage[tabs.find(tab => tab.name === activeTab)?.category || '']"
                  v-model:page-size="pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="categoryTotal[tabs.find(tab => tab.name === activeTab)?.category || ''] || 0"
                  @size-change="(size) => handleCategoryPageSizeChange(tabs.find(tab => tab.name === activeTab)?.category, size)"
                  @current-change="(current) => handleCategoryCurrentChange(tabs.find(tab => tab.name === activeTab)?.category, current)"
                />
              </div>
            </div>
          </el-card>
        </div>

        <!-- 右侧边栏 -->
        <div class="content-right">
          <!-- 个人成就卡片 -->
          <el-card class="achievement-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">个人成就</h3>
              </div>
            </template>
            <div class="achievement-content">
              <div class="achievement-item">
                <el-icon class="achievement-icon"><Star /></el-icon>
                <span class="achievement-label">获赞</span>
                <span class="achievement-value">{{ userStats.likes || 0 }}</span>
              </div>
              <div class="achievement-item">
                <el-icon class="achievement-icon"><Collection /></el-icon>
                <span class="achievement-label">收藏</span>
                <span class="achievement-value">{{ userStats.collections || 0 }}</span>
              </div>
              <div class="achievement-item">
                <el-icon class="achievement-icon"><ChatLineSquare /></el-icon>
                <span class="achievement-label">评论</span>
                <span class="achievement-value">{{ userStats.comments || 0 }}</span>
              </div>
              <div class="achievement-item">
                <el-icon class="achievement-icon"><Trophy /></el-icon>
                <span class="achievement-label">博客排名</span>
                <span class="achievement-value">{{ userStats.blogRank || 0 }}</span>
              </div>
              <div class="achievement-item">
                <el-icon class="achievement-icon"><Clock /></el-icon>
                <span class="achievement-label">连续创作</span>
                <span class="achievement-value">{{ userStats.continuousDays || 0 }} 天</span>
              </div>
            </div>
          </el-card>

          <!-- 热门标签 -->
          <el-card class="tags-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">热门标签</h3>
              </div>
            </template>
            <div class="tags-content">
              <el-tag v-for="tag in hotTags" :key="tag" size="small" class="tag-item">
                {{ tag }}
              </el-tag>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api, { avatarApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Star, Collection, View, Clock, ChatLineSquare, Trophy } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const userId = Number(route.params.id)

const userInfo = ref({})
const articles = ref([])
const recentArticles = ref([])
const collections = ref([])
const followStats = ref({
  following: 0,
  followers: 0
})

const userStats = reactive({
  userId: null,
  username: '',
  nickname: '',
  original: 0,
  likes: 0,
  collections: 0,
  comments: 0,
  blogRank: 0,
  continuousDays: 0
})
const isFollowing = ref(false)
const isLoading = ref(true)
const error = ref('')
const activeTab = ref('articles')
const tabs = ref([
  { name: 'articles', label: '文章', category: '' }
])
const categoryArticles = ref({})
const isLoadingCategory = ref(false)
const hotTags = ref(['前端', '后端', 'JavaScript', 'Python', 'Java', 'React', 'Vue', 'Node.js'])
const unreadMessageCount = ref(0)

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const categoryCurrentPage = ref({})
const categoryTotal = ref({})



const loadUserInfo = async () => {
  try {
    // 尝试从用户统计数据中获取用户基本信息
    if (userStats.userId) {
      userInfo.value = {
        id: userStats.userId,
        username: userStats.username,
        nickname: userStats.nickname,
        avatar: userStats.avatar
      }
    } else {
      // 如果用户统计数据不存在，再发送请求
      try {
        const response = await api.get(`/api/users/${userId}`)
        // 检查响应是否是ApiResponse格式
        const userData = response.data ? response.data : response
        if (userData) {
          userInfo.value = {
            id: userData.id,
            username: userData.username,
            nickname: userData.nickname,
            avatar: userData.avatar
          }
        }
      } catch (error) {
        // 如果获取用户信息失败，尝试从统计数据中获取
        const statsResponse = await api.get(`/api/users/${userId}/stats`)
        // 检查响应是否是ApiResponse格式
        const statsData = statsResponse.data ? statsResponse.data : statsResponse
        if (statsData) {
          userInfo.value = {
            id: statsData.userId,
            username: statsData.username,
            nickname: statsData.nickname,
            avatar: statsData.avatar
          }
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadUserStats = async () => {
  try {
    // 使用 api 实例来获取用户统计信息，添加随机参数以避免缓存
    const response = await api.get(`/api/users/${userId}/stats?timestamp=${Date.now()}`)
    // 检查响应是否是ApiResponse格式
    const data = response.data ? response.data : response
    if (data) {
      // 直接修改 userStats 对象的属性
      userStats.userId = data.userId
      userStats.username = data.username
      userStats.nickname = data.nickname
      userStats.avatar = data.avatar
      userStats.original = data.original || 0
      userStats.likes = data.likes || 0
      userStats.collections = data.collections || 0
      userStats.comments = data.comments || 0
      userStats.blogRank = data.blogRank || 0
      userStats.continuousDays = data.continuousDays || 0
      // 同时更新用户信息
      userInfo.value = {
        id: data.userId,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar
      }
      
      // 更新分类标签的文章数量
      const articleTabIndex = tabs.value.findIndex(tab => tab.name === 'articles')
      if (articleTabIndex !== -1) {
        tabs.value[articleTabIndex].count = data.original || 0
      }
    }
  } catch (error) {
    console.error('加载用户统计信息失败:', error)
  }
}

const loadArticles = async (page = 1, size = 10) => {
  try {
    const response = await api.get(`/api/documents/author/${userId}/all?page=${page}&size=${size}`)
    // 提取所有文章
    const allArticles = []
    if (response && response.records) {
      response.records.forEach(item => {
        // 直接使用item作为文章数据
        allArticles.push(item)
      })
    }
    
    // 按更新时间排序
    allArticles.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    
    articles.value = allArticles
    recentArticles.value = allArticles
    const articleTotal = response && response.total ? response.total : 0
    total.value = articleTotal
    
    // 更新分类标签的文章数量
    const articleTabIndex = tabs.value.findIndex(tab => tab.name === 'articles')
    if (articleTabIndex !== -1) {
      // 优先使用userStats.original，如果没有则使用文章总数
      tabs.value[articleTabIndex].count = userStats.original || articleTotal || 0
    }
  } catch (error) {
    console.error('加载文章列表失败:', error)
  }
}

const loadCollections = async () => {
  try {
    const response = await api.get(`/api/users/${userId}/collections`)
    // 按照更新时间排序，最近的收藏排在前面
    const sortedCollections = Array.isArray(response) ? response.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)) : []
    collections.value = sortedCollections
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    collections.value = []
  }
}

const loadCategoryArticles = async (category, page = 1, size = 10) => {
  if (!category) return
  
  isLoadingCategory.value = true
  try {
    const response = await api.get(`/api/documents/author/${userId}/category/${category}?page=${page}&size=${size}`)
    const total = response && response.total ? response.total : 0
    categoryArticles.value[category] = {
      records: response && response.records ? response.records : [],
      total: total
    }
    categoryTotal.value[category] = total
    
    // 更新分类标签的文章数量
    const tabIndex = tabs.value.findIndex(tab => tab.category === category)
    if (tabIndex !== -1) {
      tabs.value[tabIndex].count = total
    }
  } catch (error) {
    console.error(`加载${category}分类文章失败:`, error)
    categoryArticles.value[category] = {
      records: [],
      total: 0
    }
    categoryTotal.value[category] = 0
  } finally {
    isLoadingCategory.value = false
  }
}

const loadFollowStats = async () => {
  try {
    const response = await api.get(`/api/users/${userId}/follow/count`)
    if (response) {
      followStats.value = response
    }
  } catch (error) {
    console.error('加载关注统计失败:', error)
  }
}

const checkFollowStatus = async () => {
  if (!authStore.isAuthenticated) {
    return
  }
  try {
    const response = await api.get(`/api/users/${userId}/follow/status`)
    isFollowing.value = response
  } catch (error) {
    console.error('检查关注状态失败:', error)
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

const getCategoryArticles = (category) => {
  // 使用后端返回的分类文章数据
  return categoryArticles.value[category] ? categoryArticles.value[category].records : []
}

const handleTabChange = async (tab) => {
  if (tab && tab.name) {
    activeTab.value = tab.name
    if (tab.category) {
      // 重置分类分页状态
      categoryCurrentPage.value[tab.category] = 1
      await loadCategoryArticles(tab.category, 1, pageSize.value)
    } else {
      // 重置文章分页状态
      currentPage.value = 1
      await loadArticles(1, pageSize.value)
    }
  }
}

const goBack = () => {
  router.push('/documents')
}

const handleCommand = (command) => {
  switch (command) {
    case 'documents':
      router.push('/documents')
      break
    case 'public':
      router.push('/public')
      break
    case 'messages':
      router.push('/messages')
      break
    case 'content-management':
      router.push('/content-management')
      break
    case 'logout':
      authStore.logout()
      router.push('/login')
      break
    default:
      break
  }
}

const handleFollow = async () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录后再关注，是否前往登录页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
    })
    return
  }

  try {
    if (isFollowing.value) {
      const response = await api.delete(`/api/users/${userId}/follow`)
      isFollowing.value = false
      followStats.value.followers--
      ElMessage.success('取消关注成功')
    } else {
      const response = await api.post(`/api/users/${userId}/follow`)
      isFollowing.value = true
      followStats.value.followers++
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  }
}

const handleMessage = async () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录后再发送私信，是否前往登录页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
    })
    return
  }

  try {
    // 跳转到消息中心的聊天界面，打开和该用户的聊天框
    router.push({
      path: '/messages',
      query: {
        tab: 'chat',
        userId: userId
      }
    })
  } catch (error) {
    console.error('发送私信失败:', error)
    ElMessage.error('操作失败')
  }
}

// 处理个人主页点击事件
const handleProfileClick = (event) => {
  // 阻止默认导航行为
  event.preventDefault()
  // 刷新页面
  window.location.reload()
}

// 分页处理函数
const handlePageSizeChange = async (size) => {
  pageSize.value = size
  await loadArticles(1, size)
}

const handleCurrentChange = async (current) => {
  currentPage.value = current
  await loadArticles(current, pageSize.value)
}

const getCategoryCurrentPage = (category) => {
  return categoryCurrentPage.value[category] || 1
}

const getCategoryTotal = (category) => {
  return categoryTotal.value[category] || 0
}

const handleCategoryPageSizeChange = async (category, size) => {
  pageSize.value = size
  categoryCurrentPage.value[category] = 1
  await loadCategoryArticles(category, 1, size)
}

const handleCategoryCurrentChange = async (category, current) => {
  categoryCurrentPage.value[category] = current
  await loadCategoryArticles(category, current, pageSize.value)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getPermissionType = (permission) => {
  switch (permission) {
    case 'public':
      return 'success'
    case 'private':
      return 'info'
    case 'secret':
      return 'warning'
    default:
      return 'default'
  }
}

const getPermissionText = (permission) => {
  switch (permission) {
    case 'public':
      return '公开'
    case 'private':
      return '私有'
    case 'secret':
      return '绝密'
    default:
      return '未知'
  }
}

// 请求缓存，避免重复请求
const requestCache = {
  categories: null,
  articles: null
}

const loadCategories = async () => {
  try {
    // 如果已经缓存了分类数据，直接使用
    if (requestCache.categories) {
      const categories = requestCache.categories
      updateTabsWithCategories(categories)
      return
    }
    
    // 尝试从后端获取分类列表
    const response = await api.get(`/api/documents/categories/list`)
    const categories = Array.isArray(response) ? response : []
    requestCache.categories = categories
    
    updateTabsWithCategories(categories)
    
    // 异步加载所有分类的文章数量，不阻塞主流程
    loadCategoryCountsAsync(categories)
  } catch (error) {
    console.error('加载分类列表失败:', error)
    // 如果获取分类列表失败，使用默认分类
    tabs.value = [
      { name: 'articles', label: '文章', category: '', count: articles.value.length }
    ]
  }
}

const updateTabsWithCategories = (categories) => {
  // 保留默认标签（文章）
  const defaultTabs = [
    { name: 'articles', label: '文章', category: '', count: userStats.original || total.value || 0 }
  ]
  
  // 清空现有分类标签，保留默认标签
  tabs.value = [...defaultTabs]
  
  // 添加从字典表获取的分类标签
  if (Array.isArray(categories)) {
    categories.forEach(category => {
      if (category && category.dictCode && category.dictLabel) {
        const newTab = {
          name: category.dictCode,
          label: category.dictLabel,
          category: category.dictCode,
          count: 0
        }
        tabs.value.push(newTab)
        categoryTotal.value[category.dictCode] = 0
        categoryArticles.value[category.dictCode] = {
          records: [],
          total: 0
        }
      }
    })
  }
}

const loadCategoryCountsAsync = async (categories) => {
  if (!Array.isArray(categories) || categories.length === 0) return
  
  // 限制并发请求数量，避免过多请求
  const maxConcurrent = 3
  const categoryPromises = []
  
  for (let i = 0; i < categories.length; i += maxConcurrent) {
    const batch = categories.slice(i, i + maxConcurrent)
    const batchPromises = batch.map(category => {
      if (category && category.dictCode && category.dictLabel) {
        return loadSingleCategoryCount(category.dictCode, category.dictLabel)
      }
      return Promise.resolve()
    })
    categoryPromises.push(Promise.all(batchPromises))
  }
  
  // 启动异步加载，但不等待完成
  Promise.all(categoryPromises).catch(error => {
    console.error('加载分类文章数量失败:', error)
  })
}

const loadSingleCategoryCount = async (categoryCode, categoryLabel) => {
  try {
    const categoryResponse = await api.get(`/api/documents/author/${userId}/category/${categoryCode}?page=1&size=1`)
    const count = categoryResponse && categoryResponse.total ? categoryResponse.total : 0
    
    const tabIndex = tabs.value.findIndex(tab => tab.category === categoryCode)
    if (tabIndex !== -1) {
      tabs.value[tabIndex].count = count
    }
    categoryTotal.value[categoryCode] = count
    if (categoryArticles.value[categoryCode]) {
      categoryArticles.value[categoryCode].total = count
    }
  } catch (error) {
    console.error(`加载${categoryLabel}分类文章数量失败:`, error)
  }
}

onMounted(async () => {
  // 检查 userId 是否为有效的数字
  if (isNaN(userId) || userId <= 0) {
    error.value = '用户 ID 无效'
    isLoading.value = false
    return
  }
  
  // 记录开始时间
  const startTime = performance.now()
  
  try {
    // 第一步：并行加载关键数据
    const keyDataPromise = Promise.all([
      loadUserStats(),
      loadArticles(1, pageSize.value)
    ])
    
    // 第二步：同时启动分类加载（不等待）
    const categoriesPromise = loadCategories()
    
    // 等待关键数据加载完成
    await keyDataPromise
    
    // 关键数据加载完成后，立即显示页面
    isLoading.value = false
    
    // 第三步：在后台加载次要数据
    Promise.all([
      categoriesPromise,
      loadCollections(),
      loadFollowStats(),
      authStore.isAuthenticated ? loadUnreadMessageCount() : Promise.resolve(),
      authStore.isAuthenticated ? checkFollowStatus() : Promise.resolve()
    ]).catch(err => {
      console.error('加载次要数据失败:', err)
    })
    
    const endTime = performance.now()
    console.log(`用户主页关键数据加载完成，耗时: ${(endTime - startTime).toFixed(2)}ms`)
  } catch (err) {
    console.error('加载用户信息失败:', err)
    isLoading.value = false
  }
})
</script>

<style scoped lang="scss">
/* 全局样式 */
.user-profile-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

/* 顶部导航栏 */
.main-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 64px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;

      .back-button {
        margin-right: 16px;
      }

      .site-title {
        font-size: 20px;
        font-weight: 600;
        margin: 0;
        color: #1677ff;
      }
    }

    .header-right {
      .user-dropdown {
        .user-info {
          display: flex;
          align-items: center;
          text-decoration: none;
          color: #333;

          .username {
            margin: 0 8px;
          }

          .dropdown-icon {
            font-size: 12px;
          }
        }
      }
    }
  }
}

/* 主内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  justify-content: center;

  .container {
    width: 100%;
    display: flex;
    gap: 20px;

    .content-left {
      flex: 1;
      min-width: 0;

      /* 加载中 */
      .loading-container {
        margin-bottom: 20px;
      }

      /* 错误提示 */
      .error-container {
        margin-bottom: 20px;
      }

      /* 用户信息卡片 */
      .user-info-card {
        background-color: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        padding: 20px;
        margin-bottom: 20px;

        .user-header {
          display: flex;
          align-items: center;

          .user-avatar {
            margin-right: 20px;

            .avatar-link {
              display: inline-block;
              transition: transform 0.3s ease;

              &:hover {
                transform: scale(1.05);
              }
            }
          }

          .user-details {
            flex: 1;

            .username-link {
              text-decoration: none;
              color: inherit;
              cursor: pointer;

              &:hover {
                color: #1677ff;
              }
            }

            .username {
              font-size: 24px;
              font-weight: 600;
              margin: 0 0 8px 0;
            }

            .user-stats-container {
              display: flex;
              flex-wrap: wrap;
              align-items: center;
              gap: 20px;
              margin: 0 0 16px 0;
            }

            .user-stats {
              margin: 0;
              color: #666;

              .stat-item {
                margin-right: 16px;
              }
            }

            .user-follow-stats {
              margin: 0;
              color: #666;

              .stat-item {
                margin-right: 16px;
              }
            }

            .user-actions {
              margin-left: auto;

              .follow-button {
                margin-right: 12px;
              }
            }
          }
        }
      }

      /* 文章标签页 */
      .articles-card {
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        overflow: hidden;

        .card-header {
          padding: 0;
          border-bottom: 1px solid #e4e7ed;

          .tabs {
            display: flex;
            border-bottom: 1px solid #e4e7ed;

            .tab-item {
            padding: 12px 16px;
            cursor: pointer;
            font-size: 14px;
            color: #666;
            border-bottom: 2px solid transparent;

            &:hover {
              color: #1677ff;
            }

            &.active {
              color: #1677ff;
              border-bottom-color: #1677ff;
            }

            .tab-count {
              margin-left: 4px;
              font-size: 12px;
              color: #999;
            }

            &:hover .tab-count {
              color: #1677ff;
            }

            &.active .tab-count {
              color: #1677ff;
            }
          }
          }
        }

        .tab-content {
          padding: 20px;

          .section {
            margin-bottom: 24px;

            .section-title {
              font-size: 16px;
              font-weight: 600;
              margin: 0 0 16px 0;
              color: #333;
            }
          }

          .articles-list {
            .article-item {
              padding: 16px 0;
              border-bottom: 1px solid #f0f2f5;

              &:last-child {
                border-bottom: none;
              }

              .article-title {
                font-size: 16px;
                font-weight: 500;
                color: #333;
                text-decoration: none;
                margin-bottom: 8px;
                display: block;

                &:hover {
                  color: #1677ff;
                }
              }

              .article-meta {
                display: flex;
                align-items: center;
                font-size: 12px;
                color: #999;

                .meta-item {
                  margin-right: 16px;
                }

                .el-tag {
                  margin-right: 16px;
                }
              }
            }

            .no-articles {
              padding: 40px 0;
              text-align: center;
              color: #999;
            }
            
            .pagination-container {
              margin-top: 20px;
              display: flex;
              justify-content: flex-end;
            }
          }
        }
      }
    }

    .content-right {
      width: 280px;
      flex-shrink: 0;

      /* 个人成就卡片 */
      .achievement-card {
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        .card-header {
          .card-title {
            font-size: 14px;
            font-weight: 600;
            margin: 0;
          }
        }

        .achievement-content {
          .achievement-item {
            display: flex;
            align-items: center;
            margin-bottom: 12px;

            &:last-child {
              margin-bottom: 0;
            }

            .achievement-icon {
              font-size: 16px;
              margin-right: 8px;
              color: #1677ff;
            }

            .achievement-label {
              flex: 1;
              font-size: 14px;
              color: #666;
            }

            .achievement-value {
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
          }
        }
      }

      /* 热门标签 */
      .tags-card {
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        .card-header {
          .card-title {
            font-size: 14px;
            font-weight: 600;
            margin: 0;
          }
        }

        .tags-content {
          .tag-item {
            margin: 4px;
            cursor: pointer;
          }
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;

    .container {
      flex-direction: column;

      .content-left {
        order: 2;
      }

      .content-right {
        order: 1;
        width: 100%;
      }
    }
  }
}
</style>