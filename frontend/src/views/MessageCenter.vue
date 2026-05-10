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
                  <el-dropdown-item command="profile" divided>个人中心</el-dropdown-item>
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
            <el-tab-pane name="group">
              <template #label>
                <div class="tab-with-badge">
                  <span>群聊</span>
                  <el-badge v-if="unreadGroupCount > 0" :value="unreadGroupCount > 99 ? '99+' : unreadGroupCount" class="badge" />
                </div>
              </template>
            </el-tab-pane>
          </el-tabs>

          <!-- 消息内容区域 -->
          <div class="message-content">
            <!-- 消息列表（我的消息、评论和@、新增粉丝、赞和收藏共用） -->
            <div v-if="activeTab === 'message' || activeTab === 'comment' || activeTab === 'follower' || activeTab === 'like'" class="message-list">
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
            <div v-else-if="activeTab === 'chat'" class="chat-container">
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
                            <div class="message-time">{{ formatDate(message.createdAt) }}</div>
                          </div>
                        </div>
                        <!-- 发送消息 -->
                        <div v-else class="message-sent-container">
                          <div class="message-bubble sent">
                            <div class="message-content-text">{{ message.content }}</div>
                            <div class="message-time">
                              <span>{{ formatDate(message.createdAt) }}</span>
                              <span :class="message.isRead ? 'read-status read' : 'read-status unread'">
                                {{ message.isRead ? '已读' : '未读' }}
                              </span>
                            </div>
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
                    @keyup.enter="sendMessage"
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

            <!-- 群聊界面 -->
            <div v-else-if="activeTab === 'group'" class="group-chat-wrapper">
              <div class="group-sidebar">
                <el-button @click="createGroup" type="primary" icon="el-icon-plus" class="create-group-btn">
                  创建群聊
                </el-button>
                <div v-if="groupChats.length === 0" class="empty-group-list">
                  <el-empty description="暂无群聊" />
                </div>
                <div v-else class="group-list">
                  <div v-for="group in groupChats" :key="group.id" @click="selectGroup(group)" :class="{ active: selectedGroup && selectedGroup.id === group.id }" class="group-item">
                    <el-avatar :size="44" :src="group.avatar" class="group-item-avatar">
                      <UserFilled />
                    </el-avatar>
                    <div class="group-item-info">
                      <div class="group-item-name">{{ group.name }}</div>
                      <div class="group-item-members">{{ group.memberCount }} 位成员</div>
                    </div>
                    <div class="group-item-time">{{ formatDate(group.createdAt) }}</div>
                    <div v-if="getGroupUnreadCount(group.id) > 0" class="group-item-unread">{{ getGroupUnreadCount(group.id) }}</div>
                  </div>
                </div>
              </div>
              <div class="group-chat-main">
                <div v-if="!selectedGroup" class="group-empty-state">
                  <el-empty description="请选择一个群聊开始聊天" />
                </div>
                <div v-else class="group-chat-area">
                  <div class="group-chat-header">
                    <el-avatar :size="44" :src="selectedGroup.avatar" class="header-avatar">
                      <UserFilled />
                    </el-avatar>
                    <div class="header-info">
                      <div class="header-title">{{ selectedGroup.name }}</div>
                      <div class="header-sub">{{ selectedGroup.memberCount }} 位成员</div>
                    </div>
                    <div class="header-actions">
                      <el-button @click="toggleGroupSettings" icon="el-icon-more" class="header-action-btn" circle />
                    </div>
                  </div>
                  <div ref="groupMessagesContainer" class="group-chat-messages">
                    <div v-if="groupMessages && groupMessages.length > 0">
                      <div v-for="(msg, idx) in groupMessages" :key="idx" :class="['message-wrapper', { 'msg-system-wrap': msg.senderId === 0, 'msg-sent': msg.senderId !== 0 && msg.senderId === currentUserId, 'msg-received': msg.senderId !== 0 && msg.senderId !== currentUserId }]">
                        <div v-if="msg.senderId === 0" class="msg-system">
                          <div class="msg-system-bubble">
                            <span class="msg-system-text">{{ msg.content }}</span>
                          </div>
                          <div class="msg-system-time">{{ formatDate(msg.createdAt) }}</div>
                        </div>
                        <div v-else>
                          <div v-if="msg.senderId !== currentUserId" class="msg-left">
                            <router-link :to="`/user/${msg.senderId}`" class="msg-avatar-link">
                              <el-avatar :size="40" :src="msg.senderAvatar">{{ msg.senderNickname?.charAt(0) || 'U' }}</el-avatar>
                            </router-link>
                            <div class="msg-content">
                              <div class="msg-bubble-left">
                                <span class="msg-text">{{ msg.content }}</span>
                              </div>
                              <div class="msg-time-left">{{ formatDate(msg.createdAt) }}</div>
                            </div>
                          </div>
                          <div v-else class="msg-right">
                            <div class="msg-content-right">
                              <div class="msg-bubble-right">
                                <span class="msg-text">{{ msg.content }}</span>
                              </div>
                              <div class="msg-time-right">{{ formatDate(msg.createdAt) }}</div>
                            </div>
                            <router-link :to="`/user/${currentUserId}`" class="msg-avatar-link">
                              <el-avatar :size="40" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
                            </router-link>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-else class="empty-messages">
                      <el-empty description="暂无消息" />
                    </div>
                  </div>
                  <div class="group-chat-input">
                    <el-input
                      v-model="groupMessageContent"
                      placeholder="输入消息..."
                      type="textarea"
                      :rows="2"
                      class="message-input"
                      @keyup.enter="sendGroupMessage"
                    />
                    <el-button type="primary" @click="sendGroupMessage" :disabled="!groupMessageContent.trim()" class="send-btn">发送</el-button>
                  </div>
                </div>
              </div>
              <!-- 群资料悬浮面板（覆盖在聊天框之上） -->
              <div v-if="selectedGroup && showGroupSettings" class="group-settings-overlay" @click.self="showGroupSettings = false">
                <div class="group-settings-panel">
                  <div class="settings-header">
                    <span class="settings-title">群资料</span>
                    <el-button @click="showGroupSettings = false" icon="el-icon-close" class="close-settings-btn" circle />
                  </div>
                  <div class="settings-content">
                    <div class="settings-group-info">
                      <el-avatar :size="80" :src="selectedGroup.avatar" class="settings-avatar">
                        <UserFilled />
                      </el-avatar>
                      <div class="settings-group-name">{{ selectedGroup.name }}</div>
                      <div class="settings-group-desc">{{ selectedGroup.description || '暂无描述' }}</div>
                    </div>
                    <div class="settings-search">
                      <el-input v-model="memberSearchKeyword" placeholder="搜索群成员" prefix-icon="el-icon-search" class="member-search-input" />
                    </div>
                    <div class="settings-members">
                      <div class="members-title">群成员 ({{ selectedGroup.memberCount }})</div>
                      <div class="members-list">
                        <div 
                          v-for="member in filteredGroupMembers" 
                          :key="member.id" 
                          class="member-box"
                          @contextmenu.prevent="showMemberMenu($event, member)"
                        >
                          <router-link :to="`/user/${member.id}`" class="member-avatar-link">
                            <el-avatar :size="50" :src="member.avatar || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${member.id}`">{{ member.nickname?.charAt(0) || member.username?.charAt(0) || 'U' }}</el-avatar>
                          </router-link>
                          <div class="member-name">{{ member.nickname || member.username }}</div>
                          <div v-if="member.id === selectedGroup.creatorId" class="member-badge">群主</div>
                          <div v-else-if="member.isAdmin" class="member-badge admin">管理员</div>
                        </div>
                        <div @click="openAddMembersDialog" class="member-box add-box">
                          <div class="add-icon-wrapper">+</div>
                          <div class="member-name">添加成员</div>
                        </div>
                      </div>
                    </div>
                    <div class="settings-announcement">
                      <div class="announcement-header">
                        <div class="announcement-title">📢 群公告</div>
                        <el-button type="text" class="edit-announcement-btn" @click="editAnnouncement">
                          <el-icon><Edit /></el-icon>
                        </el-button>
                      </div>
                      <div class="announcement-text">{{ groupAnnouncement || '暂无公告' }}</div>
                    </div>
                    <div class="settings-actions">
                      <el-button type="text" class="action-item" @click="editGroupInfo">编辑群资料</el-button>
                      <el-button type="text" class="action-item" @click="openAddMembersDialog">邀请成员</el-button>
                      <el-button type="text" class="action-item danger" @click="leaveGroup">退出群聊</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右键菜单 -->
        <div 
          v-if="contextMenuVisible" 
          class="context-menu"
          :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
          @click="contextMenuVisible = false"
        >
          <div v-if="canSetAdmin" class="context-menu-item" @click.stop="handleSetAdmin">
            <el-icon><Setting /></el-icon>
            <span>{{ contextMenuUser?.isAdmin ? '取消管理员' : '设为管理员' }}</span>
          </div>
          <div v-if="canRemoveMember" class="context-menu-item delete" @click.stop="handleRemoveMember">
            <el-icon><Delete /></el-icon>
            <span>移出群聊</span>
          </div>
          <div v-if="!isSelf" class="context-menu-item" @click.stop="handleFollow">
            <el-icon><Star /></el-icon>
            <span>{{ contextMenuUser?.isFollowed ? '取消关注' : '关注' }}</span>
          </div>
          <div v-if="!isSelf" class="context-menu-item" @click.stop="handleChat">
            <el-icon><Message /></el-icon>
            <span>私聊</span>
          </div>
          <div class="context-menu-item" @click.stop="handleViewProfile">
            <el-icon><UserFilled /></el-icon>
            <span>进入个人主页</span>
          </div>
        </div>

        <!-- 添加成员弹窗 -->
        <el-dialog v-model="showAddMembers" title="邀请成员" width="500px">
          <div class="add-members-content">
            <div class="search-input-wrapper">
              <el-input 
                v-model="addMemberSearchKeyword" 
                placeholder="搜索用户" 
                prefix-icon="el-icon-search"
                clearable
              />
            </div>
            <div class="available-users-list">
              <div 
                v-for="user in filteredAvailableUsers" 
                :key="user.id"
                class="user-item"
                :class="{ selected: selectedMemberIds.includes(user.id) }"
                @click="toggleMemberSelection(user.id)"
              >
                <el-avatar :size="40" :src="user.avatar || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${user.id}`">
                  {{ user.nickname?.charAt(0) || user.username?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-info">
                  <div class="user-name">{{ user.nickname || user.username }}</div>
                  <div class="user-username">@{{ user.username }}</div>
                </div>
                <div class="selection-icon">
                  <el-icon v-if="selectedMemberIds.includes(user.id)" class="check-icon">
                    <Check />
                  </el-icon>
                </div>
              </div>
              <div v-if="filteredAvailableUsers.length === 0" class="empty-state">
                <el-empty description="没有可添加的成员" />
              </div>
            </div>
            <div class="selection-summary">
              已选择 {{ selectedMemberIds.length }} 位成员
            </div>
          </div>
          <template #footer>
            <el-button @click="showAddMembers = false">取消</el-button>
            <el-button type="primary" @click="confirmAddMembers" :disabled="selectedMemberIds.length === 0">
              确定
            </el-button>
          </template>
        </el-dialog>

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
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api, { avatarApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowDown, UserFilled, Check, Edit, Setting, Delete, Star, Message } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 状态管理
const activeTab = ref('message')
const isInitialized = ref(false)
const isTabChanging = ref(false)
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
const unreadGroupCount = ref(0) // 群聊未读消息数量
const totalUnreadMessageCount = ref(0) // 总未读消息数量
// 群聊相关状态
const groupChats = ref([])
const selectedGroup = ref(null)
const groupMessages = ref([])
const groupMessagesContainer = ref(null)
const groupMessageContent = ref('')
const groupUnreadCounts = ref({}) // 各群聊未读消息数量
// 群资料面板状态
const showGroupSettings = ref(false)
const showAddMembers = ref(false)
const memberSearchKeyword = ref('')
const addMemberSearchKeyword = ref('')
const groupMembers = ref([])
const showAllMembers = ref(false)
const availableUsers = ref([])
const selectedMemberIds = ref([])
const groupAnnouncement = ref('')
const groupSettingsPosition = ref({ top: 0, right: 0 })
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

// 当前用户ID（响应式追踪登录状态变化）
const currentUserId = computed(() => Number(authStore.user?.id) || 1)

// 判断是否滚动到底部
const isScrollAtBottom = () => {
  const container = groupMessagesContainer.value || document.querySelector('.group-chat-messages')
  if (!container) return true
  const { scrollTop, scrollHeight, clientHeight } = container
  return scrollHeight - scrollTop - clientHeight < 50
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    const container = groupMessagesContainer.value || document.querySelector('.group-chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 100)
}

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
    
    // 过滤掉无效用户（没有id或没有昵称/用户名的用户）
    users = users.filter(user => {
      if (!user.id) {
        console.warn('过滤掉无效用户：没有id')
        return false
      }
      if (!user.nickname && !user.username) {
        console.warn('过滤掉无效用户：没有昵称和用户名，id:', user.id)
        return false
      }
      return true
    })
    
    console.log('获取到的聊天用户列表:', users)
    
    // 保留默认用户和手动添加的用户（仅保留未删除的且有效的用户）
    const defaultUsers = chatUsers.value.filter(user => (user.isDefault || user.isManual) && !user.deleted && user.id && (user.nickname || user.username))
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
            
            // 验证用户信息是否有效
            if (!userInfo || !userInfo.id) {
              console.warn('跳过无效的默认用户，id:', defaultUser.id)
              continue
            }
            
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
              isManual: defaultUser.isManual, // 保留手动添加标记
              deleted: false,
              isBlacklisted: isBlacklisted
            }
          users.push(updatedUser)
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，跳过该默认用户
          console.warn('跳过获取信息失败的默认用户，id:', defaultUser.id)
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
    
    // 按照最后消息时间排序，时间越近越靠前，置顶的用户排在最前面，手动添加的用户排在最前面
    users.sort((a, b) => {
      // 首先按手动添加状态排序，手动添加的排最前面
      if (a.isManual && !b.isManual) return -1
      if (!a.isManual && b.isManual) return 1
      // 然后按置顶状态排序，置顶的排前面
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
      
      // 只有在组件初始化阶段才能修改 activeTab
      // 防止初始化完成后用户点击其他 tab 时被覆盖
      if (!isInitialized.value) {
        activeTab.value = 'chat'
      }
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

// 加载群聊列表
const loadGroupChats = async () => {
  try {
    const response = await api.get('/api/group/my')
    const groups = response.data ? response.data : response || []
    groupChats.value = groups
    
    // 加载每个群的未读消息数量
    let totalGroupUnread = 0
    for (const group of groups) {
      try {
        const unreadResponse = await api.get(`/api/chat/group/${group.id}/unread/count`, {
          params: { userId: currentUserId.value }
        })
        const count = unreadResponse.data ? unreadResponse.data : unreadResponse
        groupUnreadCounts.value[group.id] = count
        totalGroupUnread += count
      } catch (e) {
        groupUnreadCounts.value[group.id] = 0
      }
    }
    unreadGroupCount.value = totalGroupUnread
  } catch (error) {
    console.error('加载群聊列表失败:', error)
    groupChats.value = []
  }
}

// 获取群聊未读消息数量
const getGroupUnreadCount = (groupId) => {
  return groupUnreadCounts.value[groupId] || 0
}

// 选择群聊
const selectGroup = async (group) => {
  selectedGroup.value = group
  selectedChatUser.value = null
  showGroupSettings.value = false
  
  groupAnnouncement.value = group.announcement || ''
  
  try {
    // 并行加载群消息、群成员和标记已读
    const [messagesResponse, membersResponse, readResponse] = await Promise.all([
      api.get(`/api/chat/group/${group.id}/messages`),
      api.get(`/api/group/${group.id}/members`),
      api.post(`/api/chat/group/${group.id}/read`, null, {
        params: { userId: currentUserId.value }
      })
    ])
    
    const messages = messagesResponse || []
    groupMembers.value = membersResponse || []
    
    // 为每条消息添加发送者头像和昵称
    const processedMessages = await Promise.all(messages.map(async (message) => {
      if (message.senderId === currentUserId) {
          return {
            ...message,
            senderAvatar: authStore.user?.avatar,
            senderNickname: authStore.user?.nickname || authStore.user?.username
          }
        } else {
        try {
          const userResponse = await api.get(`/api/users/${message.senderId}`)
          const user = userResponse.data || userResponse
          return {
            ...message,
            senderAvatar: user?.avatar,
            senderNickname: user?.nickname || user?.username
          }
        } catch (e) {
          console.error('获取用户信息失败:', e)
          return {
            ...message,
            senderAvatar: null,
            senderNickname: '未知用户'
          }
        }
      }
    }))
    
    groupMessages.value = processedMessages
    
    // 重新加载未读计数
    await loadGroupChats()
    
    setTimeout(() => {
      const chatMessagesContainer = document.querySelector('.chat-messages')
      if (chatMessagesContainer) {
        chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight
      }
    }, 100)
  } catch (error) {
    console.error('加载群聊消息失败:', error)
    groupMessages.value = []
  }
}

// 发送群消息
const sendGroupMessage = async () => {
  if (!selectedGroup.value || !groupMessageContent.value.trim()) return
  
  try {
    const response = await api.post('/api/chat/group', {
      groupId: selectedGroup.value.id,
      content: groupMessageContent.value.trim()
    })
    
    if (response) {
      const messageWithAvatar = {
        ...response,
        senderId: currentUserId.value,
        senderAvatar: authStore.user?.avatar,
        senderNickname: authStore.user?.nickname || authStore.user?.username
      }
      groupMessages.value.push(messageWithAvatar)
      groupMessageContent.value = ''
      ElMessage.success('发送成功')
      
      scrollToBottom()
      
      await api.post(`/api/chat/group/${selectedGroup.value.id}/read`, null, {
        params: { userId: currentUserId.value }
      })
      
      await loadGroupChats()
    }
  } catch (error) {
    console.error('发送群消息失败:', error)
    ElMessage.error('发送消息失败：' + (error.message || '网络异常'))
  }
}

// 创建群聊
const createGroup = async () => {
  try {
    const response = await api.get('/api/users')
    const usersResponse = response.data || response
    const availableUsers = usersResponse.filter(u => u.id !== currentUserId)
    
    if (availableUsers.length === 0) {
      ElMessage.warning('没有可选择的成员')
      return
    }
    
    const selectedMemberIds = ref([])
    const groupName = ref('')
    const groupDescription = ref('')
    
    const toggleMember = (userId) => {
      const index = selectedMemberIds.value.indexOf(userId)
      if (index > -1) {
        selectedMemberIds.value.splice(index, 1)
      } else {
        selectedMemberIds.value.push(userId)
      }
    }
    
    const updateGroupName = (e) => {
      groupName.value = e.target.value
    }
    
    const updateDescription = (e) => {
      groupDescription.value = e.target.value
    }
    
    const renderMemberItem = (user) => {
      const isSelected = selectedMemberIds.value.includes(user.id)
      return h('div', {
        class: 'group-member-item',
        style: {
          display: 'flex',
          alignItems: 'center',
          padding: '8px 12px',
          borderRadius: '20px',
          backgroundColor: isSelected ? '#10b981' : '#f5f7fa',
          cursor: 'pointer',
          transition: 'all 0.3s',
          border: isSelected ? 'none' : '1px solid #e4e7ed'
        },
        onClick: () => toggleMember(user.id)
      }, [
        h('img', {
          src: user.avatar || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${user.id}`,
          style: { width: '24px', height: '24px', borderRadius: '50%', marginRight: '8px' }
        }),
        h('span', { style: { color: isSelected ? '#fff' : '#333', fontSize: '13px' } }, user.nickname || user.username)
      ])
    }
    
    const { h } = await import('vue')
    
    const component = {
      setup() {
        return () => h('div', { style: { width: '500px', maxHeight: '400px', overflowY: 'auto', padding: '10px' } }, [
          h('div', { style: { marginBottom: '20px' } }, [
            h('div', { style: { fontSize: '14px', fontWeight: '500', marginBottom: '10px' } }, '请选择群成员'),
            h('div', { style: { display: 'flex', flexWrap: 'wrap', gap: '10px' } }, 
              availableUsers.map(renderMemberItem)
            )
          ]),
          h('div', { style: { marginBottom: '15px' } }, [
            h('div', { style: { fontSize: '14px', fontWeight: '500', marginBottom: '8px' } }, '群聊名称'),
            h('input', {
              type: 'text',
              value: groupName.value,
              placeholder: '请输入群聊名称',
              style: {
                width: '100%',
                padding: '10px',
                border: '1px solid #d9d9d9',
                borderRadius: '4px',
                fontSize: '14px'
              },
              onInput: updateGroupName
            })
          ]),
          h('div', [
            h('div', { style: { fontSize: '14px', fontWeight: '500', marginBottom: '8px' } }, '群聊描述（可选）'),
            h('textarea', {
              value: groupDescription.value,
              placeholder: '请输入群聊描述',
              style: {
                width: '100%',
                padding: '10px',
                border: '1px solid #d9d9d9',
                borderRadius: '4px',
                fontSize: '14px',
                minHeight: '60px',
                resize: 'vertical'
              },
              onInput: updateDescription
            })
          ]),
          h('div', { style: { marginTop: '15px', color: '#666', fontSize: '12px' } }, 
            `已选择 ${selectedMemberIds.value.length} 位成员`)
        ])
      }
    }
    
    await ElMessageBox({
      title: '创建群聊',
      message: h(component),
      showCancelButton: true,
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      beforeClose: (action, instance, done) => {
        if (action === 'confirm') {
          if (!selectedMemberIds.value.length) {
            ElMessage.warning('请至少选择一位成员')
            done(false)
            return
          }
          if (!groupName.value.trim()) {
            ElMessage.warning('请输入群聊名称')
            done(false)
            return
          }
          done()
        } else {
          done()
        }
      }
    })
    
    await api.post('/api/group', {
      name: groupName.value.trim(),
      description: groupDescription.value?.trim() || '',
      memberIds: selectedMemberIds.value
    })
    
    ElMessage.success('群聊创建成功')
    await loadGroupChats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('创建群聊失败:', error)
      ElMessage.error('创建群聊失败：' + (error.message || '操作取消'))
    }
  }
}

// 切换群资料面板
const toggleGroupSettings = async () => {
  showGroupSettings.value = !showGroupSettings.value
  if (showGroupSettings.value && selectedGroup.value) {
    await loadGroupMembers(selectedGroup.value.id)
  }
}

// 过滤群成员
const filteredGroupMembers = computed(() => {
  let members = groupMembers.value
  console.log('搜索关键词:', memberSearchKeyword.value) // 调试信息
  console.log('群成员列表:', members) // 调试信息
  if (memberSearchKeyword.value) {
    const keyword = memberSearchKeyword.value.toLowerCase()
    members = members.filter(m => 
      (m.nickname && m.nickname.toLowerCase().includes(keyword)) ||
      (m.username && m.username.toLowerCase().includes(keyword))
    )
  }
  console.log('过滤后成员:', members) // 调试信息
  return showAllMembers.value ? members : members.slice(0, 12)
})

// 过滤可添加的成员
const filteredAvailableUsers = computed(() => {
  let users = availableUsers.value
  if (addMemberSearchKeyword.value) {
    const keyword = addMemberSearchKeyword.value.toLowerCase()
    users = users.filter(u => 
      (u.nickname && u.nickname.toLowerCase().includes(keyword)) ||
      (u.username && u.username.toLowerCase().includes(keyword))
    )
  }
  return users
})

// 加载群成员
const loadGroupMembers = async (groupId) => {
  try {
    const response = await api.get(`/api/group/${groupId}/members`)
    const members = response || []
    console.log('加载群成员:', members) // 调试信息
    groupMembers.value = members
  } catch (error) {
    console.error('加载群成员失败:', error)
    groupMembers.value = []
  }
}

// 编辑群资料
const editGroupInfo = async () => {
  try {
    const { value: newName } = await ElMessageBox.prompt(
      '请输入新的群聊名称',
      '编辑群资料',
      {
        inputValue: selectedGroup.value?.name || '',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    if (!newName?.trim()) {
      ElMessage.warning('群聊名称不能为空')
      return
    }
    
    const { value: newDesc } = await ElMessageBox.prompt(
      '请输入群聊描述',
      '群聊描述',
      {
        inputValue: selectedGroup.value?.description || '',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await api.put(`/api/group/${selectedGroup.value.id}`, {
      name: newName.trim(),
      description: newDesc?.trim() || ''
    })
    
    selectedGroup.value.name = newName.trim()
    selectedGroup.value.description = newDesc?.trim() || ''
    ElMessage.success('群资料修改成功')
    await loadGroupChats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('编辑群资料失败:', error)
      ElMessage.error('编辑群资料失败：' + (error.message || '操作取消'))
    }
  }
}

// 编辑群公告
const editAnnouncement = async () => {
  try {
    const { value: newAnnouncement } = await ElMessageBox.prompt(
      '请输入群公告内容',
      '编辑群公告',
      {
        inputValue: groupAnnouncement.value || '',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入群公告内容'
      }
    )
    
    await api.put(`/api/group/${selectedGroup.value.id}/announcement`, {
      announcement: newAnnouncement?.trim() || ''
    })
    
    groupAnnouncement.value = newAnnouncement?.trim() || ''
    ElMessage.success('群公告修改成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('编辑群公告失败:', error)
      ElMessage.error('编辑群公告失败：' + (error.message || '操作取消'))
    }
  }
}

// 打开添加成员弹窗
const openAddMembersDialog = async () => {
  try {
    const response = await api.get('/api/users')
    const usersResponse = response.data || response
    availableUsers.value = usersResponse.filter(u => 
      u.id !== currentUserId && 
      !groupMembers.value.some(m => m.id === u.id)
    )
    
    if (availableUsers.value.length === 0) {
      ElMessage.warning('没有可添加的成员')
      return
    }
    
    selectedMemberIds.value = []
    addMemberSearchKeyword.value = ''
    showAddMembers.value = true
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
}

// 切换成员选择
const toggleMemberSelection = (userId) => {
  const idx = selectedMemberIds.value.indexOf(userId)
  if (idx > -1) {
    selectedMemberIds.value.splice(idx, 1)
  } else {
    selectedMemberIds.value.push(userId)
  }
}

// 确认添加成员
const confirmAddMembers = async () => {
  try {
    const groupId = selectedGroup.value.id
    await api.post(`/api/group/${groupId}/members`, {
      memberIds: selectedMemberIds.value
    })
    
    ElMessage.success('邀请成功')
    showAddMembers.value = false
    await loadGroupMembers(groupId)
    await loadGroupChats()
    
    const updatedGroup = groupChats.value.find(g => g.id === groupId)
    if (updatedGroup) {
      selectedGroup.value = updatedGroup
    }
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error('添加成员失败：' + (error.message || '未知错误'))
  }
}

// 显示成员右键菜单
const showMemberMenu = (event, member) => {
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuUser.value = member
  contextMenuVisible.value = true
  
  document.addEventListener('click', closeContextMenu)
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  document.removeEventListener('click', closeContextMenu)
}

// 设为管理员/取消管理员
const handleSetAdmin = async () => {
  if (!contextMenuUser.value || !selectedGroup.value) return
  
  try {
    const userId = contextMenuUser.value.id
    const groupId = selectedGroup.value.id
    
    if (contextMenuUser.value.isAdmin) {
      const response = await api.delete(`/api/group/${groupId}/admin/${userId}`)
      if (response && response.data && response.data.code !== 0) {
        ElMessage.error(response.data.message || '取消管理员失败')
      } else {
        ElMessage.success('已取消管理员权限')
      }
    } else {
      const response = await api.post(`/api/group/${groupId}/admin/${userId}`)
      if (response && response.data && response.data.code !== 0) {
        ElMessage.error(response.data.message || '设置失败')
      } else {
        ElMessage.success('已设置为管理员')
      }
    }
    
    await loadGroupMembers(groupId)
    contextMenuVisible.value = false
  } catch (error) {
    console.error('管理员操作失败:', error)
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  }
}

// 移出群聊
const handleRemoveMember = async () => {
  if (!contextMenuUser.value || !selectedGroup.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要将 ${contextMenuUser.value.nickname || contextMenuUser.value.username} 移出群聊吗？`,
      '移出群聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const userId = contextMenuUser.value.id
    const groupId = selectedGroup.value.id
    
    await api.delete(`/api/group/${groupId}/members/${userId}`)
    
    ElMessage.success('已移出群聊')
    await loadGroupMembers(groupId)
    await loadGroupChats()
    contextMenuVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移出成员失败:', error)
      ElMessage.error('移出失败：' + (error.message || '操作取消'))
    }
  }
}

// 关注/取消关注
const handleFollow = async () => {
  if (!contextMenuUser.value) return
  
  try {
    const userId = contextMenuUser.value.id
    const isFollowed = contextMenuUser.value.isFollowed
    
    if (isFollowed) {
      await api.delete(`/api/${userId}/follow`)
      ElMessage.success('已取消关注')
      contextMenuUser.value.isFollowed = false
    } else {
      await api.post(`/api/${userId}/follow`)
      ElMessage.success('关注成功')
      contextMenuUser.value.isFollowed = true
    }
    
    contextMenuVisible.value = false
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  }
}

// 私聊
const handleChat = async () => {
  if (!contextMenuUser.value) return
  
  showGroupSettings.value = false
  selectedGroup.value = null
  
  const userId = contextMenuUser.value.id
  const user = {
    id: userId,
    nickname: contextMenuUser.value.nickname || contextMenuUser.value.username,
    avatar: contextMenuUser.value.avatar,
    unreadCount: 0
  }
  
  // 从群聊点击私聊时，将用户放到聊天列表第一位
  if (chatUsers.value) {
    const index = chatUsers.value.findIndex(u => u.id === userId)
    if (index !== -1) {
      // 用户已在列表中，移到第一位
      if (index !== 0) {
        const [selected] = chatUsers.value.splice(index, 1)
        chatUsers.value.unshift(selected)
      }
    } else {
      // 用户不在列表中，添加到第一位，并标记为手动添加
      chatUsers.value.unshift({
        ...user,
        isManual: true
      })
    }
  }
  
  activeTab.value = 'chat'
  
  // 直接加载消息，不调用 selectChatUser 避免重新加载列表
  selectedChatUser.value = user
  
  // 如果有未读消息，标记为已读
  const existingUser = chatUsers.value?.find(u => u.id === userId)
  if (existingUser && existingUser.unreadCount > 0) {
    try {
      await api.put(`/api/chat/read?senderId=${userId}`)
      existingUser.unreadCount = 0
    } catch (error) {
      console.error('标记消息为已读失败:', error)
    }
  }
  
  await loadChatMessages(userId)
  contextMenuVisible.value = false
}

// 进入个人主页
const handleViewProfile = () => {
  if (!contextMenuUser.value) return
  
  router.push(`/user/${contextMenuUser.value.id}`)
  contextMenuVisible.value = false
}

// 判断是否可以设置管理员
const canSetAdmin = computed(() => {
  if (!contextMenuUser.value || !selectedGroup.value) return false
  if (contextMenuUser.value.id === selectedGroup.value.creatorId) return false
  return authStore.user?.id === selectedGroup.value.creatorId
})

// 判断是否可以移除成员
const canRemoveMember = computed(() => {
  if (!contextMenuUser.value || !selectedGroup.value) return false
  if (contextMenuUser.value.id === selectedGroup.value.creatorId) return false
  if (contextMenuUser.value.id === authStore.user?.id) return false
  return authStore.user?.id === selectedGroup.value.creatorId || contextMenuUser.value.isAdmin === false
})

// 判断是否是自己
const isSelf = computed(() => {
  if (!contextMenuUser.value) return false
  return contextMenuUser.value.id === authStore.user?.id
})

// 退出群聊
const leaveGroup = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出该群聊吗？退出后将无法接收群消息',
      '退出群聊',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.delete(`/api/group/${selectedGroup.value.id}/members/me`)
    
    ElMessage.success('已退出群聊')
    showGroupSettings.value = false
    selectedGroup.value = null
    await loadGroupChats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出群聊失败:', error)
      ElMessage.error('退出群聊失败：' + (error.message || '操作取消'))
    }
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
  // 如果正在切换标签，忽略此次点击
  if (isTabChanging.value) {
    console.log('标签切换中，忽略此次点击')
    return
  }
  
  console.log('===================== 标签点击事件 =====================')
  console.log('点击的标签:', tab)
  console.log('标签的name:', tab.props.name)
  
  // 设置切换标志
  isTabChanging.value = true
  
  try {
    activeTab.value = tab.props.name
    console.log('activeTab.value:', activeTab.value)
    if (activeTab.value === 'chat') {
      console.log('当前是聊天标签，加载聊天用户列表')
      await loadChatUsers()
    } else if (activeTab.value === 'group') {
      console.log('当前是群聊标签，加载群聊列表')
      await loadGroupChats()
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
  } finally {
    // 清除切换标志
    isTabChanging.value = false
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
    case 'profile':
      router.push(`/user/${authStore.user?.id}`)
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
      
      // 群聊轮询逻辑
      if (activeTab.value === 'group') {
        await loadGroupChats()
        // 如果已选择群聊，检查是否有新消息
        if (selectedGroup.value) {
          const oldMessageCount = groupMessages.value.length
          const response = await api.get(`/api/chat/group/${selectedGroup.value.id}/messages`)
          const messages = response.data ? response.data : response || []
          
          // 如果有新消息
          if (messages.length > oldMessageCount) {
            // 只有用户当前在底部才自动滚动
            if (isScrollAtBottom()) {
              const processedMessages = await Promise.all(messages.slice(oldMessageCount).map(async (message) => {
                if (message.senderId === currentUserId.value) {
                  return {
                    ...message,
                    senderAvatar: authStore.user?.avatar,
                    senderNickname: authStore.user?.nickname || authStore.user?.username
                  }
                } else {
                  try {
                    const userResponse = await api.get(`/api/users/${message.senderId}`)
                    const user = userResponse.data || userResponse
                    return {
                      ...message,
                      senderAvatar: user?.avatar,
                      senderNickname: user?.nickname || user?.username
                    }
                  } catch (e) {
                    return {
                      ...message,
                      senderAvatar: null,
                      senderNickname: '未知用户'
                    }
                  }
                }
              }))
              groupMessages.value.push(...processedMessages)
              scrollToBottom()
            }
          }
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
  
  // 标记初始化完成
  isInitialized.value = true
  
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
        // 如果是临时用户（数据库中不存在的用户），直接从前端删除
        if (user.isDefault) {
          chatUsers.value = chatUsers.value.filter(u => u.id !== user.id)
          if (selectedChatUser.value && selectedChatUser.value.id === user.id) {
            selectedChatUser.value = null
            chatMessages.value = []
          }
          ElMessage.success('对话已删除')
          return
        }
        
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
      overflow: hidden;

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
              scrollbar-width: thin;
              scrollbar-color: #d9d9d9 #f5f5f5;
              
              &::-webkit-scrollbar {
                width: 6px;
              }
              
              &::-webkit-scrollbar-track {
                background: #f5f5f5;
              }
              
              &::-webkit-scrollbar-thumb {
                background-color: #d9d9d9;
                border-radius: 3px;
              }
              
              &::-webkit-scrollbar-thumb:hover {
                background-color: #bfbfbf;
              }

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
                  padding: 12px 16px;
                  border-radius: 20px;
                  position: relative;
                  word-break: break-word;
                  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

                  .message-content-text {
                    margin-bottom: 6px;
                    color: #333;
                    line-height: 1.5;
                    font-size: 15px;
                  }

                  .message-time {
                    font-size: 11px;
                    color: #999;
                    text-align: right;
                    display: flex;
                    align-items: center;
                    justify-content: flex-end;
                    gap: 6px;
                    opacity: 0.8;
                    
                    &::before {
                      content: '';
                      display: inline-block;
                      width: 12px;
                      height: 12px;
                      background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cpolyline points='12 6 12 12 16 14'/%3E%3C/svg%3E") no-repeat center;
                      background-size: 12px 12px;
                    }

                    .read-status {
                      padding: 1px 6px;
                      border-radius: 10px;
                      font-size: 10px;
                      font-weight: 500;
                      
                      &.read {
                        color: #10b981;
                        background: rgba(16, 185, 129, 0.15);
                      }
                      
                      &.unread {
                        color: #f59e0b;
                        background: rgba(245, 158, 11, 0.15);
                      }
                    }
                  }

                  &.received {
                    background-color: #fff;
                    border-bottom-left-radius: 6px;
                    border: 1px solid #e8e8e8;

                    &::before {
                      content: '';
                      position: absolute;
                      left: -10px;
                      top: 12px;
                      width: 0;
                      height: 0;
                      border-right: 10px solid #fff;
                      border-top: 6px solid transparent;
                      border-bottom: 6px solid transparent;
                      border-right-color: #e8e8e8;
                    }

                    &::after {
                      content: '';
                      position: absolute;
                      left: -8px;
                      top: 12px;
                      width: 0;
                      height: 0;
                      border-right: 10px solid #fff;
                      border-top: 6px solid transparent;
                      border-bottom: 6px solid transparent;
                    }
                  }

                  &.sent {
                    background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
                    border-bottom-right-radius: 6px;
                    box-shadow: 0 4px 12px rgba(7, 193, 96, 0.3);

                    &::before {
                      content: '';
                      position: absolute;
                      right: -10px;
                      top: 12px;
                      width: 0;
                      height: 0;
                      border-left: 10px solid #10b981;
                      border-top: 6px solid transparent;
                      border-bottom: 6px solid transparent;
                    }

                    .message-content-text {
                      color: #fff;
                    }

                    .message-time {
                      color: rgba(255, 255, 255, 0.7);
                      
                      &::before {
                        background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='rgba(255,255,255,0.7)' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cpolyline points='12 6 12 12 16 14'/%3E%3C/svg%3E") no-repeat center;
                        background-size: 12px 12px;
                      }

                      .read-status {
                        &.read {
                          color: rgba(255, 255, 255, 0.9);
                          background: rgba(255, 255, 255, 0.2);
                        }
                        
                        &.unread {
                          color: rgba(255, 255, 255, 0.9);
                          background: rgba(245, 158, 11, 0.3);
                        }
                      }
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

/* 群聊布局 */
.group-chat-wrapper {
  display: flex;
  height: 600px;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  
  /* 左侧群列表 */
  .group-sidebar {
    width: 300px;
    background: #fff;
    border-right: 1px solid #e4e7ed;
    display: flex;
    flex-direction: column;
    
    .create-group-btn {
      margin: 12px;
    }
    
    .empty-group-list {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .group-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
      
      .group-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: background 0.2s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.active {
          background: #e6f7ff;
        }
        
        .group-item-avatar {
          margin-right: 12px;
        }
        
        .group-item-info {
          flex: 1;
          min-width: 0;
          
          .group-item-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
          }
          
          .group-item-members {
            font-size: 12px;
            color: #999;
          }
        }
        
        .group-item-time {
          font-size: 12px;
          color: #999;
          margin-right: 8px;
        }
        
        .group-item-unread {
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
  }
  
  /* 中间聊天区域 */
  .group-chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #f5f7fa;
    
    .group-empty-state {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .group-chat-area {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: #fff;
      min-height: 0;
      
      .group-chat-header {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        background: #fff;
        border-bottom: 1px solid #e8e8e8;
        
        .header-avatar {
          margin-right: 12px;
        }
        
        .header-info {
          flex: 1;
          
          .header-title {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin-bottom: 2px;
          }
          
          .header-sub {
            font-size: 12px;
            color: #999;
          }
        }
        
        .header-actions {
          .header-action-btn {
            color: #666;
            
            &:hover {
              background: #f5f7fa;
              color: #333;
            }
          }
        }
      }
      
      .group-chat-messages {
        flex: 1;
        padding: 20px;
        overflow-y: auto;
        background: #fafafa;
        scrollbar-width: thin;
        scrollbar-color: #d9d9d9 #f5f5f5;
        
        &::-webkit-scrollbar {
          width: 6px;
        }
        
        &::-webkit-scrollbar-track {
          background: #f5f5f5;
        }
        
        &::-webkit-scrollbar-thumb {
          background-color: #d9d9d9;
          border-radius: 3px;
        }
        
        &::-webkit-scrollbar-thumb:hover {
          background-color: #bfbfbf;
        }
        
        .message-wrapper {
          margin-bottom: 20px;
          display: flex;
          
          &.msg-system-wrap {
            justify-content: center;
            
            .msg-system {
              display: flex;
              flex-direction: column;
              align-items: center;
              width: 100%;
              max-width: 60%;
              
              .msg-system-bubble {
                background: #f5f5f5;
                padding: 8px 16px;
                border-radius: 16px;
                text-align: center;
                
                .msg-system-text {
                  font-size: 13px;
                  color: #666;
                  line-height: 1.5;
                }
              }
              
              .msg-system-time {
                font-size: 11px;
                color: #999;
                margin-top: 4px;
              }
            }
          }
          
          &.msg-received {
            justify-content: flex-start;
            
            .msg-left {
              display: flex;
              align-items: flex-start;
              
              .msg-avatar-link {
                margin-right: 12px;
              }
              
              .msg-content {
                max-width: 70%;
                
                .msg-sender-name {
                  font-size: 12px;
                  color: #999;
                  margin-bottom: 4px;
                  margin-left: 8px;
                }
                
                .msg-bubble-left {
                  background: #fff;
                  padding: 12px 16px;
                  border-radius: 0 12px 12px 12px;
                  border: 1px solid #e8e8e8;
                  box-shadow: 0 2px 4px rgba(0,0,0,0.04);
                  
                  .msg-text {
                    font-size: 15px;
                    color: #333;
                    line-height: 1.5;
                  }
                }
                
                .msg-time-left {
                  font-size: 11px;
                  color: #999;
                  margin-top: 4px;
                  margin-left: 8px;
                }
              }
            }
          }
          
          &.msg-sent {
            justify-content: flex-end;
            
            .msg-right {
              display: flex;
              align-items: flex-start;
              
              .msg-content-right {
                max-width: 70%;
                text-align: right;
                
                .msg-bubble-right {
                  background: #10b981;
                  padding: 12px 16px;
                  border-radius: 12px 0 12px 12px;
                  
                  .msg-text {
                    font-size: 15px;
                    color: #fff;
                    line-height: 1.5;
                  }
                }
                
                .msg-time-right {
                  font-size: 11px;
                  color: #999;
                  margin-top: 4px;
                }
              }
              
              .msg-avatar-link {
                margin-left: 12px;
              }
            }
          }
        }
        
        .empty-messages {
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      
      .group-chat-input {
        padding: 12px 16px;
        background: #fff;
        border-top: 1px solid #e8e8e8;
        display: flex;
        gap: 12px;
        
        .message-input {
          flex: 1;
        }
        
        .send-btn {
          width: 80px;
          height: 42px;
        }
      }
    }
  }
  
  /* 群资料悬浮面板（覆盖在聊天框之上） */
  .group-settings-overlay {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 280px;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1000;
    display: flex;
    justify-content: flex-end;
    align-items: stretch;
    
    .group-settings-panel {
      width: 360px;
      background: #fff;
      display: flex;
      flex-direction: column;
      box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
      
      .settings-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border-bottom: 1px solid #e8e8e8;
        background: #fafafa;
        
        .settings-title {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }
        
        .close-settings-btn {
          color: #999;
          
          &:hover {
            color: #666;
            background: #f0f0f0;
          }
        }
      }
      
      .settings-content {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        
        .settings-group-info {
          text-align: center;
          margin-bottom: 20px;
          padding-bottom: 20px;
          border-bottom: 1px solid #f0f0f0;
          
          .settings-avatar {
            margin-bottom: 12px;
            border: 4px solid #f0f0f0;
          }
          
          .settings-group-name {
            font-size: 18px;
            font-weight: 600;
            color: #333;
            margin-bottom: 4px;
          }
          
          .settings-group-desc {
            font-size: 13px;
            color: #999;
          }
        }
        
        .settings-search {
          margin-bottom: 16px;
          
          .member-search-input {
            width: 100%;
          }
        }
        
        .settings-members {
          margin-bottom: 20px;
          
          .members-title {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 12px;
          }
          
          .members-list {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 16px;
            
            .member-box {
              display: flex;
              flex-direction: column;
              align-items: center;
              cursor: pointer;
              padding: 8px;
              border-radius: 8px;
              transition: background 0.2s;
              
              &:hover {
                background: #f5f7fa;
              }
              
              .member-avatar-link {
                margin-bottom: 8px;
                
                .el-avatar {
                  border: 2px solid #f0f0f0;
                }
              }
              
              .member-name {
                font-size: 12px;
                color: #333;
                text-align: center;
                max-width: 100%;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                line-height: 1.4;
              }
              
              .member-badge {
                font-size: 10px;
                color: #10b981;
                background: rgba(16, 185, 129, 0.1);
                padding: 2px 6px;
                border-radius: 10px;
                margin-top: 4px;
              }
              
              &.add-box {
                .add-icon-wrapper {
                  width: 50px;
                  height: 50px;
                  border-radius: 50%;
                  border: 2px dashed #d9d9d9;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-size: 24px;
                  color: #999;
                  margin-bottom: 8px;
                  
                  &:hover {
                    border-color: #10b981;
                    color: #10b981;
                  }
                }
              }
            }
          }
        }
        
        .settings-announcement {
          margin-bottom: 20px;
          padding: 16px;
          background: #fffbe6;
          border-radius: 8px;
          
          .announcement-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
          }
          
          .announcement-title {
            font-size: 14px;
            font-weight: 600;
            color: #333;
          }
          
          .edit-announcement-btn {
            font-size: 12px;
            color: #1890ff;
            padding: 0;
            
            &:hover {
              color: #40a9ff;
              background: transparent;
            }
          }
          
          .announcement-text {
            font-size: 13px;
            color: #666;
            line-height: 1.5;
          }
        }
        
        .settings-actions {
          display: flex;
          flex-direction: column;
          gap: 8px;
          padding-top: 16px;
          border-top: 1px solid #f0f0f0;
          
          .action-item {
            color: #666;
            justify-content: flex-start;
            padding-left: 0;
            padding: 10px 12px;
            border-radius: 6px;
            
            &:hover {
              color: #1677ff;
              background: #f0f5ff;
            }
            
            &.danger {
              color: #ff4d4f;
              
              &:hover {
                color: #ff7875;
                background: #fff1f0;
              }
            }
          }
        }
      }
    }
  }
}

/* 添加成员弹窗样式 */
.add-members-content {
  .search-input-wrapper {
    margin-bottom: 16px;
  }
  
  .available-users-list {
    max-height: 300px;
    overflow-y: auto;
    
    .user-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s;
      margin-bottom: 8px;
      border: 2px solid transparent;
      
      &:hover {
        background: #f5f7fa;
      }
      
      &.selected {
        background: #f0f5ff;
        border-color: #1677ff;
      }
      
      .user-info {
        flex: 1;
        margin-left: 12px;
        
        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 2px;
        }
        
        .user-username {
          font-size: 12px;
          color: #999;
        }
      }
      
      .selection-icon {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .check-icon {
          color: #1677ff;
          font-size: 20px;
        }
      }
    }
    
    .empty-state {
      padding: 40px 0;
    }
  }
  
  .selection-summary {
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    text-align: center;
    color: #666;
    font-size: 14px;
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