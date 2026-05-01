<template>
  <div class="message-center-container">
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
              <router-link :to="`/user/${authStore.user?.id}`" class="avatar-link">
                <el-avatar :size="32" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              </router-link>
              <div class="username-with-badge">
                <router-link :to="`/user/${authStore.user?.id}`" class="username-link">
                  <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
                </router-link>
                <el-badge v-if="totalUnreadMessageCount > 0" is-dot class="username-badge" />
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <span v-else class="user-info">
              <el-avatar :size="32">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="username-with-badge">
                <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
                <el-badge v-if="totalUnreadMessageCount > 0" is-dot class="username-badge" />
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
                      <el-badge v-if="totalUnreadMessageCount > 0" :value="totalUnreadMessageCount > 99 ? '99+' : totalUnreadMessageCount" class="message-badge" />
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
          <!-- 消息中心标题 -->
          <h1 class="message-center-title">消息中心</h1>

          <!-- 消息标签页 -->
          <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="message-tabs">
            <el-tab-pane name="message">
              <template #label>
                <div class="tab-with-badge">
                  <span>我的消息</span>
                  <el-badge v-if="unreadMessageCount > 0" :value="unreadMessageCount > 99 ? '99+' : unreadMessageCount" class="badge" />
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane name="comment">
              <template #label>
                <div class="tab-with-badge">
                  <span>评论和@</span>
                  <el-badge v-if="unreadCommentCount > 0" :value="unreadCommentCount > 99 ? '99+' : unreadCommentCount" class="badge" />
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane name="follower">
              <template #label>
                <div class="tab-with-badge">
                  <span>新增粉丝</span>
                  <el-badge v-if="unreadFollowerCount > 0" :value="unreadFollowerCount > 99 ? '99+' : unreadFollowerCount" class="badge" />
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane name="like">
              <template #label>
                <div class="tab-with-badge">
                  <span>赞和收藏</span>
                  <el-badge v-if="unreadLikeCount > 0" :value="unreadLikeCount > 99 ? '99+' : unreadLikeCount" class="badge" />
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane name="chat">
              <template #label>
                <div class="tab-with-badge">
                  <span>聊天</span>
                  <el-badge v-if="unreadCount > 0" :value="unreadCount > 99 ? '99+' : unreadCount" class="badge" />
                </div>
              </template>
            </el-tab-pane>
          </el-tabs>

          <!-- 消息内容区域 -->
          <div class="message-content">
            <!-- 消息列表 -->
            <div v-if="activeTab !== 'chat'" class="message-list">
              <div v-if="messages.length === 0" class="empty-message">
                <el-empty description="暂无消息" />
              </div>
              <div v-else v-for="message in messages" :key="message.id" class="message-item" @click="handleMessageClick(message)">
                <div class="message-info">
                  <div class="message-type" :class="message.type">
                  {{ message.type === 'comment' || message.type === 'comment_replay' ? '评论' : message.type === 'like' ? '点赞' : message.type === 'follower' ? '粉丝' : '系统' }}
                </div>
                  <div class="message-content-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatDate(message.createdAt) }}</div>
                </div>
              </div>
              <!-- 分页组件 -->
              <div v-if="messages.length > 0" class="pagination-container">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="totalMessages"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>

            <!-- 聊天界面 -->
            <div v-else class="chat-container">
              <div class="chat-sidebar">
                <el-input
                  v-model="searchUser"
                  placeholder="搜索用户"
                  prefix-icon="el-icon-search"
                  class="search-input"
                  @input="searchUsers"
                />
                <!-- 搜索结果 -->
                <div v-if="searchResults.length > 0" class="search-results">
                  <div v-for="(user, index) in searchResults" :key="index" @click="selectSearchUser(user)" class="search-result-item">
                    <a :href="`/user/${user.id}`" class="avatar-link" target="_blank">
                      <el-avatar :size="40" :src="user.avatar">{{ user.nickname?.charAt(0) || user.username?.charAt(0) || 'U' }}</el-avatar>
                    </a>
                    <div class="user-info">
                      <div class="user-name">{{ user.nickname || user.username }}</div>
                    </div>
                  </div>
                </div>
                <!-- 聊天用户列表 -->
                <div v-else-if="!chatUsers || chatUsers.length === 0" class="empty-chat-users">
                  <el-empty description="暂无聊天用户" />
                </div>
                <div v-else class="chat-users-list">
                  <div v-for="(user, index) in chatUsers" :key="index" @click="selectChatUser(user)" @contextmenu="handleContextMenu($event, user)" :class="{ active: selectedChatUser && selectedChatUser.id === user.id }" class="chat-user-card">
                    <div class="chat-user-info">
                      <router-link :to="`/user/${user.id}`" class="avatar-link">
                        <el-avatar :size="40" :src="user.avatar">{{ user.nickname?.charAt(0) || user.username?.charAt(0) || 'U' }}</el-avatar>
                      </router-link>
                      <div class="user-info">
                        <div class="user-name">{{ user.nickname || user.username }}</div>
                        <div v-if="user.lastMessage" class="last-message">{{ user.lastMessage }}</div>
                        <div v-else class="last-message">暂无消息</div>
                      </div>
                    </div>
                    <div class="chat-time">
                      {{ user.lastMessageTime ? formatDate(user.lastMessageTime) : '' }}
                    </div>
                    <div v-if="user.unreadCount > 0 && !user.doNotDisturb" class="unread-badge">{{ user.unreadCount }}</div>
                  </div>
                  <!-- 右键菜单 -->
                  <div v-if="contextMenuVisible" :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }" class="context-menu">
                    <div class="context-menu-item" @click="enterUserHome(contextMenuUser)">进入个人主页</div>
                    <div class="context-menu-item" @click="pinChat(contextMenuUser)">{{ contextMenuUser?.isPinned ? '取消置顶' : '置顶聊天' }}</div>
                    <div class="context-menu-item" @click="toggleDoNotDisturb(contextMenuUser)">{{ contextMenuUser?.doNotDisturb ? '取消免打扰' : '消息免打扰' }}</div>
                    <div class="context-menu-item" :class="{ 'in-blacklist': contextMenuUser?.isBlacklisted }" @click="toggleBlacklist(contextMenuUser)">
                      {{ contextMenuUser?.isBlacklisted ? '移出黑名单' : '加入黑名单' }}
                    </div>
                    <div class="context-menu-item delete" @click="deleteChat(contextMenuUser)">删除对话</div>
                  </div>
                </div>
              </div>
              <div class="chat-content">
                <div v-if="!selectedChatUser" class="empty-chat">
                  <el-empty description="请选择一个用户开始聊天" />
                </div>
                <div v-else class="chat-messages">
                  <!-- 聊天消息 -->
                    <div v-if="chatMessages && chatMessages.length > 0">
                      <div v-for="(message, idx) in chatMessages" 
                        :key="idx"
                        :class="{ 'message-sent': message.senderId === currentUserId, 'message-received': message.senderId !== currentUserId }"
                        class="message-item-chat"
                      >
                        <!-- 接收消息 -->
                        <div v-if="message.senderId !== currentUserId" class="message-received-container">
                          <div class="message-avatar">
                            <router-link :to="`/user/${message.senderId}`" class="avatar-link">
                              <el-avatar :size="32" :src="message.senderAvatar">{{ message.senderNickname?.charAt(0) || message.senderUsername?.charAt(0) || 'U' }}</el-avatar>
                            </router-link>
                          </div>
                          <div class="message-bubble received">
                            <div class="message-content-text">{{ message.content }}</div>
                            <div class="message-time">{{ formatDate(message.timestamp) }}</div>
                          </div>
                        </div>
                        <!-- 发送消息 -->
                        <div v-else class="message-sent-container">
                          <div class="message-bubble sent">
                            <div class="message-content-text">{{ message.content }}</div>
                            <div class="message-time">{{ formatDate(message.timestamp) }}</div>
                          </div>
                          <div class="message-avatar">
                            <router-link :to="`/user/${currentUserId}`" class="avatar-link">
                              <el-avatar :size="32" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
                            </router-link>
                          </div>
                        </div>
                      </div>
                    </div>
                  <div v-else>
                    <el-empty description="暂无聊天消息" />
                  </div>
                </div>
                <!-- 聊天输入 -->
                <div class="chat-input-area">
                  <el-input
                    v-model="messageContent"
                    placeholder="输入消息..."
                    type="textarea"
                    :rows="2"
                    class="chat-input"
                  />
                  <el-button 
                    type="primary" 
                    @click="sendMessage" 
                    class="send-button" 
                    :disabled="!selectedChatUser || !messageContent.trim()"
                  >
                    发送
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="content-right">
          <!-- 用户信息卡片 -->
          <el-card class="user-card">
            <template #header>
              <div class="card-header">
                <span>我的信息</span>
              </div>
            </template>
            <div class="user-profile">
              <div class="author-info-center">
                <el-avatar :size="64" class="user-avatar" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || authStore.user?.username?.charAt(0) || 'U' }}</el-avatar>
                <div class="user-name">{{ authStore.user?.nickname || authStore.user?.username || '未登录' }}</div>
              </div>
              <div class="user-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.original || 0 }}</div>
                  <div class="stat-label">原创</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.likes || 0 }}</div>
                  <div class="stat-label">获赞</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.collections || 0 }}</div>
                  <div class="stat-label">收藏</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.followers || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
              </div>
              <div class="following-stats">
                <div class="stat-value">{{ userStats.following || 0 }}</div>
                <div class="stat-label">关注</div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api, { avatarApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 状态管理
const activeTab = ref('message')
const messages = ref([])
const chatUsers = ref([])
const selectedChatUser = ref(null)
const chatMessages = ref([])
const messageContent = ref('')
const searchUser = ref('')
const searchResults = ref([])
const isSearching = ref(false)
const userStats = ref({
  original: 0,
  likes: 0,
  collections: 0,
  followers: 0,
  following: 0
})
const unreadCount = ref(0)
// 未读消息数量
const unreadMessageCount = ref(0)
const unreadCommentCount = ref(0)
const unreadFollowerCount = ref(0)
const unreadLikeCount = ref(0)
const totalUnreadMessageCount = ref(0) // 总未读消息数量
// 轮询定时器
let pollingTimer = null
// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuUser = ref(null)
// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalMessages = ref(0)

// 当前用户ID（实际应用中应该从登录状态获取）
const currentUserId = ref(authStore.user?.id || 1)

// 加载消息列表
const loadMessages = async (type = 'message') => {
  console.log('===================== 加载消息列表 =====================')
  console.log('type:', type)
  try {
    let response
    if (type === 'message') {
      console.log('调用 api.get(\'/api/messages\')')
      response = await api.get('/api/messages', {
        params: {
          page: currentPage.value,
          pageSize: pageSize.value
        }
      })
    } else if (type === 'comment') {
      // 构建URL，传递多个types参数
      console.log('调用 api.get(\'/api/messages/types?types=comment&types=comment_replay\')')
      response = await api.get('/api/messages/types', {
        params: {
          types: ['comment', 'comment_replay'],
          page: currentPage.value,
          pageSize: pageSize.value
        }
      })
    } else if (type === 'follower') {
      console.log('调用 api.get(\'/api/messages/type/follow\')')
      response = await api.get('/api/messages/type/follow', {
        params: {
          page: currentPage.value,
          pageSize: pageSize.value
        }
      })
    } else if (type === 'like') {
      // 构建URL，传递多个types参数
      console.log('调用 api.get(\'/api/messages/types?types=like&types=collect\')')
      response = await api.get('/api/messages/types', {
        params: {
          types: ['like', 'collect'],
          page: currentPage.value,
          pageSize: pageSize.value
        }
      })
    }
    // 处理后端返回的数据格式
    console.log('加载消息成功，响应数据:', response)
    console.log('响应数据类型:', typeof response)
    console.log('响应数据的data属性:', response.data)
    // 根据 response 的类型来决定如何获取消息列表
    let messageList = []
    let total = 0
    if (response && typeof response === 'object') {
      // 处理API响应格式: {"data": [...], "total": 100, "page": 1, "pageSize": 10}
      if (response.data && Array.isArray(response.data)) {
        messageList = response.data
        total = response.total || 0
      } else if (Array.isArray(response)) {
        // 处理非分页数据
        messageList = response
      }
    } else if (Array.isArray(response)) {
      // 处理数组数据
      messageList = response
    }
    console.log('消息列表:', messageList)
    console.log('消息列表长度:', messageList.length)
    console.log('总消息数:', total)
    // 更新 messages
    messages.value = messageList
    totalMessages.value = total
    console.log('messages.value:', messages.value)
    console.log('messages.value 长度:', messages.value.length)
    // 使用 nextTick 确保页面重新渲染
    await nextTick()
    console.log('页面已重新渲染')
  } catch (error) {
    console.error('加载消息失败:', error)
    messages.value = []
    totalMessages.value = 0
  }
  console.log('===================== 加载消息列表结束 =====================')
}

// 加载所有未读消息数量
const loadUnreadMessageCounts = async () => {
  try {
    // 分别获取各类型消息的未读数量
    // 加载评论消息未读数量
    const commentResponse = await api.get('/api/messages/types?types=comment&types=comment_replay')
    const commentList = commentResponse.data && Array.isArray(commentResponse.data) ? commentResponse.data : []
    unreadCommentCount.value = commentList.filter(msg => !msg.isRead).length
    
    // 加载粉丝消息未读数量
    const followerResponse = await api.get('/api/messages/type/follow')
    const followerList = followerResponse.data && Array.isArray(followerResponse.data) ? followerResponse.data : []
    unreadFollowerCount.value = followerList.filter(msg => !msg.isRead).length
    
    // 加载赞和收藏消息未读数量
    const likeResponse = await api.get('/api/messages/types?types=like&types=collect')
    const likeList = likeResponse.data && Array.isArray(likeResponse.data) ? likeResponse.data : []
    unreadLikeCount.value = likeList.filter(msg => !msg.isRead).length
    
    // 计算我的消息未读数量（评论和@ + 新增粉丝 + 赞和收藏）
    unreadMessageCount.value = unreadCommentCount.value + unreadFollowerCount.value + unreadLikeCount.value
    
    // 计算总未读消息数量（我的消息 + 聊天消息）
    totalUnreadMessageCount.value = unreadMessageCount.value + unreadCount.value
  } catch (error) {
    console.error('加载未读消息数量失败:', error)
  }
}

// 加载聊天用户列表
const loadChatUsers = async () => {
  try {
    console.log('开始加载聊天用户列表，userId参数:', route.query.userId)
    const response = await api.get('/api/chat/messages/list')
    // 检查响应是否是ApiResponse格式
    let users = response.data ? response.data : response || []
    console.log('获取到的聊天用户列表:', users)
    
    // 保留默认用户（仅保留未删除的）
    const defaultUsers = chatUsers.value.filter(user => user.isDefault && !user.deleted)
    console.log('保留的默认用户:', defaultUsers)
    
    // 合并默认用户和新用户，避免重复
    for (const defaultUser of defaultUsers) {
      const existingUserIndex = users.findIndex(user => user.id == defaultUser.id)
      if (existingUserIndex === -1) {
        // 尝试获取用户的真实信息
          try {
            const userInfoResponse = await api.get(`/api/users/${defaultUser.id}`)
            // 检查响应是否是ApiResponse格式
            const userInfo = userInfoResponse.data ? userInfoResponse.data : userInfoResponse
            console.log('获取到的用户信息:', userInfo)
            // 检查用户是否在黑名单中
            const isBlacklistedResponse = await api.get(`/api/chat/blacklist/check`, {
              params: { userId: defaultUser.id }
            })
            // 检查响应是否是ApiResponse格式
            const isBlacklisted = isBlacklistedResponse.data ? isBlacklistedResponse.data : isBlacklistedResponse
            const updatedUser = {
              id: defaultUser.id,
              nickname: userInfo.nickname || defaultUser.nickname,
              username: userInfo.username || defaultUser.username,
              avatar: userInfo.avatar,
              lastMessage: defaultUser.lastMessage,
              lastMessageTime: defaultUser.lastMessageTime,
              unreadCount: defaultUser.unreadCount,
              isDefault: true, // 标记为默认用户
              deleted: false,
              isBlacklisted: isBlacklisted
            }
          users.push(updatedUser)
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，使用默认用户
          users.push({...defaultUser, deleted: false, isBlacklisted: false})
        }
      }
    }
    
    // 计算未读消息总数（排除免打扰用户）
    let totalUnread = 0
    users.forEach(user => {
      // 如果用户开启了免打扰，不计入未读消息数
      if (!user.doNotDisturb) {
        totalUnread += user.unreadCount || 0
      }
    })
    // 最多显示99个，超过99时显示99+
    unreadCount.value = totalUnread
    console.log('未读消息总数:', totalUnread)
    
    // 按照最后消息时间排序，时间越近越靠前，置顶的用户排在最前面
    users.sort((a, b) => {
      // 首先按置顶状态排序，置顶的排前面
      if (a.isPinned && !b.isPinned) return -1
      if (!a.isPinned && b.isPinned) return 1
      
      // 处理不同格式的时间字符串
      const getTimeStamp = (time) => {
        if (!time) return 0
        // 尝试不同的时间格式解析
        try {
          // 处理 YYYY/MM/DD HH:MM:SS 格式
          if (time.includes('/')) {
            const parts = time.split(' ')
            const dateParts = parts[0].split('/')
            const timeParts = parts[1].split(':')
            const date = new Date(
              parseInt(dateParts[0]),
              parseInt(dateParts[1]) - 1, // 月份从0开始
              parseInt(dateParts[2]),
              parseInt(timeParts[0]),
              parseInt(timeParts[1]),
              parseInt(timeParts[2])
            )
            return date.getTime()
          }
          // 处理其他格式
          const date = new Date(time)
          return date.getTime()
        } catch (error) {
          console.error('时间解析错误:', error, '时间字符串:', time)
          return 0
        }
      }
      
      const timeA = getTimeStamp(a.lastMessageTime)
      const timeB = getTimeStamp(b.lastMessageTime)
      console.log('排序前:', a.nickname, a.lastMessageTime, timeA, b.nickname, b.lastMessageTime, timeB, '比较结果:', timeB - timeA)
      const result = timeB - timeA
      console.log('排序后:', a.nickname, '应该排在', b.nickname, result < 0 ? '前面' : '后面')
      return result
    })
    console.log('排序后的聊天用户列表:', users)
    
    chatUsers.value = users
    console.log('最终的聊天用户列表:', chatUsers.value)
    // 更新总未读消息数量（我的消息 + 聊天消息）
    totalUnreadMessageCount.value = unreadMessageCount.value + unreadCount.value
  } catch (error) {
    console.error('加载聊天用户列表失败:', error)
    // 使用默认数据
    let defaultUsers = []
    if (route.query.userId) {
      // 如果通过私信进来，添加默认用户
      const targetUserId = parseInt(route.query.userId)
      defaultUsers = [{
        id: targetUserId,
        nickname: '用户' + targetUserId,
        username: 'user' + targetUserId,
        lastMessage: '暂无消息',
        lastMessageTime: new Date().toISOString(),
        unreadCount: 0,
        isDefault: true // 标记为默认用户
      }]
      console.log('使用默认用户:', defaultUsers)
    }
    chatUsers.value = defaultUsers
    unreadCount.value = 0
    // 更新总未读消息数量（我的消息 + 聊天消息）
    totalUnreadMessageCount.value = unreadMessageCount.value + unreadCount.value
  }
}

// 选择聊天用户
const selectChatUser = async (user) => {
  selectedChatUser.value = user
  // 如果有未读消息，标记为已读
  if (user.unreadCount > 0) {
    try {
      await api.put(`/api/chat/read?senderId=${user.id}`)
      // 标记为已读后刷新聊天用户列表，更新未读消息数量
      await loadChatUsers()
    } catch (error) {
      console.error('标记消息为已读失败:', error)
    }
  }
  await loadChatMessages(user.id)
}

// 加载聊天消息
const loadChatMessages = async (userId, scrollToBottom = true) => {
  try {
    const response = await api.get('/api/chat/messages', {
      params: { receiverId: userId }
    })
    
    // 为每条消息添加senderAvatar字段
    if (response && Array.isArray(response)) {
      // 获取对方用户信息
      const userInfoResponse = await api.get(`/api/users/${userId}`)
      const userAvatar = userInfoResponse?.avatar
      
      // 处理消息，添加senderAvatar字段
      chatMessages.value = response.map(message => {
        // 如果消息是对方发送的，使用对方的头像
        if (message.senderId === userId) {
          return {
            ...message,
            senderAvatar: userAvatar
          }
        }
        // 如果消息是自己发送的，使用自己的头像
        else {
          return {
            ...message,
            senderAvatar: authStore.user?.avatar
          }
        }
      })
    } else {
      chatMessages.value = []
    }
    
    // 只有在需要时才滚动到最新的一条消息（底部）
    if (scrollToBottom) {
      setTimeout(() => {
        const chatMessagesContainer = document.querySelector('.chat-messages')
        if (chatMessagesContainer) {
          chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight
        }
      }, 100)
    }
  } catch (error) {
    console.error('加载聊天消息失败:', error)
    chatMessages.value = []
  }
}

// 发送消息
const sendMessage = async () => {
  // ✅ 修复：先做判断，不满足就提示
  if (!selectedChatUser.value) {
    ElMessage.warning('请先选择聊天用户')
    return
  }
  if (!messageContent.value || !messageContent.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  try {
    const response = await api.post('/api/chat', {
      receiverId: selectedChatUser.value.id,
      content: messageContent.value.trim()
    })
    
    if (response) {
      // 为发送的消息添加senderAvatar字段
      const messageWithAvatar = {
        ...response,
        senderAvatar: authStore.user?.avatar
      }
      chatMessages.value.push(messageWithAvatar)
      messageContent.value = ''
      ElMessage.success('发送成功')
      
      // 滚动到最新的消息（底部）
      setTimeout(() => {
        const chatMessagesContainer = document.querySelector('.chat-messages')
        if (chatMessagesContainer) {
          chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight
        }
      }, 100)
      
      // 发送后刷新聊天列表
      await loadChatUsers()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败：' + (error.message || '网络异常'))
  }
}

// 处理消息点击
const handleMessageClick = (message) => {
  console.log('===================== 消息点击事件 =====================')
  console.log('点击消息:', message)
  console.log('消息类型:', message.type)
  console.log('documentId:', message.documentId)
  console.log('relatedId:', message.relatedId)
  console.log('消息内容:', message.content)
  console.log('消息中的文档ID:', message.documentId)
  
  // 对于评论和评论回复消息，使用 message.documentId 作为文档 ID
  // 对于其他消息，使用 message.documentId || message.relatedId 作为文档 ID
  let documentId
  if (message.type === 'comment' || message.type === 'comment_replay') {
    documentId = message.documentId
  } else {
    documentId = message.documentId || message.relatedId
  }
  
  console.log('确定的 documentId:', documentId)
  console.log('documentId 类型:', typeof documentId)
  console.log('documentId 是否为空:', !documentId)
  console.log('documentId 是否为数字:', !isNaN(documentId))
  
  if (documentId) {
    console.log('准备跳转到文档详情页，documentId:', documentId)
    // 调用 viewDocument 函数跳转到文档详情页
    viewDocument(documentId)
  } else {
    console.error('documentId 为空，无法跳转到文档详情页')
    ElMessage.error('文档不存在或已被删除')
  }
  console.log('===================== 消息点击事件结束 =====================')
}

// 查看文档
const viewDocument = (documentId) => {
  console.log('===================== 跳转到文档详情页 =====================')
  console.log('documentId:', documentId)
  console.log('documentId 类型:', typeof documentId)
  if (documentId) {
    const targetUrl = window.location.origin + `/document/${documentId}`
    console.log('导航到:', targetUrl)
    console.log('准备执行 window.location.href')
    window.location.href = targetUrl
    console.log('执行 window.location.href 完成')
  } else {
    console.error('documentId 为空，无法跳转到文档详情页')
    ElMessage.error('文档不存在或已被删除')
  }
  console.log('===================== 跳转到文档详情页结束 =====================')
}

// 处理标签点击
const handleTabClick = async (tab) => {
  console.log('===================== 标签点击事件 =====================')
  console.log('点击的标签:', tab)
  console.log('标签的name:', tab.props.name)
  activeTab.value = tab.props.name
  console.log('activeTab.value:', activeTab.value)
  if (activeTab.value === 'chat') {
    console.log('当前是聊天标签，加载聊天用户列表')
    await loadChatUsers()
    // 如果有userId参数，选择对应的用户
    if (route.query.userId) {
      const targetUserId = parseInt(route.query.userId)
      let user = chatUsers.value.find(u => u.id == targetUserId)
      if (user) {
        await selectChatUser(user)
      } else {
        // 如果用户不在列表中，添加一个默认用户
        try {
          // 尝试获取用户的真实信息
          const userInfoResponse = await api.get(`/api/users/${targetUserId}`)
          console.log('获取到的用户信息:', userInfoResponse)
          const defaultUser = {
            id: targetUserId,
            nickname: userInfoResponse.nickname || '用户' + targetUserId,
            username: userInfoResponse.username || 'user' + targetUserId,
            avatar: userInfoResponse.avatar,
            lastMessage: '暂无消息',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0,
            isDefault: true // 标记为默认用户
          }
          chatUsers.value.unshift(defaultUser)
          await selectChatUser(defaultUser)
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，使用默认昵称
          const defaultUser = {
            id: targetUserId,
            nickname: '用户' + targetUserId,
            username: 'user' + targetUserId,
            lastMessage: '暂无消息',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0,
            isDefault: true // 标记为默认用户
          }
          chatUsers.value.unshift(defaultUser)
          await selectChatUser(defaultUser)
        }
      }
    }
  } else {
    console.log('当前不是聊天标签，加载消息列表，type:', activeTab.value)
    // 重置分页状态
    currentPage.value = 1
    await loadMessages(activeTab.value)
    // 标记该分类的消息为已读
    try {
      await api.put('/api/messages/read')
      // 更新未读消息数量
      await loadUnreadMessageCounts()
    } catch (error) {
      console.error('标记消息为已读失败:', error)
    }
  }
  console.log('===================== 标签点击事件结束 =====================')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 从后端获取头像 URL
const getAvatarUrl = async (userId) => {
  try {
    const avatar = await avatarApi.getAvatar(userId)
    return avatar || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(userId)}`
  } catch (error) {
    console.error('获取头像失败:', error)
    return `https://api.dicebear.com/7.x/pixel-art/svg?seed=${encodeURIComponent(userId)}`
  }
}

// 返回上一页
const goBack = () => {
  router.push('/documents')
}

// 处理用户菜单命令
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

// 搜索用户
const searchUsers = async () => {
  console.log('searchUsers 被调用，searchUser.value:', searchUser.value)
  if (!searchUser.value || searchUser.value.trim() === '') {
    console.log('搜索关键词为空，清空搜索结果')
    searchResults.value = []
    return
  }
  
  isSearching.value = true
  try {
    console.log('开始搜索用户，关键词:', searchUser.value.trim())
    const response = await api.get('/api/users/search', {
      params: { keyword: searchUser.value.trim() }
    })
    console.log('搜索结果:', response)
    console.log('搜索结果类型:', typeof response)
    console.log('搜索结果是否是数组:', Array.isArray(response))
    searchResults.value = response || []
    console.log('searchResults.value:', searchResults.value)
  } catch (error) {
    console.error('搜索用户失败:', error)
    console.error('错误响应:', error.response)
    searchResults.value = []
  } finally {
    isSearching.value = false
  }
}

// 选择搜索结果中的用户
const selectSearchUser = async (user) => {
  // 检查该用户是否已经在聊天列表中
  let existingUser = chatUsers.value.find(u => u.id === user.id)
  if (!existingUser) {
    // 如果不在聊天列表中，添加一个新的聊天用户
    existingUser = {
      id: user.id,
      nickname: user.nickname || user.username,
      username: user.username,
      avatar: user.avatar,
      lastMessage: '暂无消息',
      lastMessageTime: new Date().toISOString(),
      unreadCount: 0,
      isDefault: true // 标记为默认用户，避免被loadChatUsers移除
    }
    chatUsers.value.unshift(existingUser)
  }
  
  // 选择该用户开始聊天
  await selectChatUser(existingUser)
  // 清空搜索结果
  searchResults.value = []
  searchUser.value = ''
}

// 加载用户统计信息
const loadUserStats = async () => {
  try {
    const response = await api.get('/api/users/stats')
    userStats.value = response || {
      original: 105,
      likes: 13,
      collections: 13,
      followers: 1,
      following: 2
    }
  } catch (error) {
    console.error('加载用户统计信息失败:', error)
    // 使用默认值
    userStats.value = {
      original: 105,
      likes: 13,
      collections: 13,
      followers: 1,
      following: 2
    }
  }
}

// 启动轮询
const startPolling = () => {
  // 每3秒轮询一次
  pollingTimer = setInterval(async () => {
    try {
      // 加载未读消息数量
      await loadUnreadMessageCounts()
      // 只有在聊天标签时才加载聊天用户列表，更新未读消息数量
      if (activeTab.value === 'chat') {
        await loadChatUsers()
        // 如果已选择用户，加载最新消息
        if (selectedChatUser.value) {
          const oldMessageCount = chatMessages.value.length
          
          // 检查用户是否正在查看最新消息（滚动条在底部）
          const chatMessagesContainer = document.querySelector('.chat-messages')
          let isAtBottom = true
          if (chatMessagesContainer) {
            const { scrollTop, scrollHeight, clientHeight } = chatMessagesContainer
            // 当滚动条距离底部小于20px时，认为用户正在查看最新消息
            isAtBottom = scrollHeight - scrollTop - clientHeight < 20
          }
          
          // 调用loadChatMessages时传递scrollToBottom参数
          await loadChatMessages(selectedChatUser.value.id, isAtBottom)
        }
      }
    } catch (error) {
      console.error('轮询失败:', error)
    }
  }, 3000)
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  console.log('组件挂载，路由参数:', route.query)
  await loadUserStats()
  // 总是加载聊天用户列表，以计算未读消息数量
  await loadChatUsers()
  // 加载未读消息数量
  await loadUnreadMessageCounts()
  // 确保activeTab被正确设置
  if (route.query.tab) {
    activeTab.value = route.query.tab
    console.log('设置activeTab为:', activeTab.value)
  } else if (route.query.userId) {
    // 如果有userId参数，自动切换到聊天标签
    activeTab.value = 'chat'
    console.log('有userId参数，设置activeTab为:', activeTab.value)
  } else {
    activeTab.value = 'message'
    console.log('默认设置activeTab为:', activeTab.value)
  }
  console.log('最终activeTab为:', activeTab.value)
  // 使用nextTick确保el-tabs组件能够正确响应activeTab的变化
  await nextTick()
  if (activeTab.value === 'chat') {
    console.log('当前是聊天标签')
    if (route.query.userId) {
      console.log('有userId参数，尝试选择用户')
      const targetUserId = parseInt(route.query.userId)
      let user = chatUsers.value.find(u => u.id == targetUserId)
      console.log('找到的用户:', user)
      if (user) {
        console.log('选择用户:', user)
        await selectChatUser(user)
      } else {
        // 如果用户不在列表中，添加一个默认用户
        console.log('用户不在列表中，添加默认用户')
        try {
          // 尝试获取用户的真实信息
          const userInfoResponse = await api.get(`/api/users/${targetUserId}`)
          console.log('获取到的用户信息:', userInfoResponse)
          const defaultUser = {
            id: targetUserId,
            nickname: userInfoResponse.nickname || '用户' + targetUserId,
            username: userInfoResponse.username || 'user' + targetUserId,
            avatar: userInfoResponse.avatar,
            lastMessage: '暂无消息',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0,
            isDefault: true // 标记为默认用户
          }
          chatUsers.value.unshift(defaultUser)
          console.log('添加默认用户后:', chatUsers.value)
          await selectChatUser(defaultUser)
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，使用默认昵称
          const defaultUser = {
            id: targetUserId,
            nickname: '用户' + targetUserId,
            username: 'user' + targetUserId,
            lastMessage: '暂无消息',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0,
            isDefault: true // 标记为默认用户
          }
          chatUsers.value.unshift(defaultUser)
          console.log('添加默认用户后:', chatUsers.value)
          await selectChatUser(defaultUser)
        }
      }
    }
  } else {
    console.log('当前不是聊天标签，加载消息列表')
    await loadMessages(activeTab.value)
  }
  // 启动轮询
  startPolling()
})

// 处理右键菜单事件
const handleContextMenu = (event, user) => {
  event.preventDefault()
  contextMenuUser.value = user
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
  
  // 点击其他地方关闭右键菜单
  setTimeout(() => {
    document.addEventListener('click', closeContextMenu)
  }, 0)
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  document.removeEventListener('click', closeContextMenu)
}

// 进入个人主页
const enterUserHome = (user) => {
  if (user) {
    window.open(`/user/${user.id}`, '_blank')
  }
  closeContextMenu()
}

// 置顶聊天
const pinChat = async (user) => {
  if (user) {
    const newPinnedState = !user.isPinned
    try {
      await api.put('/api/chat/pin', null, {
        params: {
          chatPartnerId: user.id,
          isPinned: newPinnedState
        }
      })
      user.isPinned = newPinnedState
      chatUsers.value.sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        const getTimeStamp = (time) => {
          if (!time) return 0
          try {
            if (time.includes('/')) {
              const parts = time.split(' ')
              const dateParts = parts[0].split('/')
              const timeParts = parts[1].split(':')
              const date = new Date(
                parseInt(dateParts[0]),
                parseInt(dateParts[1]) - 1,
                parseInt(dateParts[2]),
                parseInt(timeParts[0]),
                parseInt(timeParts[1]),
                parseInt(timeParts[2])
              )
              return date.getTime()
            }
            const date = new Date(time)
            return date.getTime()
          } catch (error) {
            console.error('时间解析错误:', error, '时间字符串:', time)
            return 0
          }
        }
        const timeA = getTimeStamp(a.lastMessageTime)
        const timeB = getTimeStamp(b.lastMessageTime)
        return timeB - timeA
      })
      ElMessage.success(newPinnedState ? '聊天已置顶' : '已取消置顶')
    } catch (error) {
      console.error('置顶失败:', error)
      ElMessage.error('置顶失败')
    }
  }
  closeContextMenu()
}

// 消息免打扰
const toggleDoNotDisturb = async (user) => {
  if (user) {
    const newDndState = !user.doNotDisturb
    try {
      await api.put('/api/chat/dnd', null, {
        params: {
          chatPartnerId: user.id,
          doNotDisturb: newDndState
        }
      })
      user.doNotDisturb = newDndState
      ElMessage.success(newDndState ? '已开启消息免打扰' : '已关闭消息免打扰')
    } catch (error) {
      console.error('设置免打扰失败:', error)
      ElMessage.error('设置失败')
    }
  }
  closeContextMenu()
}

// 加入黑名单/移出黑名单
const toggleBlacklist = async (user) => {
  if (!user) {
    closeContextMenu()
    return
  }
  
  if (user.isBlacklisted) {
    // 用户已在黑名单，移出
    try {
      await api.delete('/api/chat/blacklist', {
        params: { blockedUserId: user.id }
      })
      user.isBlacklisted = false
      ElMessage.success('已移出黑名单')
    } catch (error) {
      console.error('移出黑名单失败:', error)
      ElMessage.error('移出黑名单失败')
    }
  } else {
    // 用户不在黑名单，加入
    try {
      await ElMessageBox.confirm(
        `确定要将 ${user.nickname || user.username} 加入黑名单吗？加入后对方将无法给您发送消息`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await api.post('/api/chat/blacklist', null, {
        params: { blockedUserId: user.id }
      })
      user.isBlacklisted = true
      ElMessage.success('已加入黑名单')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('加入黑名单失败:', error)
        ElMessage.error('加入黑名单失败')
      }
    }
  }
  closeContextMenu()
}

// 删除对话
const deleteChat = (user) => {
  if (user) {
    ElMessageBox.confirm(
      `删除后将清空对话的消息记录`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        // 调用后端API删除对话
        await api.delete('/api/chat/conversation', {
          params: { partnerId: user.id }
        })

        // 从聊天列表中移除该用户
        chatUsers.value = chatUsers.value.filter(u => u.id !== user.id)

        // 如果当前正在和该用户聊天，清空聊天内容
        if (selectedChatUser.value && selectedChatUser.value.id === user.id) {
          selectedChatUser.value = null
          chatMessages.value = []
        }

        ElMessage.success('对话已删除')
      } catch (error) {
        console.error('删除对话失败:', error)
        ElMessage.error('删除对话失败')
      }
    }).catch(() => {
      // 取消操作
    })
  }
  closeContextMenu()
}

// 分页相关方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadMessages(activeTab.value)
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadMessages(activeTab.value)
}

// 组件卸载时停止轮询
onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
/* 全局样式 */
.message-center-container {
  min-height: 100vh;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 头像超链接 */
.avatar-link {
  text-decoration: none;
  color: inherit;
  display: inline-block;
}

.avatar-link:hover {
  text-decoration: none;
  color: inherit;
}

/* 用户名超链接 */
.username-link {
  text-decoration: none;
  color: inherit;
  display: inline-block;
}

.username-link:hover {
  text-decoration: none;
  color: #1677ff;
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
    justify-content: center;

    /* 左侧内容区 */
    .content-left {
      flex: 1;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      padding: 30px;
      max-width: 800px;

      /* 消息中心标题 */
      .message-center-title {
        margin: 0 0 20px 0;
        font-size: 24px;
        font-weight: 600;
        color: #333;
        text-align: left;
      }

      /* 消息标签页 */
      .message-tabs {
        margin-bottom: 20px;
      }
      
      /* 标签徽章样式 */
      .tab-with-badge {
        display: flex;
        align-items: center;
        gap: 4px;
      }
      
      .tab-with-badge .badge {
        margin-left: 4px;
      }

      /* 消息内容区域 */
      .message-content {
        /* 消息列表 */
        .message-list {
          .empty-message {
            text-align: center;
            padding: 60px 0;
          }

          .message-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            margin-bottom: 10px;
            background: #f5f7fa;
            border-radius: 8px;
            transition: all 0.3s ease;

            &:hover {
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            .message-info {
              flex: 1;

              .message-type {
                display: inline-block;
                padding: 2px 8px;
                border-radius: 10px;
                font-size: 12px;
                margin-bottom: 8px;

                &.comment,
                &.comment_replay {
                  background: #e6f7ff;
                  color: #1890ff;
                }

                &.like {
                  background: #f6ffed;
                  color: #52c41a;
                }

                &.follower {
                  background: #fff7e6;
                  color: #fa8c16;
                }

                &.system {
                  background: #f9f0ff;
                  color: #722ed1;
                }
              }

              .message-content-text {
                font-size: 14px;
                margin-bottom: 8px;
                line-height: 1.4;
                color: #333;
              }

              .message-time {
                font-size: 12px;
                color: #909399;
              }
            }

            .message-action {
              margin-left: 20px;
            }
          }

          /* 分页容器 */
          .pagination-container {
            margin-top: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
          }
        }

        /* 聊天界面 */
        .chat-container {
          display: flex;
          height: 600px;
          background: #f5f7fa;
          border-radius: 8px;
          overflow: hidden;

          .chat-sidebar {
            width: 300px;
            border-right: 1px solid #e4e7ed;
            background: #fff;
            padding: 15px;
            overflow-y: auto;

            .search-input {
              margin-bottom: 15px;
            }

            .empty-chat-users {
              text-align: center;
              padding: 60px 0;
            }

            .chat-user-card {
              display: flex;
              align-items: center;
              padding: 12px;
              margin-bottom: 10px;
              border-radius: 8px;
              cursor: pointer;
              transition: background-color 0.3s;

              &:hover {
                background-color: #f5f7fa;
              }

              &.active {
                background-color: #e6f7ff;
              }

              .chat-user-info {
                display: flex;
                align-items: center;
                flex: 1;

                .el-avatar {
                  margin-right: 12px;
                }

                .user-info {
                  flex: 1;

                  .user-name {
                    font-size: 14px;
                    font-weight: 500;
                    margin-bottom: 4px;
                    color: #333;
                  }

                  .last-message {
                    font-size: 12px;
                    color: #909399;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  }
                }
              }

              .chat-time {
                font-size: 12px;
                color: #909399;
                margin-right: 10px;
              }

              .unread-badge {
                background: #ff4d4f;
                color: #fff;
                font-size: 12px;
                padding: 2px 8px;
                border-radius: 10px;
                min-width: 20px;
                text-align: center;
              }
            }
          }

          .chat-content {
            flex: 1;
            display: flex;
            flex-direction: column;
            background: #fff;

            .empty-chat {
              flex: 1;
              display: flex;
              align-items: center;
              justify-content: center;
            }

            .chat-messages {
              flex: 1;
              padding: 20px;
              overflow-y: auto;

              .message-item-chat {
                margin-bottom: 15px;

                .message-received-container,
                .message-sent-container {
                  display: flex;
                  align-items: flex-start;
                }

                .message-sent-container {
                  justify-content: flex-end;
                }

                .message-avatar {
                  flex-shrink: 0;
                  margin: 0 8px;
                  margin-top: 5px;
                }

                .message-bubble {
                  max-width: 70%;
                  padding: 10px 14px;
                  border-radius: 18px;
                  position: relative;
                  word-break: break-word;

                  .message-content-text {
                    margin-bottom: 8px;
                    color: #333;
                    line-height: 1.4;
                  }

                  .message-time {
                    font-size: 12px;
                    color: #909399;
                    text-align: right;
                  }

                  &.received {
                    background-color: #f5f5f5;
                    border-bottom-left-radius: 4px;

                    &::before {
                      content: '';
                      position: absolute;
                      left: -8px;
                      top: 10px;
                      width: 0;
                      height: 0;
                      border-right: 8px solid #f5f5f5;
                      border-top: 8px solid transparent;
                      border-bottom: 8px solid transparent;
                    }
                  }

                  &.sent {
                    background-color: #07c160;
                    border-bottom-right-radius: 4px;

                    &::before {
                      content: '';
                      position: absolute;
                      right: -8px;
                      top: 10px;
                      width: 0;
                      height: 0;
                      border-left: 8px solid #07c160;
                      border-top: 8px solid transparent;
                      border-bottom: 8px solid transparent;
                    }

                    .message-content-text {
                      color: #fff;
                    }

                    .message-time {
                      color: rgba(255, 255, 255, 0.8);
                    }
                  }
                }
              }
            }

            .chat-input-area {
              display: flex;
              padding: 15px;
              border-top: 1px solid #e4e7ed;
              background: #f5f7fa;

              .chat-input {
                flex: 1;
                margin-right: 10px;
                border-radius: 4px;
              }

              .send-button {
                width: 100px;
                height: 100%;
              }
            }
          }
        }
      }
    }

    /* 右侧边栏 */
    .content-right {
      width: 300px;

      /* 用户信息卡片 */
      .user-card {
          border: 1px solid #f0f0f0;
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

          .card-header {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            padding: 12px 16px;
            border-bottom: 1px solid #f0f0f0;
            text-align: center;
          }

          .user-profile {
            padding: 20px;

            .author-info-center {
              display: flex;
              align-items: center;
              margin-bottom: 20px;
              justify-content: center;

              .user-avatar {
                margin-right: 15px;
                border: 3px solid #f0f0f0;
              }

              .user-name {
                font-size: 16px;
                font-weight: 600;
                color: #1677ff;
                margin: 0;
              }
            }

            .user-stats {
              display: grid;
              grid-template-columns: repeat(2, 1fr);
              grid-gap: 15px;

              .stat-item {
                text-align: center;

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
            
            .following-stats {
              margin-top: 20px;
              text-align: center;
              
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
        }
    }
  }
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  min-width: 150px;
  overflow: hidden;

  .context-menu-item {
    padding: 8px 16px;
    cursor: pointer;
    font-size: 14px;
    color: #333;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    &.in-blacklist {
      color: #E6A23C;
    }

    &.delete {
      color: #ff4d4f;

      &:hover {
        background-color: #fff1f0;
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

        .message-center-title {
          font-size: 20px;
        }

        .chat-container {
          height: 500px;

          .chat-sidebar {
            width: 250px;
          }

          .chat-content {
            .chat-messages {
              padding: 15px;

              .message-item-chat {
                .message-card {
                  max-width: 80%;
                }
              }
            }

            .chat-input-area {
              padding: 10px;

              .send-button {
                width: 80px;
              }
            }
          }
        }
      }
    }
  }
}
</style>