<template>
  <div class="document-view-container">
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
              <el-avatar :size="32">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="username-with-badge">
                <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
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
          <!-- 文档标题 -->
          <h1 class="document-title">{{ document.title }}</h1>

          <!-- 作者信息 -->
          <div class="author-info">
            <router-link 
              v-if="authorInfo.id" 
              :to="`/user/${authorInfo.id}`" 
              class="author-avatar-link"
            >
              <el-avatar :size="48" :src="authorInfo.avatar">{{ authorInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            </router-link>
            <el-avatar v-else :size="48">{{ authorInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="author-details">
              <router-link 
                v-if="authorInfo.id" 
                :to="`/user/${authorInfo.id}`" 
                class="author-name"
              >{{ authorInfo.nickname || authorInfo.username }}</router-link>
              <span v-else class="author-name">{{ authorInfo.nickname || authorInfo.username }}</span>
              <div class="author-stats">
                <span class="stat-item">{{ authorInfo.original || 0 }} 原创</span>
                <span class="stat-item">{{ authorInfo.likes || 0 }} 点赞</span>
                <span class="stat-item">{{ authorInfo.collections || 0 }} 收藏</span>
                <span class="stat-item">{{ authorInfo.followers || 0 }} 粉丝</span>
                <span class="stat-item">{{ authorInfo.following || 0 }} 关注</span>
              </div>
            </div>
            <div class="author-actions">
              <el-button
                type="primary"
                @click="editDocument"
                class="edit-button"
                v-if="authStore.user?.id === authorInfo.id"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                :type="isFollowing ? 'default' : 'primary'"
                @click="handleFollow"
                class="follow-button"
                v-if="authStore.isAuthenticated && authStore.user?.id !== authorInfo.id"
              >
                {{ isFollowing ? '取消关注' : '关注' }}
              </el-button>
            </div>
          </div>

          <!-- 文档元信息 -->
          <div class="document-meta">
            <span class="meta-item">
              <el-icon class="meta-icon"><Timer /></el-icon>
              {{ formatDate(document.updatedAt) }}
            </span>
            <span class="meta-item">
              <el-icon class="meta-icon"><View /></el-icon>
              {{ document.viewCount || 0 }} 浏览
            </span>
            <span class="meta-item">
              <el-tag :type="getPermissionType(document.permission)" size="small">
                {{ getPermissionText(document.permission) }}
              </el-tag>
            </span>
            <span class="meta-item">
              <el-tag size="small">{{ categoryLabel }}</el-tag>
            </span>
            <span class="meta-item" v-if="document.tags">
              <el-tag size="small">{{ document.tags }}</el-tag>
            </span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button
              :type="isLiked ? 'warning' : 'default'"
              @click="handleLike"
              :disabled="!authStore.isAuthenticated"
              class="action-button like-button"
            >
              <el-icon><Star /></el-icon>
              {{ isLiked ? '取消点赞' : '点赞' }} ({{ document.likeCount || 0 }})
            </el-button>
            <el-button
              :type="isCollected ? 'primary' : 'default'"
              @click="handleCollect"
              :disabled="!authStore.isAuthenticated"
              class="action-button collect-button"
            >
              <el-icon v-if="isCollected"><CollectionTag /></el-icon>
              <el-icon v-else><Collection /></el-icon>
              {{ isCollected ? '取消收藏' : '收藏' }}
            </el-button>
          </div>

          <!-- 文档内容 -->
          <div class="document-content">
            <div class="content" v-html="renderedContent"></div>
          </div>

          <!-- 评论区域 -->
          <div class="comment-section">
            <div class="comment-header">
              <h3>评论 ({{ comments.length }})</h3>
            </div>

            <!-- 评论输入框 -->
            <div class="comment-input" v-if="authStore.isAuthenticated">
              <router-link v-if="authStore.user?.id" :to="`/user/${authStore.user?.id}`" class="comment-avatar-link">
                <el-avatar :size="32" class="comment-avatar" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              </router-link>
              <el-avatar v-else :size="32" class="comment-avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="input-wrapper">
                <el-input
                  type="textarea"
                  v-model="newComment"
                  :rows="3"
                  :maxlength="150"
                  show-word-limit
                  placeholder="请输入评论，最多150个汉字..."
                />
                <el-button type="primary" @click="submitComment" class="submit-btn" :disabled="isSubmitting">发表评论</el-button>
              </div>
            </div>
            <div class="comment-login-tip" v-else>
              <el-button type="primary" @click="goToLogin">登录</el-button>
              <span>后发表评论</span>
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
              <!-- 所有评论（包括主评论和回复） -->
              <div v-for="comment in allComments" :key="comment.id" class="comment-item">
                <router-link 
                v-if="comment.userId" 
                :to="`/user/${comment.userId}`" 
                class="comment-avatar-link"
              >
              <el-avatar :size="40" class="comment-avatar" :src="comment.avatar">{{ comment.nickname?.charAt(0) || comment.username?.charAt(0) || 'U' }}</el-avatar>
              </router-link>
              <el-avatar v-else :size="40" class="comment-avatar">{{ comment.nickname?.charAt(0) || comment.username?.charAt(0) || 'U' }}</el-avatar>
                <div class="comment-body">
                  <div class="comment-meta">
                    <router-link 
                      v-if="comment.userId" 
                      :to="`/user/${comment.userId}`" 
                      class="comment-username"
                    >{{ comment.nickname || comment.username }}</router-link>
                    <span v-else class="comment-username">{{ comment.nickname || comment.username }}</span>
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">
                    <!-- 回复标记 -->
                    <span class="reply-mark" v-if="comment.repliedNickname">
                      <a 
                        v-if="comment.repliedUserId" 
                        :href="comment.repliedUserId ? `/user/${comment.repliedUserId}` : '#'" 
                        class="reply-to"
                        @click="comment.repliedUserId ? undefined : (e) => e.preventDefault()"
                      >@{{ comment.repliedNickname }}</a>
                      <span v-else>@{{ comment.repliedNickname }}</span>
                    </span>
                    <span v-if="comment.repliedNickname">&nbsp;</span>
                    {{ comment.content }}
                  </div>
                  <div class="comment-actions">
                    <el-button
                      type="text"
                      size="small"
                      @click="toggleReply(comment.id)"
                      v-if="authStore.isAuthenticated"
                    >
                      回复
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="deleteComment(comment.id)"
                      v-if="comment.userId === authStore.user?.id || document.userId === authStore.user?.id || isAdmin"
                    >
                      删除
                    </el-button>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div class="reply-input" v-if="replyingCommentId === comment.id && authStore.isAuthenticated">
                    <a v-if="authStore.user?.id" :href="`/user/${authStore.user?.id}`" class="comment-avatar-link">
                      <el-avatar :size="24" class="reply-avatar" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
                    </a>
                    <el-avatar v-else :size="24" class="reply-avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
                    <div class="reply-input-wrapper">
                      <el-input
                        type="textarea"
                        v-model="replyContent"
                        :rows="2"
                        :maxlength="150"
                        show-word-limit
                        placeholder="请输入回复，最多150个汉字..."
                      />
                      <div class="reply-buttons">
                        <el-button @click="replyingCommentId = null">取消</el-button>
                        <el-button type="primary" @click="submitReply(comment.id)">回复</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="allComments.length === 0" class="no-comments">
                暂无评论，来发表第一条评论吧！
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="content-right">
          <!-- 作者信息卡片 -->
          <el-card class="author-card">
            <template #header>
              <div class="card-header">
                <span>作者信息</span>
              </div>
            </template>
            <div class="author-profile">
              <a 
                v-if="authorInfo.id" 
                :href="authorInfo.id ? `/user/${authorInfo.id}` : '#'" 
                class="author-avatar-link"
                @click="authorInfo.id ? undefined : (e) => e.preventDefault()"
              >
                <el-avatar :size="64" class="author-avatar" :src="authorInfo.avatar">{{ authorInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
              </a>
              <el-avatar v-else :size="64" class="author-avatar">{{ authorInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
              <a 
                v-if="authorInfo.id" 
                :href="authorInfo.id ? `/user/${authorInfo.id}` : '#'" 
                class="author-name"
                @click="authorInfo.id ? undefined : (e) => e.preventDefault()"
              >{{ authorInfo.nickname || authorInfo.username }}</a>
              <span v-else class="author-name">{{ authorInfo.nickname || authorInfo.username }}</span>
              <div class="author-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ authorInfo.original || 0 }}</div>
                  <div class="stat-label">原创</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ authorInfo.likes || 0 }}</div>
                  <div class="stat-label">获赞</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ authorInfo.collections || 0 }}</div>
                  <div class="stat-label">收藏</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ authorInfo.followers || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ authorInfo.following || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
              </div>
              <div class="author-actions" v-if="authStore.isAuthenticated && authStore.user?.id !== authorInfo.id">
                <el-button
                  type="primary"
                  @click="handleMessage"
                  class="message-button"
                >
                  私信
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 作者其他文章 -->
          <el-card class="author-articles-card">
            <template #header>
              <div class="card-header">
                <span>作者其他文章</span>
              </div>
            </template>
            <div class="author-articles-list">
              <div class="category-section" v-for="(item, index) in authorDocuments" :key="index">
                <a :href="`/author/${document.userId}/category/${item.category}`" class="category-title">{{ item.category }}</a>
                <div v-if="item.document" class="article-item">
                  <a :href="`/document/${item.document.id}`" class="article-link">{{ item.document.title }}</a>
                </div>
                <div v-else class="no-article">
                  暂无该分类文章
                </div>
              </div>
              <div v-if="authorDocuments.length === 0" class="no-articles">
                暂无其他文章
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import api from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 生成头像 URL
const getAvatarUrl = (seed) => {
  // 使用 DiceBear 生成像素艺术风格的头像，根据种子生成唯一头像
  return `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(seed)}`
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const document = ref({
  id: null,
  title: '',
  content: '',
  category: '',
  tags: '',
  permission: 'private',
  updatedAt: '',
  viewCount: 0,
  likeCount: 0,
  userId: null
})
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)
const authorInfo = ref({
  id: null,
  username: '',
  nickname: '',
  original: 0,
  likes: 0,
  collections: 0
})
const authorDocuments = ref([])
const userInfo = ref({ id: null })
const categories = ref([])
const comments = ref([])
const newComment = ref('')
const replyContent = ref('')
const replyingCommentId = ref(null)
const isAdmin = ref(false)
const isSubmitting = ref(false)
const unreadMessageCount = ref(0)

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

const loadCategories = () => {
  categories.value = [
    { dictCode: 'tech', dictLabel: '技术' },
    { dictCode: 'life', dictLabel: '生活' },
    { dictCode: 'work', dictLabel: '工作' },
    { dictCode: 'entertainment', dictLabel: '娱乐' },
    { dictCode: 'other', dictLabel: '其他' }
  ]
}

const categoryLabel = computed(() => {
  if (!document.value.category || categories.value.length === 0) {
    return document.value.category
  }
  const category = categories.value.find(cat => cat.dictCode === document.value.category)
  return category ? category.dictLabel : document.value.category
})

const renderedContent = computed(() => {
  if (!document.value.content) return ''
  const rawHtml = marked(document.value.content)
  return DOMPurify.sanitize(rawHtml)
})

// 主评论（没有父评论的评论）
const mainComments = computed(() => {
  return comments.value.filter(comment => !comment.parentId)
})

// 获取子评论（回复）
const getSubComments = (parentId) => {
  return comments.value.filter(comment => comment.parentId === parentId)
}

// 递归获取所有嵌套回复
const getAllSubComments = (parentId) => {
  const subComments = getSubComments(parentId)
  return subComments.concat(
    ...subComments.map(comment => getAllSubComments(comment.id))
  )
}

// 所有评论（包括主评论和回复），按时间正序排序
const allComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    return new Date(a.createdAt) - new Date(b.createdAt)
  })
})

const loadDocument = async () => {
  const id = route.params.id
  console.log('loadDocument 被调用，id:', id)
  if (!id) {
    console.error('id 为空，无法加载文档')
    ElMessage.error('文档不存在或已被删除')
    router.push('/documents')
    return
  }

  try {
    console.log('准备调用 api.get(`/api/documents/${id}`)')
    const response = await api.get(`/api/documents/${id}`)
    console.log('api.get 调用成功，响应:', response)
    // 处理后端返回的数据格式
    if (response.data) {
      document.value = response.data
    } else {
      document.value = response
    }

    await loadAuthorInfo(document.value.userId)

    if (authStore.isAuthenticated) {
      await loadUserInteractionStatus(id)
      await checkFollowStatus(document.value.userId)
    }

    await loadComments(id)
  } catch (error) {
    console.error('加载文档失败:', error)
    console.error('错误响应:', error.response)
    if (error.response?.status === 404) {
      ElMessage.error('文档不存在或已被删除')
      router.push('/documents')
    } else if (error.response?.status !== 403) {
      ElMessage.error('加载文档失败')
      router.push('/documents')
    }
  }
}

const loadAuthorInfo = async (userId) => {
  try {
    authorInfo.value = {
      id: userId,
      username: '加载中...',
      nickname: '加载中...',
      original: 0,
      likes: 0,
      collections: 0
    }

    const response = await api.get(`/api/users/${userId}/stats`)
    const data = response
    // 将 userId 映射到 id，确保前端代码可以统一使用 authorInfo.value.id
    authorInfo.value = {
      ...data,
      id: data.userId || data.id || userId
    }
    await loadAuthorDocuments(userId)
  } catch (error) {
    console.error('加载作者信息失败:', error)
    authorInfo.value = {
      id: userId,
      username: 'user',
      nickname: '用户',
      original: 96,
      likes: 1652,
      collections: 1326
    }
  }
}

const loadAuthorDocuments = async (userId) => {
  try {
    const response = await api.get(`/api/documents/author/${userId}?excludeId=${document.value.id}`)
    authorDocuments.value = response
  } catch (error) {
    console.error('加载作者其他文章失败:', error)
    authorDocuments.value = []
  }
}

const loadUserInfo = async () => {
  try {
    const response = await api.get('/api/users/info')
    userInfo.value = response
    checkIsAdmin()
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const checkIsAdmin = () => {
  const user = authStore.user
  if (user && user.authorities) {
    isAdmin.value = user.authorities.some(a => a === 'ROLE_ADMIN')
  } else {
    isAdmin.value = false
  }
}

const loadUserInteractionStatus = async (documentId) => {
  try {
    const [collectResponse, likeResponse] = await Promise.all([
      api.get(`/api/documents/${documentId}/collect/status`),
      api.get(`/api/documents/${documentId}/like/status`)
    ]);
    isCollected.value = collectResponse;
    isLiked.value = likeResponse;
    console.log('收藏状态:', isCollected.value);
    console.log('点赞状态:', isLiked.value);
  } catch (error) {
    console.error('加载用户交互状态失败:', error);
    isCollected.value = false;
    isLiked.value = false;
  }
}

const checkFollowStatus = async (userId) => {
  if (!authStore.isAuthenticated || !userId || isNaN(userId)) {
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
    const userId = authorInfo.value.userId || authorInfo.value.id
    if (isFollowing.value) {
      const response = await api.delete(`/api/users/${userId}/follow`)
      isFollowing.value = false
      ElMessage.success('取消关注成功')
    } else {
      const response = await api.post(`/api/users/${userId}/follow`)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const loadComments = async (documentId) => {
  try {
    const response = await api.get(`/api/comments/document/${documentId}`)
    comments.value = response
  } catch (error) {
    console.error('加载评论失败:', error)
    comments.value = []
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (newComment.value.length > 150) {
    ElMessage.warning('评论内容不能超过150个字符')
    return
  }

  try {
    // 禁用提交按钮，防止重复点击
    isSubmitting.value = true
    
    console.log('开始发表评论，documentId:', document.value.id)
    console.log('评论内容:', newComment.value.trim())
    
    const response = await api.post('/api/comments', {
      documentId: document.value.id,
      content: newComment.value.trim()
    })
    
    console.log('评论发表成功，响应:', response)
    
    comments.value.unshift(response)
    newComment.value = ''
    ElMessage.success('评论发表成功')
  } catch (error) {
    console.error('发表评论失败:', error)
    console.error('错误响应:', error.response)
    ElMessage.error('发表评论失败')
  } finally {
    // 启用提交按钮
    isSubmitting.value = false
  }
}

const submitReply = async (parentId) => {
  console.log('submitReply 被调用，parentId:', parentId)
  console.log('document.value:', document.value)
  console.log('document.value.id:', document.value?.id)
  console.log('replyContent.value:', replyContent.value)

  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  if (replyContent.value.length > 150) {
    ElMessage.warning('回复内容不能超过150个字符')
    return
  }

  if (!document.value?.id) {
    console.error('document.value.id 为空')
    ElMessage.error('无法获取文档ID')
    return
  }

  try {
    console.log('准备发送回复请求...')
    console.log('请求数据:', {
      documentId: document.value.id,
      parentId: parentId,
      content: replyContent.value.trim()
    })
    
    const response = await api.post('/api/comments', {
      documentId: document.value.id,
      parentId: parentId,
      content: replyContent.value.trim()
    })
    console.log('回复请求成功，响应:', response)
    comments.value.push(response)
    replyContent.value = ''
    replyingCommentId.value = null
    ElMessage.success('回复发表成功')
  } catch (error) {
    console.error('发表回复失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('发表回复失败')
  }
}

const deleteComment = async (commentId) => {
  try {
    await api.delete(`/api/comments/${commentId}`)
    comments.value = comments.value.filter(c => c.id !== commentId)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除失败')
  }
}

const toggleReply = (commentId) => {
  if (replyingCommentId.value === commentId) {
    replyingCommentId.value = null
  } else {
    replyingCommentId.value = commentId
    replyContent.value = ''
  }
}

const goToLogin = () => {
  router.push(`/login?redirect=/document/${route.params.id}`)
}

const handleLike = async () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录后再点赞，是否前往登录页面？', '提示', {
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
    if (isLiked.value) {
      const response = await api.delete(`/api/documents/${document.value.id}/like`)
      isLiked.value = false
      document.value.likeCount = response
      // 直接更新作者点赞数，不需要重新加载整个作者信息
      if (authorInfo.value.likes && authorInfo.value.likes > 0) {
        authorInfo.value.likes--
      }
      ElMessage.success('取消点赞成功')
    } else {
      const response = await api.post(`/api/documents/${document.value.id}/like`)
      isLiked.value = true
      document.value.likeCount = response
      // 直接更新作者点赞数，不需要重新加载整个作者信息
      authorInfo.value.likes = (authorInfo.value.likes || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleCollect = async () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录后再收藏，是否前往登录页面？', '提示', {
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
    if (isCollected.value) {
      await api.delete(`/api/documents/${document.value.id}/collect`)
      isCollected.value = false
      // 直接更新作者收藏数，不需要重新加载整个作者信息
      if (authorInfo.value.collections && authorInfo.value.collections > 0) {
        authorInfo.value.collections--
      }
      ElMessage.success('取消收藏成功')
    } else {
      await api.post(`/api/documents/${document.value.id}/collect`)
      isCollected.value = true
      // 直接更新作者收藏数，不需要重新加载整个作者信息
      authorInfo.value.collections = (authorInfo.value.collections || 0) + 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('收藏操作失败:', error)
  }
}

const editDocument = () => {
  console.log('点击编辑按钮，文档ID:', document.value.id)
  console.log('当前路由:', route.path)
  console.log('目标路由:', `/editor/${document.value.id}`)
  try {
    router.push(`/editor/${document.value.id}`)
    console.log('路由跳转成功')
  } catch (error) {
    console.error('路由跳转失败:', error)
  }
}

const goBack = () => {
  // 尝试返回到上一个页面
  if (window.history.length > 1) {
    router.back()
  } else {
    // 如果没有上一个页面，返回文档列表页面
    router.push('/documents')
  }
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
    // 获取作者 ID
    const userId = authorInfo.value.userId || authorInfo.value.id
    console.log('作者 ID:', userId)
    
    // 检查是否关注了作者
    await checkFollowStatus(userId)
    
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

const getPermissionType = (permission) => {
  switch (permission) {
    case 'public': return 'success'
    case 'private': return 'info'
    case 'secret': return 'danger'
    default: return ''
  }
}

const getPermissionText = (permission) => {
  switch (permission) {
    case 'public': return '公开'
    case 'private': return '私密'
    case 'secret': return '绝密'
    default: return ''
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(async () => {
  loadCategories()
  await loadUserInfo()
  if (authStore.isAuthenticated) {
    await loadUnreadMessageCount()
  }
  await loadDocument()
})
</script>

<style scoped lang="scss">
/* 全局样式 */
.document-view-container {
  min-height: 100vh;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 顶部导航栏 */
.main-header {
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  padding: 12px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 15px;

      .back-button {
        color: #333;
        border: 1px solid #e5e6eb;

        &:hover {
          background: #f5f7fa;
        }
      }

      .site-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #333;
      }
    }

    .header-right {
      .user-dropdown {
        .user-info {
          display: flex;
          align-items: center;
          gap: 10px;
          cursor: pointer;
          padding: 8px 12px;
          border-radius: 20px;
          transition: background 0.3s;
          text-decoration: none;
          color: inherit;

          &:hover {
            background: #f5f7fa;
          }

          .username {
            font-size: 14px;
            font-weight: 500;
            color: #333;
          }

          .dropdown-icon {
            font-size: 12px;
            color: #666;
          }
        }
      }
    }
  }
}

/* 主内容区域 */
.main-content {
  padding: 20px 0;

  .container {
    display: flex;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    gap: 30px;

    /* 左侧内容区 */
    .content-left {
      flex: 1;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      padding: 30px;

      /* 文档标题 */
      .document-title {
        margin: 0 0 20px 0;
        font-size: 28px;
        font-weight: 700;
        color: #333;
        line-height: 1.3;
      }

      /* 作者信息 */
      .author-info {
        display: flex;
        align-items: center;
        gap: 15px;
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #f0f0f0;

        .author-avatar-link {
          text-decoration: none;
          color: inherit;

          &:hover {
            opacity: 0.8;
          }
        }

        .author-avatar {
          border: 2px solid #f0f0f0;
        }

        .author-details {
          flex: 1;

          .author-name {
            font-size: 16px;
            font-weight: 600;
            color: #1677ff;
            margin-bottom: 5px;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }
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
          display: flex;
          gap: 10px;

          .edit-button,
          .follow-button {
            border-radius: 6px;
            font-size: 14px;
          }
        }
      }

      /* 文档元信息 */
      .document-meta {
        display: flex;
        align-items: center;
        gap: 15px;
        font-size: 14px;
        color: #666;
        margin-bottom: 20px;
        flex-wrap: wrap;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 5px;

          .meta-icon {
            font-size: 14px;
            color: #999;
          }
        }
      }

      /* 操作按钮 */
      .action-buttons {
        display: flex;
        gap: 10px;
        margin-bottom: 30px;
        padding-bottom: 20px;
        border-bottom: 1px solid #f0f0f0;

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

      /* 文档内容 */
      .document-content {
        .content {
          line-height: 1.6;
          color: #333;

          h1, h2, h3, h4, h5, h6 {
            margin-top: 28px;
            margin-bottom: 16px;
            font-weight: 600;
            line-height: 1.3;
            color: #2c3e50;
          }

          h1 {
            font-size: 2em;
            border-bottom: 1px solid #eaecef;
            padding-bottom: .3em;
            margin-top: 0;
          }
          h2 {
            font-size: 1.5em;
            border-bottom: 1px solid #eaecef;
            padding-bottom: .3em;
          }
          h3 { font-size: 1.25em; }
          h4 { font-size: 1.1em; }
          h5 { font-size: 1em; }
          h6 { font-size: 0.9em; color: #666; }

          p {
            margin-bottom: 18px;
            text-align: justify;
          }

          code {
            padding: .2em .4em;
            margin: 0;
            font-size: 85%;
            background-color: rgba(27, 31, 35, .05);
            border-radius: 3px;
            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
          }

          pre {
            background-color: #f6f8fa;
            border-radius: 6px;
            padding: 16px;
            overflow: auto;
            font-size: 85%;
            line-height: 1.45;
            margin-bottom: 18px;

            code {
              background-color: transparent;
              padding: 0;
            }
          }

          table {
            border-spacing: 0;
            border-collapse: collapse;
            margin-bottom: 18px;
            width: 100%;

            th, td {
              padding: 10px 13px;
              border: 1px solid #dfe2e5;
              text-align: left;
            }

            th {
              font-weight: 600;
              background-color: #f6f8fa;
            }

            tr {
              background-color: #fff;
              border-top: 1px solid #c6cbd1;
            }

            tr:nth-child(2n) {
              background-color: #f6f8fa;
            }
          }

          blockquote {
            border-left: 4px solid #dfe2e5;
            padding: 12px 20px;
            color: #6a737d;
            margin: 0 0 18px 0;
            background-color: #f6f8fa;
            border-radius: 0 6px 6px 0;
          }

          ul, ol {
            margin-bottom: 18px;
            padding-left: 2em;
          }

          li {
            margin-bottom: 8px;
          }

          img {
            max-width: 100%;
            border-radius: 6px;
            margin: 10px 0;
          }

          a {
            color: #1677ff;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }
          }
        }
      }

      /* 评论区域 */
      .comment-section {
        margin-top: 40px;
        padding-top: 30px;
        border-top: 1px solid #f0f0f0;

        .comment-header {
          margin-bottom: 20px;

          h3 {
            margin: 0;
            font-size: 18px;
            font-weight: 600;
            color: #333;
          }
        }

        .comment-input {
          display: flex;
          gap: 15px;
          margin-bottom: 30px;

          .comment-avatar-link {
            text-decoration: none;
            color: inherit;

            &:hover {
              opacity: 0.8;
            }
          }

          .comment-avatar {
            flex-shrink: 0;
          }

          .input-wrapper {
            flex: 1;

            .submit-btn {
              margin-top: 10px;
            }
          }
        }

        .comment-login-tip {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 15px;
          background: #f5f7fa;
          border-radius: 6px;
          margin-bottom: 20px;

          span {
            color: #666;
            font-size: 14px;
          }
        }

        .comment-list {
          .comment-item {
            display: flex;
            gap: 12px;
            padding: 10px 0;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
              border-bottom: none;
            }

            .comment-avatar-link {
              text-decoration: none;
              color: inherit;

              &:hover {
                opacity: 0.8;
              }
            }

            .comment-avatar {
              flex-shrink: 0;
            }

            .comment-body {
              flex: 1;

              .comment-meta {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 5px;

                .comment-author {
                  font-weight: 600;
                  color: #1677ff;
                  font-size: 14px;
                  text-decoration: none;

                  &:hover {
                    text-decoration: underline;
                  }
                }

                .comment-time {
                  color: #999;
                  font-size: 12px;
                }
              }

              .comment-content {
                color: #333;
                font-size: 14px;
                line-height: 1.5;
                margin-bottom: 8px;

                .reply-mark {
                  color: #999;
                  font-size: 14px;

                  .reply-to {
                    color: #1677ff;
                    font-weight: 500;
                    text-decoration: none;

                    &:hover {
                      text-decoration: underline;
                    }
                  }
                }
              }

              .comment-actions {
                display: flex;
                gap: 8px;
                margin-bottom: 10px;

                el-button {
                  font-size: 12px;
                  color: #999;

                  &:hover {
                    color: #1677ff;
                  }
                }
              }

              /* 回复输入框 */
              .reply-input {
                display: flex;
                gap: 8px;
                margin-top: 10px;
                padding: 12px;
                background: #f5f7fa;
                border-radius: 4px;

                .comment-avatar-link {
                  text-decoration: none;
                  color: inherit;

                  &:hover {
                    opacity: 0.8;
                  }
                }

                .reply-avatar {
                  flex-shrink: 0;
                }

                .reply-input-wrapper {
                  flex: 1;

                  .reply-buttons {
                    display: flex;
                    gap: 8px;
                    margin-top: 8px;
                    justify-content: flex-end;
                  }
                }
              }

              /* 子评论 */
              .sub-comments {
                margin-top: 15px;
                margin-left: 55px;

                .sub-comment-item {
                  display: flex;
                  gap: 10px;
                  padding: 10px 0;
                  border-top: 1px solid #f0f0f0;

                  &:first-child {
                    border-top: none;
                    padding-top: 0;
                  }

                  .sub-comment-avatar {
                    flex-shrink: 0;
                  }

                  .sub-comment-body {
                    flex: 1;

                    .sub-comment-meta {
                      display: flex;
                      align-items: center;
                      gap: 10px;
                      margin-bottom: 5px;

                      .sub-comment-author {
                        font-weight: 600;
                        color: #333;
                        font-size: 13px;
                      }

                      .sub-comment-time {
                        color: #999;
                        font-size: 11px;
                      }
                    }

                    .sub-comment-content {
                      color: #333;
                      font-size: 13px;
                      line-height: 1.5;
                      margin-bottom: 8px;

                      .sub-reply-mark {
                        color: #999;
                        font-size: 11px;
                        margin-bottom: 3px;

                        .sub-reply-to {
                          color: #1677ff;
                          font-weight: 500;
                        }
                      }
                    }

                    .sub-comment-actions {
                      display: flex;
                      gap: 8px;
                    }

                    /* 回复输入框 */
                    .reply-input {
                      display: flex;
                      gap: 10px;
                      margin-top: 10px;
                      padding: 12px;
                      background: #f0f0f0;
                      border-radius: 4px;

                      .reply-avatar {
                        flex-shrink: 0;
                      }

                      .reply-input-wrapper {
                        flex: 1;

                        .reply-buttons {
                          display: flex;
                          gap: 8px;
                          margin-top: 8px;
                          justify-content: flex-end;
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          .no-comments {
            text-align: center;
            color: #999;
            padding: 30px 0;
            font-size: 14px;
          }
        }
      }
    }

    /* 右侧边栏 */
    .content-right {
      width: 300px;

      /* 作者信息卡片 */
      .author-card {
        margin-bottom: 20px;
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        .card-header {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          padding: 12px 16px;
          border-bottom: 1px solid #f0f0f0;
        }

        .author-profile {
          padding: 20px;
          text-align: center;

          .author-avatar-link {
            text-decoration: none;
            color: inherit;

            &:hover {
              opacity: 0.8;
            }
          }

          .author-avatar {
            margin: 0 auto 15px;
            border: 3px solid #f0f0f0;
          }

          .author-name {
            font-size: 16px;
            font-weight: 600;
            color: #1677ff;
            margin: 0 0 15px 0;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }
          }

          .author-stats {
            display: flex;
            flex-wrap: wrap;
            justify-content: space-around;

            .stat-item {
              text-align: center;
              flex: 1 1 100px;
              margin: 5px 0;

              .stat-value {
                font-size: 18px;
                font-weight: 600;
                color: #333;
                margin-bottom: 5px;
              }

              .stat-label {
                font-size: 12px;
                color: #666;
              }
            }
          }

          .author-actions {
            margin-top: 20px;

            .follow-button {
              width: 100%;
              border-radius: 6px;
              font-size: 14px;
            }
          }
        }
      }

      /* 作者其他文章 */
      .author-articles-card {
        border: 1px solid #f0f0f0;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        margin-bottom: 20px;

        .card-header {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          padding: 12px 16px;
          border-bottom: 1px solid #f0f0f0;
        }

        .author-articles-list {
          padding: 15px 16px;

          .category-section {
            margin-bottom: 15px;
            padding-bottom: 15px;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
              margin-bottom: 0;
              padding-bottom: 0;
              border-bottom: none;
            }

            .category-title {
              font-size: 14px;
              font-weight: 600;
              color: #1677ff;
              margin-bottom: 8px;
              text-transform: capitalize;
              text-decoration: none;
              display: inline-block;
              transition: all 0.3s ease;

              &:hover {
                color: #409eff;
                text-decoration: underline;
              }
            }

            .article-item {
              margin-left: 8px;

              .article-link {
                color: #333;
                text-decoration: none;
                font-size: 14px;
                display: block;
                padding: 5px 0;
                transition: all 0.3s ease;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;

                &:hover {
                  color: #1677ff;
                  transform: translateX(5px);
                }
              }
            }

            .no-article {
              color: #999;
              font-size: 13px;
              margin-left: 8px;
              padding: 5px 0;
            }
          }

          .no-articles {
            text-align: center;
            color: #999;
            padding: 20px 0;
          }
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 992px) {
  .main-content {
    .container {
      flex-direction: column;

      .content-right {
        width: 100%;

        .author-articles-card {
          .author-articles-list {
            .category-section {
              margin-bottom: 12px;
              padding-bottom: 12px;

              .category-title {
                font-size: 13px;
              }

              .article-item {
                .article-link {
                  font-size: 13px;
                }
              }

              .no-article {
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .main-header {
    .header-content {
      padding: 0 15px;

      .header-left {
        .site-title {
          font-size: 18px;
        }
      }

      .header-right {
        .user-dropdown {
          .user-info {
            .username {
              display: none;
            }
          }
        }
      }
    }
  }

  .main-content {
    padding: 10px 0;

    .container {
      padding: 0 15px;
      gap: 20px;

      .content-left {
        padding: 20px;

        .document-title {
          font-size: 24px;
        }

        .author-info {
          flex-wrap: wrap;

          .edit-button {
            font-size: 12px;
          }
        }

        .document-meta {
          font-size: 12px;
          gap: 10px;
        }

        .action-buttons {
          .action-button {
            font-size: 12px;
          }
        }

        .document-content {
          .content {
            padding: 0;

            h1 { font-size: 1.8em; }
            h2 { font-size: 1.4em; }
            h3 { font-size: 1.2em; }
          }
        }

        .comment-section {
          .comment-list {
            .comment-item {
              .comment-body {
                .sub-comments {
                  margin-left: 45px;
                }
              }
            }
          }
        }
      }

      .content-right {
        .author-articles-card {
          .author-articles-list {
            padding: 10px 12px;

            .category-section {
              margin-bottom: 10px;
              padding-bottom: 10px;

              .category-title {
                font-size: 12px;
              }

              .article-item {
                .article-link {
                  font-size: 12px;
                }
              }

              .no-article {
                font-size: 11px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
