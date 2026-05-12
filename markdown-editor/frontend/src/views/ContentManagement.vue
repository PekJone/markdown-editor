<template>
  <div class="content-management-container">
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
                  <el-dropdown-item command="profile" divided>个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
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
            <h3>管理</h3>
          </div>
          <div class="category-list">
            <el-menu :default-active="activeMenu" @select="handleMenuSelect">
              <el-sub-menu index="management">
                <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>管理</span>
                </template>
                <el-menu-item index="content">
                  <el-icon><Document /></el-icon>
                  <span>内容管理</span>
                </el-menu-item>
                <el-menu-item index="comments">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>评论管理</span>
                </el-menu-item>
                <el-menu-item index="tags">
                  <el-icon><Grid /></el-icon>
                  <span>标签管理</span>
                </el-menu-item>
                <el-menu-item index="labels">
                    <el-icon><UserFilled /></el-icon>
                    <span>粉丝管理</span>
                  </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="data">
                <template #title>
                  <el-icon><DataAnalysis /></el-icon>
                  <span>数据</span>
                </template>
                <el-menu-item index="works">
                  <el-icon><DocumentChecked /></el-icon>
                  <span>作品数据</span>
                </el-menu-item>
                <el-menu-item index="income">
                  <el-icon><Money /></el-icon>
                  <span>收益数据</span>
                </el-menu-item>
                <el-menu-item index="followers">
                  <el-icon><UserFilled /></el-icon>
                  <span>粉丝数据</span>
                </el-menu-item>
                <el-menu-item index="summary">
                  <el-icon><Calendar /></el-icon>
                  <span>一周小结</span>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="personal-center">
                <template #title>
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </template>
                <el-menu-item index="profile">
                  <el-icon><UserFilled /></el-icon>
                  <span>个人资料</span>
                </el-menu-item>
                <el-menu-item index="address">
                  <el-icon><MapLocation /></el-icon>
                  <span>地址管理</span>
                </el-menu-item>
                <el-menu-item index="account">
                  <el-icon><Setting /></el-icon>
                  <span>账号设置</span>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </div>
        </el-aside>

        <el-main>
          <div class="content-management-content">
            <div class="management-content">
              <!-- 内容管理 -->
              <div v-if="activeMenu === 'content'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="content-list">
                    <!-- 内容列表 -->
                    <el-table :data="contents" style="width: 100%" v-loading="loading" empty-text="你还没有文章，快去写一篇吧">
                      <el-table-column prop="title" label="标题" min-width="400">
                        <template #default="{ row }">
                          <div class="article-title">
                            <router-link :to="`/document/${row.id}`" class="title-text">{{ row.title }}</router-link>
                          </div>
                          <div class="article-time">{{ row.updatedAt }}</div>
                        </template>
                      </el-table-column>
                      <el-table-column label="浏览" width="80" align="center">
                        <template #default="{ row }">
                          {{ row.viewCount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="点赞" width="80" align="center">
                        <template #default="{ row }">
                          {{ row.likeCount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="评论" width="80" align="center">
                        <template #default="{ row }">
                          {{ row.commentCount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="收藏" width="80" align="center">
                        <template #default="{ row }">
                          {{ row.collectionCount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="" width="200" fixed="right">
                        <template #default="{ row }">
                          <el-popover
                            placement="top"
                            :width="400"
                            trigger="click"
                          >
                            <template #reference>
                              <el-button link size="small">数据</el-button>
                            </template>
                            <div class="document-stats-panel">
                              <h3>{{ row.title }}</h3>
                              <div class="stats-grid">
                                <div class="stat-item">
                                  <span class="stat-label">浏览量</span>
                                  <span class="stat-value">{{ row.viewCount || 0 }}</span>
                                </div>
                                <div class="stat-item">
                                  <span class="stat-label">点赞量</span>
                                  <span class="stat-value">{{ row.likeCount || 0 }}</span>
                                </div>
                                <div class="stat-item">
                                  <span class="stat-label">评论量</span>
                                  <span class="stat-value">{{ row.commentCount || 0 }}</span>
                                </div>
                                <div class="stat-item">
                                  <span class="stat-label">收藏量</span>
                                  <span class="stat-value">{{ row.collectionCount || 0 }}</span>
                                </div>
                              </div>
                            </div>
                          </el-popover>
                          <el-button link type="primary" size="small" @click="editDocument(row.id)">编辑</el-button>
                          <el-button link size="small" @click="viewDocument(row.id)">浏览</el-button>
                          <el-dropdown>
                            <el-button link size="small">
                              <el-icon><More /></el-icon>
                            </el-button>
                            <template #dropdown>
                              <el-dropdown-menu>
                                <el-dropdown-item @click="deleteDocument(row.id)">删除</el-dropdown-item>
                              </el-dropdown-menu>
                            </template>
                          </el-dropdown>
                        </template>
                      </el-table-column>
                    </el-table>
                    <!-- 分页控件 -->
                    <div class="pagination-container" v-if="total > 0">
                      <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :page-sizes="[10, 20, 50, 100]"
                        layout="total, sizes, prev, pager, next, jumper"
                        :total="total"
                        @size-change="handleSizeChange"
                        @current-change="handlePageChange"
                      />
                    </div>
                  </div>
                </el-card>
              </div>

              <!-- 评论管理 -->
              <div v-if="activeMenu === 'comments'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="comments-list">
                    <!-- 评论标签页 -->
                    <el-tabs v-model="activeCommentTab" @tab-click="handleCommentTabClick">
                      <el-tab-pane label="我的评论" name="my">
                        <el-table :data="myComments" style="width: 100%" v-loading="loadingComments" empty-text="暂无数据">
                          <el-table-column label="评论内容" min-width="400">
                            <template #default="{ row }">
                              <div class="comment-content" @click="navigateToDocument(row.documentId)">
                                {{ row.content }}
                              </div>
                            </template>
                          </el-table-column>
                          <el-table-column label="评论文章" width="200">
                            <template #default="{ row }">
                              <div class="document-title" @click="navigateToDocument(row.documentId)">
                                {{ row.documentTitle }}
                              </div>
                            </template>
                          </el-table-column>
                          <el-table-column prop="createdAt" label="评论时间" width="180" />
                          <el-table-column label="操作" width="100" fixed="right">
                            <template #default="{ row }">
                              <el-button link type="danger" size="small" @click="deleteComment(row.id)">删除</el-button>
                            </template>
                          </el-table-column>
                        </el-table>
                        <!-- 分页控件 -->
                        <div class="pagination-container" v-if="myCommentsTotal > 0">
                          <el-pagination
                            v-model:current-page="commentCurrentPage"
                            v-model:page-size="commentPageSize"
                            :page-sizes="[10, 20, 50, 100]"
                            layout="total, sizes, prev, pager, next, jumper"
                            :total="myCommentsTotal"
                            @size-change="handleCommentSizeChange"
                            @current-change="handleCommentPageChange"
                          />
                        </div>
                      </el-tab-pane>
                      <el-tab-pane label="收到的评论" name="received">
                        <el-table :data="receivedComments" style="width: 100%" v-loading="loadingComments" empty-text="暂无数据">
                          <el-table-column label="评论内容" min-width="400">
                            <template #default="{ row }">
                              <div class="comment-content" @click="navigateToDocument(row.documentId)">
                                {{ row.content }}
                              </div>
                            </template>
                          </el-table-column>
                          <el-table-column prop="nickname" label="评论者" width="150" />
                          <el-table-column label="评论文章" width="200">
                            <template #default="{ row }">
                              <div class="document-title" @click="navigateToDocument(row.documentId)">
                                {{ row.documentTitle }}
                              </div>
                            </template>
                          </el-table-column>
                          <el-table-column prop="createdAt" label="评论时间" width="180" />
                          <el-table-column label="操作" width="100" fixed="right">
                            <template #default="{ row }">
                              <el-button link type="danger" size="small" @click="deleteComment(row.id)">删除</el-button>
                            </template>
                          </el-table-column>
                        </el-table>
                        <!-- 分页控件 -->
                        <div class="pagination-container" v-if="receivedCommentsTotal > 0">
                          <el-pagination
                            v-model:current-page="commentCurrentPage"
                            v-model:page-size="commentPageSize"
                            :page-sizes="[10, 20, 50, 100]"
                            layout="total, sizes, prev, pager, next, jumper"
                            :total="receivedCommentsTotal"
                            @size-change="handleCommentSizeChange"
                            @current-change="handleCommentPageChange"
                          />
                        </div>
                      </el-tab-pane>
                    </el-tabs>
                  </div>
                </el-card>
              </div>

              <!-- 标签管理 -->
              <div v-if="activeMenu === 'tags'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                      <el-button type="primary" size="small" @click="createTag">
                        <el-icon><Plus /></el-icon>
                        新建标签
                      </el-button>
                    </div>
                  </template>
                  <div class="tags-list">
                    <!-- 标签列表 -->
                    <el-table :data="tags.slice((currentTagPage - 1) * tagPageSize, currentTagPage * tagPageSize)" style="width: 100%" empty-text="暂无标签">
                      <el-table-column prop="tagName" label="标签名称" min-width="200" />
                      <el-table-column label="标签分类" width="150">
                        <template #default="{ row }">
                          {{ getCategoryName(row.category) }}
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="150" fixed="right">
                        <template #default="{ row }">
                          <el-button link type="primary" size="small" @click="editTag(row)">编辑</el-button>
                          <el-button link type="danger" size="small" @click="deleteTag(row.id)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                    <!-- 分页组件 -->
                    <div class="pagination-container" style="margin-top: 20px;">
                      <el-pagination
                        v-model:current-page="currentTagPage"
                        v-model:page-size="tagPageSize"
                        :page-sizes="[5, 10, 20, 50]"
                        layout="total, sizes, prev, pager, next, jumper"
                        :total="totalTags"
                        @size-change="handleTagSizeChange"
                        @current-change="handleTagPageChange"
                      />
                    </div>
                  </div>
                </el-card>

                <!-- 标签编辑对话框 -->
                <el-dialog
                  v-model="dialogVisible"
                  :title="dialogTitle"
                  width="500px"
                >
                  <el-form :model="form" label-width="80px">
                    <el-form-item label="标签名称">
                      <el-input v-model="form.tagName" placeholder="请输入标签名称" />
                    </el-form-item>
                    <el-form-item label="标签分类">
                      <el-select v-model="form.category" placeholder="请选择标签分类">
                        <el-option
                          v-for="category in categories"
                          :key="category.value"
                          :label="category.label"
                          :value="category.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-form>
                  <template #footer>
                    <span class="dialog-footer">
                      <el-button @click="dialogVisible = false">取消</el-button>
                      <el-button type="primary" @click="saveTag">保存</el-button>
                    </span>
                  </template>
                </el-dialog>
              </div>

              <!-- 粉丝管理 -->
              <div v-if="activeMenu === 'labels'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <!-- 标签切换 -->
                  <el-tabs v-model="activeFollowerTab">
                    <el-tab-pane label="我的粉丝" name="followers">
                      <div class="followers-list">
                        <!-- 我的粉丝列表 -->
                        <el-table :data="myFollowers" style="width: 100%" empty-text="暂无粉丝">
                          <el-table-column prop="username" label="用户名" width="200" />
                          <el-table-column prop="nickname" label="昵称" width="200" />
                          <el-table-column label="关注时间" width="200">
                          <template #default="{ row }">
                            {{ formatDate(row.createdAt) }}
                          </template>
                        </el-table-column>
                          <el-table-column label="操作" width="400" fixed="right" align="left">
                            <template #default="{ row }">
                              <div style="display: flex; gap: 10px; align-items: center;">
                                <el-button link type="primary" size="small" style="min-width: 50px; text-align: left;" @click="viewUser(row.id)">查看</el-button>
                                <el-button link type="info" size="small" @click="sendMessage(row.id)" style="min-width: 50px; text-align: left;">私信</el-button>
                                <div style="min-width: 80px;">
                                  <el-button v-if="!row.isFollowing" link type="success" size="small" @click="followUser(row.id)" style="width: 100%; text-align: left;">回关</el-button>
                                  <el-button v-else link type="info" size="small" @click="unfollowUser(row.id)" style="width: 100%; text-align: left;">取消关注</el-button>
                                </div>
                                <el-button link type="warning" size="small" @click="removeFollower(row.id)" style="min-width: 50px; text-align: left;">移除</el-button>
                                <div style="min-width: 80px;">
                                  <el-button v-if="!row.isBlocked" link type="danger" size="small" @click="blockUser(row.id)" style="width: 100%; text-align: left;">拉黑</el-button>
                                  <el-button v-else link type="success" size="small" @click="unblockUser(row.id)" style="width: 100%; text-align: left;">取消拉黑</el-button>
                                </div>
                              </div>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="我的关注" name="following">
                      <div class="following-list">
                        <!-- 我的关注列表 -->
                        <el-table :data="myFollowing" style="width: 100%" empty-text="暂无关注">
                          <el-table-column prop="username" label="用户名" width="200" />
                          <el-table-column prop="nickname" label="昵称" width="200" />
                          <el-table-column label="关注时间" width="200">
                          <template #default="{ row }">
                            {{ formatDate(row.createdAt) }}
                          </template>
                        </el-table-column>
                          <el-table-column label="操作" width="350" fixed="right" align="left">
                            <template #default="{ row }">
                              <div style="display: flex; gap: 10px; align-items: center;">
                                <el-button link type="primary" size="small" style="min-width: 50px; text-align: left;" @click="viewUser(row.id)">查看</el-button>
                                <el-button link type="info" size="small" @click="sendMessage(row.id)" style="min-width: 50px; text-align: left;">私信</el-button>
                                <el-button link type="danger" size="small" @click="unfollowUser(row.id)" style="min-width: 80px; text-align: left;">取消关注</el-button>
                                <div style="min-width: 80px;">
                                  <el-button v-if="!row.isBlocked" link type="danger" size="small" @click="blockUser(row.id)" style="width: 100%; text-align: left;">拉黑</el-button>
                                  <el-button v-else link type="success" size="small" @click="unblockUser(row.id)" style="width: 100%; text-align: left;">取消拉黑</el-button>
                                </div>
                              </div>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="好友" name="friends">
                      <div class="friends-list">
                        <!-- 好友列表 -->
                        <el-table :data="myFriends" style="width: 100%" empty-text="暂无好友">
                          <el-table-column prop="username" label="用户名" width="200" />
                          <el-table-column prop="nickname" label="昵称" width="200" />
                          <el-table-column label="成为好友时间" width="200">
                          <template #default="{ row }">
                            {{ formatDate(row.createdAt) }}
                          </template>
                        </el-table-column>
                          <el-table-column label="操作" width="300" fixed="right" align="left">
                            <template #default="{ row }">
                              <div style="display: flex; gap: 10px; align-items: center;">
                                <el-button link type="primary" size="small" style="min-width: 50px; text-align: left;" @click="viewUser(row.id)">查看</el-button>
                                <el-button link type="info" size="small" @click="sendMessage(row.id)" style="min-width: 50px; text-align: left;">私信</el-button>
                                <el-button link type="danger" size="small" @click="unfollowUser(row.id)" style="min-width: 80px; text-align: left;">取消关注</el-button>
                                <div style="min-width: 80px;">
                                  <el-button v-if="!row.isBlocked" link type="danger" size="small" @click="blockUser(row.id)" style="width: 100%; text-align: left;">拉黑</el-button>
                                  <el-button v-else link type="success" size="small" @click="unblockUser(row.id)" style="width: 100%; text-align: left;">取消拉黑</el-button>
                                </div>
                              </div>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="黑名单" name="blacklist">
                      <div class="blacklist-list">
                        <!-- 黑名单列表 -->
                        <el-table :data="myBlacklist" style="width: 100%" empty-text="暂无黑名单">
                          <el-table-column prop="username" label="用户名" width="200" />
                          <el-table-column prop="nickname" label="昵称" width="200" />
                          <el-table-column label="拉黑时间" width="200">
                          <template #default="{ row }">
                            {{ formatDate(row.createdAt) }}
                          </template>
                        </el-table-column>
                          <el-table-column label="操作" width="200" fixed="right" align="left">
                            <template #default="{ row }">
                              <div style="display: grid; grid-template-columns: 50px 70px; gap: 10px; align-items: center;">
                                <el-button link type="primary" size="small" style="width: 50px; text-align: left;">查看</el-button>
                                <el-button link type="success" size="small" @click="unblockUser(row.id)" style="width: 70px; text-align: left;">取消拉黑</el-button>
                              </div>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
                </el-card>
              </div>

              <!-- 作品数据 -->
              <div v-if="activeMenu === 'works'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="works-data-grid">
                    <div class="stat-item">
                      <div class="stat-title">文章总数</div>
                      <div class="stat-value">{{ workStats.totalArticles }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-title">点赞数</div>
                      <div class="stat-value">{{ workStats.totalLikes }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-title">评论数</div>
                      <div class="stat-value">{{ workStats.totalComments }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-title">浏览量</div>
                      <div class="stat-value">{{ workStats.totalViews }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-title">收藏数</div>
                      <div class="stat-value">{{ workStats.totalCollections }}</div>
                    </div>
                  </div>
                </el-card>

                <!-- 趋势图部分 -->
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="trend-tabs">
                    <!-- 标签切换 -->
                    <el-tabs v-model="activeTrendTab">
                      <el-tab-pane label="趋势图" name="chart">
                        <div class="trend-info">
                          数据统计只支持此功能上线后的数据变化展示
                        </div>
                        
                        <!-- 时间范围选择 -->
                        <div class="time-range">
                          <el-button 
                            size="small" 
                            :type="activeTimeRange === '7days' ? 'primary' : ''"
                            @click="selectTimeRange('7days')">近7天</el-button>
                          <el-button 
                            size="small" 
                            :type="activeTimeRange === '30days' ? 'primary' : ''"
                            @click="selectTimeRange('30days')">近1月</el-button>
                          <el-date-picker
                            v-model="dateRange"
                            type="daterange"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期"
                            size="small"
                            @change="handleDateRangeChange"
                            :picker-options="datePickerOptions"
                          />
                        </div>
                        
                        <!-- 数据类型选择 -->
                        <div class="data-types">
                          <el-checkbox-group v-model="selectedDataTypes">
                            <el-checkbox label="浏览量">浏览量</el-checkbox>
                            <el-checkbox label="评论数">评论数</el-checkbox>
                            <el-checkbox label="粉丝数">粉丝数</el-checkbox>
                            <el-checkbox label="收藏数">收藏数</el-checkbox>
                          </el-checkbox-group>
                        </div>
                        
                        <!-- 趋势图表 -->
                        <div class="chart-container">
                          <div class="chart-wrapper">
                            <!-- Y轴 -->
                            <div class="y-axis">
                              <div class="y-label">{{ maxValue }}</div>
                              <div class="y-label">{{ Math.round(maxValue * 0.875) }}</div>
                              <div class="y-label">{{ Math.round(maxValue * 0.75) }}</div>
                              <div class="y-label">{{ Math.round(maxValue * 0.625) }}</div>
                              <div class="y-label">{{ Math.round(maxValue * 0.5) }}</div>
                              <div class="y-label">{{ Math.round(maxValue * 0.375) }}</div>
                              <div class="y-label">{{ Math.round(maxValue * 0.25) }}</div>
                              <div class="y-label">0</div>
                            </div>
                            
                            <!-- 图表区域 -->
                            <div class="chart-area">
                              <!-- 网格线 -->
                              <div class="grid-lines">
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                                <div class="grid-line"></div>
                              </div>
                              
                              <!-- 折线图 -->
                              <svg class="line-chart" viewBox="0 0 800 200" preserveAspectRatio="none">
                                <!-- 动态折线 -->
                                <template v-for="(type, typeIndex) in selectedDataTypes" :key="'line-' + type">
                                  <path 
                                    :d="getLinePath(type)" 
                                    fill="none" 
                                    :stroke="getColor(type)" 
                                    stroke-width="2"
                                    stroke-linecap="round"
                                    stroke-linejoin="round"
                                  />
                                  <!-- 数据点 -->
                                  <g v-for="(point, index) in getChartPoints(type)" :key="'point-group-' + typeIndex + '-' + index">
                                    <!-- 透明的大点击区域 -->
                                    <rect 
                                      :x="point.x - 10" 
                                      :y="point.y - 10" 
                                      width="20" 
                                      height="20" 
                                      fill="transparent"
                                      @mouseenter="showTooltip(type, point.x, point.y, index)"
                                      @mouseleave="hideTooltip"
                                    />
                                    <!-- 实际显示的数据点 -->
                                    <circle 
                                      :cx="point.x" 
                                      :cy="point.y" 
                                      r="4" 
                                      :fill="getColor(type)" 
                                      class="data-point"
                                    />
                                    <!-- 悬停时显示的高亮圈 -->
                                    <circle 
                                      :cx="point.x" 
                                      :cy="point.y" 
                                      r="8" 
                                      :fill="getColor(type)" 
                                      fill-opacity="0.3"
                                      class="data-point-hover"
                                    />
                                  </g>
                                </template>
                              </svg>
                              
                              <!-- 悬停提示框 -->
                              <div 
                                v-if="tooltipVisible" 
                                class="chart-tooltip"
                                :style="{ 
                                  left: tooltipData.x + 'px', 
                                  top: tooltipData.y + 'px',
                                  borderColor: getColor(tooltipData.type)
                                }"
                              >
                                <div class="tooltip-date">{{ tooltipData.date }}</div>
                                <div class="tooltip-content">
                                  <span class="tooltip-type">{{ tooltipData.type }}</span>
                                  <span class="tooltip-value">{{ tooltipData.value }}</span>
                                </div>
                              </div>
                              
                              <!-- X轴 -->
                              <div class="x-axis">
                                <div class="x-label" v-for="(item, index) in trendDataList" :key="'x-' + index">
                                  {{ formatDate(item.date) }}
                                </div>
                              </div>
                            </div>
                            
                            <!-- 图例 -->
                            <div class="chart-legend">
                              <div 
                                v-for="type in selectedDataTypes" 
                                :key="'legend-' + type"
                                class="legend-item"
                              >
                                <span class="legend-color" :style="{ backgroundColor: getColor(type) }"></span>
                                <span class="legend-text">{{ type }}</span>
                                <span class="legend-value">{{ getTotal(type) }}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </el-tab-pane>
                      <el-tab-pane label="数据列表" name="list">
                        <div class="trend-info">
                          数据统计只支持此功能上线后的数据变化展示
                        </div>
                        
                        <!-- 时间范围选择 -->
                        <div class="time-range">
                          <el-button 
                            size="small" 
                            :type="activeTimeRange === '7days' ? 'primary' : ''"
                            @click="selectTimeRange('7days')">近7天</el-button>
                          <el-button 
                            size="small" 
                            :type="activeTimeRange === '30days' ? 'primary' : ''"
                            @click="selectTimeRange('30days')">近1月</el-button>
                          <el-date-picker
                            v-model="dateRange"
                            type="daterange"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期"
                            size="small"
                            @change="handleDateRangeChange"
                            :picker-options="datePickerOptions"
                          />
                        </div>
                        
                        <!-- 数据类型选择 -->
                        <div class="data-types">
                          <el-checkbox-group v-model="selectedDataTypes">
                            <el-checkbox label="浏览量">浏览量</el-checkbox>
                            <el-checkbox label="评论数">评论数</el-checkbox>
                            <el-checkbox label="粉丝数">粉丝数</el-checkbox>
                            <el-checkbox label="收藏数">收藏数</el-checkbox>
                          </el-checkbox-group>
                        </div>
                        
                        <!-- 数据列表 -->
                        <div class="data-list-container">
                          <el-table :data="trendDataList" style="width: 100%" empty-text="暂无数据">
                            <el-table-column prop="date" label="日期" width="150" />
                            <el-table-column 
                              v-for="type in selectedDataTypes" 
                              :key="'col-' + type"
                              :prop="fieldMap[type]" 
                              :label="type" 
                              width="100" 
                            />
                          </el-table>
                        </div>
                      </el-tab-pane>
                    </el-tabs>
                  </div>
                </el-card>
              </div>

              <!-- 收益数据 -->
              <div v-if="activeMenu === 'income'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="income-data">
                    <el-statistic :value="incomeStats.totalIncome" title="总收入" />
                    <el-statistic :value="incomeStats.monthlyIncome" title="本月收入" />
                    <el-statistic :value="incomeStats.dailyIncome" title="今日收入" />
                  </div>
                </el-card>
              </div>

              <!-- 粉丝数据 -->
              <div v-if="activeMenu === 'followers'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="followers-data">
                    <el-statistic :value="followerStats.totalFollowers" title="总粉丝数" />
                    <el-statistic :value="followerStats.monthlyGrowth" title="本月增长" />
                    <el-statistic :value="followerStats.dailyGrowth" title="今日增长" />
                  </div>
                </el-card>
              </div>

              <!-- 一周小结 -->
              <div v-if="activeMenu === 'summary'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="summary-data">
                    <el-statistic :value="summaryStats.weekArticles" title="本周发文" />
                    <el-statistic :value="summaryStats.weekViews" title="本周浏览" />
                    <el-statistic :value="summaryStats.weekLikes" title="本周点赞" />
                    <el-statistic :value="summaryStats.weekFollowers" title="本周新增粉丝" />
                  </div>
                </el-card>
              </div>

              <!-- 个人资料 -->
              <div v-if="activeMenu === 'profile'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="profile-section">
                    <h3 class="section-title">基本信息</h3>
                    <el-divider></el-divider>
                    <!-- 头像 -->
                    <div class="info-item">
                      <span class="info-label">头像</span>
                      <div class="avatar-wrapper" @click="triggerAvatarUpload">
                        <img 
                          :src="userAvatar" 
                          alt="用户头像"
                          class="user-avatar-inline"
                        />
                        <div class="avatar-overlay">
                          <span class="upload-icon">+</span>
                          <span class="upload-text">更换头像</span>
                        </div>
                      </div>
                      <input 
                        type="file" 
                        id="avatar-upload" 
                        class="avatar-upload-input"
                        accept="image/*"
                        @change="handleAvatarUpload"
                      />
                    </div>
                    <div class="info-item">
                      <span class="info-label">用户昵称</span>
                      <template v-if="editingNickname">
                        <el-input 
                          v-model="editNickname" 
                          class="edit-input"
                          @keyup.enter="saveNickname"
                        />
                        <el-button link type="success" @click="saveNickname">保存</el-button>
                        <el-button link @click="cancelEditNickname">取消</el-button>
                      </template>
                      <template v-else>
                        <span class="info-value">{{ authStore.user?.nickname || '未设置' }}</span>
                        <el-button link type="primary" @click="startEditNickname">编辑</el-button>
                      </template>
                    </div>
                    <div class="info-item">
                      <span class="info-label">用户ID</span>
                      <span class="info-value">{{ authStore.user?.id || '未设置' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">性别</span>
                      <span class="info-value">{{ authStore.user?.gender || '未设置' }}</span>
                      <button class="edit-btn" @click="startEdit('gender')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">个人简介</span>
                      <span class="info-value">{{ authStore.user?.bio || '未填写' }}</span>
                      <button class="edit-btn" @click="startEdit('bio')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">所在地区</span>
                      <span class="info-value">{{ authStore.user?.location || '未设置' }}</span>
                      <button class="edit-btn" @click="startEdit('location')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">收货地址</span>
                      <div v-if="selectedAddress" class="address-display">
                        <div class="address-content">
                          <div class="address-display-header">
                            <span class="address-display-name">{{ selectedAddress.name }}</span>
                            <span class="address-display-phone">{{ selectedAddress.phone }}</span>
                            <span v-if="selectedAddress.isDefault === 1" class="address-display-default">默认</span>
                          </div>
                          <div class="address-display-detail">
                            {{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detail }}
                          </div>
                        </div>
                        <button class="edit-btn" @click="showAddressPickerModal = true">选择</button>
                      </div>
                      <el-button v-else link type="primary" @click="openAddModal">+ 新增收货地址</el-button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">出生日期</span>
                      <span class="info-value">{{ formatDate(authStore.user?.birthDate) || '未设置' }}</span>
                      <button class="edit-btn" @click="startEdit('birthDate')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">开始工作</span>
                      <span class="info-value">{{ formatDate(authStore.user?.startWorkDate) || '未选择' }}</span>
                      <button class="edit-btn" @click="startEdit('startWorkDate')">编辑</button>
                    </div>
                    
                    <h3 class="section-title">教育信息</h3>
                    <el-divider></el-divider>
                    <div class="info-item">
                      <span class="info-label">学校名称</span>
                      <span class="info-value">{{ authStore.user?.school || '未填写' }}</span>
                      <button class="edit-btn" @click="startEdit('school')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">专业</span>
                      <span class="info-value">{{ authStore.user?.major || '未填写' }}</span>
                      <button class="edit-btn" @click="startEdit('major')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">入学时间</span>
                      <span class="info-value">{{ formatDate(authStore.user?.enrollmentDate) || '未选择' }}</span>
                      <button class="edit-btn" @click="startEdit('enrollmentDate')">编辑</button>
                    </div>
                    <div class="info-item">
                      <span class="info-label">学历</span>
                      <span class="info-value">{{ authStore.user?.education || '未选择' }}</span>
                      <button class="edit-btn" @click="startEdit('education')">编辑</button>
                    </div>
                  </div>
                </el-card>
              </div>

              <!-- 地址管理 -->
              <div v-if="activeMenu === 'address'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                      <el-button type="primary" @click="openAddModal" class="add-btn">+ 新增收货地址</el-button>
                    </div>
                  </template>
                  <div class="address-management">
                    <div class="address-list" v-if="addresses.length > 0">
                      <div 
                        v-for="address in addresses" 
                        :key="address.id" 
                        class="address-card"
                        :class="{ 'default': address.isDefault === 1 }"
                      >
                        <div class="address-header">
                          <div class="user-info">
                            <span class="name">{{ address.name }}</span>
                            <span class="phone">{{ address.phone }}</span>
                          </div>
                          <span v-if="address.isDefault === 1" class="default-tag">默认地址</span>
                        </div>
                        <div class="address-detail">
                          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
                        </div>
                        <div class="address-actions">
                          <button v-if="address.isDefault !== 1" @click="setDefault(address.id)" class="action-btn default-btn">设为默认</button>
                          <button @click="editAddress(address)" class="action-btn edit-btn">编辑</button>
                          <button @click="deleteAddress(address.id)" class="action-btn delete-btn">删除</button>
                        </div>
                      </div>
                    </div>
                    <div class="empty-state" v-else>
                      <div class="empty-icon">📦</div>
                      <p>暂无收货地址</p>
                      <el-button type="primary" @click="openAddModal">添加第一个地址</el-button>
                    </div>
                    
                    <!-- 分页组件 -->
                    <div v-if="addresses.length > 0" class="pagination-container">
                      <el-pagination
                        background
                        layout="prev, pager, next, jumper, ->, total"
                        :total="addressTotal"
                        :page-size="addressPageSize"
                        :current-page="addressCurrentPage"
                        @current-change="handleAddressPageChange"
                      />
                    </div>
                  </div>
                </el-card>
              </div>

              <!-- 地址管理弹窗 -->
              <div class="modal-overlay" v-if="showAddModal || showEditModal" @click.self="closeModal">
                <div class="modal">
                  <div class="modal-header">
                    <h3>{{ isEditing ? '编辑地址' : '新增地址' }}</h3>
                    <span class="close-btn" @click="closeModal">×</span>
                  </div>
                  <div class="modal-body">
                    <form @submit.prevent="submitAddress">
                      <div class="form-group">
                        <label>收货人</label>
                        <input 
                          v-model="formData.name" 
                          type="text" 
                          placeholder="请输入收货人姓名"
                          required
                        />
                      </div>
                      <div class="form-group">
                        <label>联系电话</label>
                        <input 
                          v-model="formData.phone" 
                          type="tel" 
                          placeholder="请输入联系电话"
                          required
                        />
                      </div>
                      <div class="form-group">
                        <label>省市区县</label>
                        <div class="location-picker">
                          <div class="location-display" @click="showAddressLocationPicker = !showAddressLocationPicker">
                            <span>{{ formData.province && formData.city ? formData.province + ' / ' + formData.city : '请选择省市区县' }}</span>
                            <span class="location-arrow">▼</span>
                          </div>
                          <div v-if="showAddressLocationPicker" class="location-dropdown">
                            <div class="location-columns">
                              <div class="location-column">
                                <div 
                                  v-for="province in addressProvinces" 
                                  :key="province.name"
                                  class="location-item"
                                  :class="{ active: formData.province === province.name }"
                                  @click="selectAddressProvince(province)"
                                >
                                  {{ province.name }}
                                </div>
                              </div>
                              <div class="location-column">
                                <div 
                                  v-for="city in addressCities" 
                                  :key="city"
                                  class="location-item"
                                  :class="{ active: formData.city === city }"
                                  @click="selectAddressCity(city)"
                                >
                                  {{ city }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="form-group">
                        <label>详细地址</label>
                        <textarea 
                          v-model="formData.detail" 
                          placeholder="请输入详细地址"
                        ></textarea>
                      </div>
                      <div class="form-group checkbox-group">
                        <input 
                          v-model="formData.isDefault" 
                          type="checkbox" 
                          id="isDefault"
                        />
                        <label for="isDefault">设为默认地址</label>
                      </div>
                      <div class="form-actions">
                        <el-button type="button" @click="closeModal">取消</el-button>
                        <el-button type="primary" native-type="submit">{{ isEditing ? '保存修改' : '添加地址' }}</el-button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>

              <!-- 地址选择弹窗 -->
              <div class="modal-overlay" v-if="showAddressPickerModal" @click.self="showAddressPickerModal = false">
                <div class="modal picker-modal">
                  <div class="modal-header">
                    <h3>选择收货地址</h3>
                    <span class="close-btn" @click="showAddressPickerModal = false">×</span>
                  </div>
                  <div class="modal-body">
                    <div class="picker-address-list" v-if="addresses.length > 0">
                      <div 
                        v-for="address in addresses" 
                        :key="address.id"
                        class="picker-address-card"
                        :class="{ active: selectedAddress?.id === address.id }"
                        @click="selectAddressFromPicker(address)"
                      >
                        <div class="picker-address-header">
                          <span class="picker-address-name">{{ address.name }}</span>
                          <span class="picker-address-phone">{{ address.phone }}</span>
                          <span v-if="address.isDefault === 1" class="picker-address-default">默认</span>
                        </div>
                        <div class="picker-address-detail">
                          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
                        </div>
                      </div>
                    </div>
                    <div class="empty-state" v-else>
                      <div class="empty-icon">📦</div>
                      <p>暂无收货地址</p>
                      <el-button type="primary" @click="openAddModalFromPicker">添加第一个地址</el-button>
                    </div>
                    <div class="form-actions" v-if="addresses.length > 0">
                      <el-button type="primary" @click="openAddModalFromPicker">+ 新增收货地址</el-button>
                      <el-button type="button" @click="showAddressPickerModal = false">取消</el-button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 修改密码弹窗 -->
              <div class="modal-overlay" v-if="showChangePasswordModal" @click.self="closeChangePasswordModal">
                <div class="modal">
                  <div class="modal-header">
                    <h3>{{ isResetPassword ? '重置密码' : '修改密码' }}</h3>
                    <span class="close-btn" @click="closeChangePasswordModal">×</span>
                  </div>
                  <div class="modal-body">
                    <form @submit.prevent="isResetPassword ? handleResetPassword : handleChangePassword" class="change-password-form">
                      <div v-if="!isResetPassword" class="form-group">
                        <label>原密码</label>
                        <input 
                          v-model="changePasswordForm.oldPassword" 
                          type="password" 
                          placeholder="请输入原密码"
                          required
                        />
                      </div>
                      
                      <div v-if="isResetPassword" class="form-group">
                        <label>邮箱</label>
                        <input 
                          v-model="changePasswordForm.email" 
                          type="email" 
                          placeholder="请输入注册邮箱"
                          required
                        />
                      </div>
                      
                      <div v-if="isResetPassword" class="form-group">
                        <label>邮箱验证码</label>
                        <div class="code-input-wrapper">
                          <input 
                            v-model="changePasswordForm.emailCode" 
                            type="text" 
                            placeholder="请输入验证码"
                            required
                          />
                          <el-button 
                            type="text" 
                            :disabled="resetPasswordCodeDisabled || !changePasswordForm.email"
                            @click="sendResetPasswordCode"
                          >
                            {{ resetPasswordCodeText }}
                          </el-button>
                        </div>
                      </div>
                      
                      <div class="form-group">
                        <label>新密码</label>
                        <input 
                          v-model="changePasswordForm.newPassword" 
                          type="password" 
                          placeholder="请输入新密码（至少6位）"
                          required
                        />
                      </div>
                      <div class="form-group">
                        <label>确认密码</label>
                        <input 
                          v-model="changePasswordForm.confirmPassword" 
                          type="password" 
                          placeholder="请再次输入新密码"
                          required
                        />
                      </div>
                      <div v-if="!isResetPassword" class="form-group">
                        <a href="#" class="forgot-password-link" @click.prevent="switchToResetPassword">忘记密码？</a>
                      </div>
                      <div v-if="isResetPassword" class="form-group">
                        <a href="#" class="forgot-password-link" @click.prevent="switchToChangePassword">返回修改密码</a>
                      </div>
                      <div class="form-actions">
                        <el-button type="primary" @click="isResetPassword ? handleResetPassword : handleChangePassword">
                          {{ isResetPassword ? '确认重置' : '确认修改' }}
                        </el-button>
                        <el-button type="button" @click="closeChangePasswordModal">取消</el-button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>

              <!-- 修改手机号弹窗 -->
              <div class="modal-overlay" v-if="showChangePhoneModal" @click.self="closeChangePhoneModal">
                <div class="modal">
                  <div class="modal-header">
                    <h3>修改手机号</h3>
                    <span class="close-btn" @click="closeChangePhoneModal">×</span>
                  </div>
                  <div class="modal-body">
                    <form @submit.prevent="handleChangePhone" class="change-phone-form">
                      <div class="form-group">
                        <label>新手机号</label>
                        <input 
                          v-model="changePhoneForm.phone" 
                          type="tel" 
                          placeholder="请输入新手机号"
                          required
                        />
                      </div>
                      <div class="form-group">
                        <label>验证码</label>
                        <div class="code-input-wrapper">
                          <input 
                            v-model="changePhoneForm.code" 
                            type="text" 
                            placeholder="请输入验证码"
                            required
                          />
                          <button 
                            type="button" 
                            class="code-button"
                            :disabled="phoneCodeButtonDisabled"
                            @click="sendPhoneCode"
                          >{{ phoneCodeButtonText }}</button>
                        </div>
                      </div>
                      <div class="form-actions">
                        <el-button type="primary" @click="handleChangePhone">确认修改</el-button>
                        <el-button type="button" @click="closeChangePhoneModal">取消</el-button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>

              <!-- 修改邮箱弹窗 -->
              <div class="modal-overlay" v-if="showChangeEmailModal" @click.self="closeChangeEmailModal">
                <div class="modal">
                  <div class="modal-header">
                    <h3>修改邮箱</h3>
                    <span class="close-btn" @click="closeChangeEmailModal">×</span>
                  </div>
                  <div class="modal-body">
                    <form @submit.prevent="handleChangeEmail" class="change-email-form">
                      <div class="form-group">
                        <label>新邮箱</label>
                        <input 
                          v-model="changeEmailForm.email" 
                          type="email" 
                          placeholder="请输入新邮箱"
                          required
                        />
                      </div>
                      <div class="form-group">
                        <label>验证码</label>
                        <div class="code-input-wrapper">
                          <input 
                            v-model="changeEmailForm.code" 
                            type="text" 
                            placeholder="请输入验证码"
                            required
                          />
                          <button 
                            type="button" 
                            class="code-button"
                            :disabled="emailCodeButtonDisabled"
                            @click="sendEmailCode"
                          >{{ emailCodeButtonText }}</button>
                        </div>
                      </div>
                      <div class="form-actions">
                        <el-button type="primary" @click="handleChangeEmail">确认修改</el-button>
                        <el-button type="button" @click="closeChangeEmailModal">取消</el-button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>

              <!-- 登录记录弹窗 -->
              <div class="modal-overlay" v-if="showLoginRecordsModal" @click.self="closeLoginRecordsModal">
                <div class="modal login-records-modal">
                  <div class="modal-header">
                    <h3>登录记录</h3>
                    <span class="close-btn" @click="closeLoginRecordsModal">×</span>
                  </div>
                  <div class="modal-body">
                    <div v-if="loginRecords.length > 0" class="login-records-list">
                      <div v-for="(record, index) in loginRecords" :key="record.id" class="login-record-item">
                        <div class="record-index">{{ index + 1 }}</div>
                        <div class="record-info">
                          <div class="record-row">
                            <span class="record-label">登录时间</span>
                            <span class="record-value">{{ formatLoginTime(record.loginTime) }}</span>
                          </div>
                          <div class="record-row">
                            <span class="record-label">IP地址</span>
                            <span class="record-value">{{ record.ipAddress || '未知' }}</span>
                          </div>
                          <div class="record-row">
                            <span class="record-label">登录地点</span>
                            <span class="record-value">{{ record.location || '未知位置' }}</span>
                          </div>
                          <div class="record-row">
                            <span class="record-label">设备</span>
                            <span class="record-value">{{ record.device || '未知' }}</span>
                          </div>
                          <div class="record-row">
                            <span class="record-label">浏览器</span>
                            <span class="record-value">{{ record.browser || '未知' }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-else class="empty-state">
                      <div class="empty-icon">📋</div>
                      <p>暂无登录记录</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 账号设置 -->
              <div v-if="activeMenu === 'account'" class="content-section">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span class="bold-title">{{ activeMenuLabel }}</span>
                    </div>
                  </template>
                  <div class="account-section">
                    <div class="account-item">
                      <span class="account-label">密码</span>
                      <span class="account-value">已设置密码</span>
                      <el-button link type="primary" @click="showChangePasswordModal = true">修改密码</el-button>
                    </div>
                    <div class="account-item">
                      <span class="account-label">手机</span>
                      <span class="account-value">{{ authStore.user?.phone ? authStore.user.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '未绑定' }}</span>
                      <el-button link type="primary" @click="showChangePhoneModal = true">修改手机</el-button>
                    </div>
                    <div class="account-item">
                      <span class="account-label">邮箱</span>
                      <span class="account-value">{{ authStore.user?.email ? authStore.user.email.replace(/(.{2})(.*)(@.*)/, '$1****$3') : '未绑定' }}</span>
                      <el-button link type="primary" @click="showChangeEmailModal = true">修改邮箱</el-button>
                    </div>
                    <div class="account-item">
                      <span class="account-label">登录记录</span>
                      <el-button link type="primary" @click="openLoginRecordsModal">查看记录</el-button>
                    </div>
                    <div class="account-item">
                      <span class="account-label">账号注销</span>
                      <el-button link type="primary">立即注销</el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>

            <!-- 编辑弹窗 - 使用原生HTML实现 -->
            <div v-if="editDialogVisible" class="custom-modal-overlay" @click.self="closeEditModal">
              <div class="custom-modal">
                <div class="custom-modal-header">
                  <span>编辑{{ editFieldLabel }}</span>
                  <button class="modal-close" @click="closeEditModal">×</button>
                </div>
                <div class="custom-modal-body">
                  <label>{{ editFieldLabel }}:</label>
                  
                  <!-- 性别选择 -->
                  <div v-if="editField === 'gender'" class="gender-options">
                    <label class="gender-option">
                      <input type="radio" v-model="editValue" value="男" />
                      <span>男</span>
                    </label>
                    <label class="gender-option">
                      <input type="radio" v-model="editValue" value="女" />
                      <span>女</span>
                    </label>
                  </div>
                  
                  <!-- 地区选择器 -->
                  <div v-else-if="editField === 'location'" class="location-picker">
                    <div class="location-display" @click="showLocationPicker = !showLocationPicker">
                      <span>{{ selectedProvince && selectedCity ? selectedProvince + ' / ' + selectedCity : '请选择地区' }}</span>
                      <span class="location-arrow">▼</span>
                    </div>
                    <div v-if="showLocationPicker" class="location-dropdown">
                      <div class="location-columns">
                        <div class="location-column">
                          <div 
                            v-for="province in provinces" 
                            :key="province.name"
                            class="location-item"
                            :class="{ active: selectedProvince === province.name }"
                            @click="selectProvince(province)"
                          >
                            {{ province.name }}
                          </div>
                        </div>
                        <div class="location-column">
                          <div 
                            v-for="city in cities" 
                            :key="city"
                            class="location-item"
                            :class="{ active: selectedCity === city }"
                            @click="selectCity(city)"
                          >
                            {{ city }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 学历选择器 -->
                  <div v-else-if="editField === 'education'" class="education-picker">
                    <div 
                      class="education-display" 
                      @click="showEducationPicker = !showEducationPicker"
                      style="cursor: pointer; user-select: none;"
                    >
                      <span>{{ editValue || '请选择学历' }}</span>
                      <span class="education-arrow">▼</span>
                    </div>
                    <div v-if="showEducationPicker" class="education-dropdown">
                      <div 
                        v-for="item in educationOptions" 
                        :key="item.dictCode"
                        class="education-item"
                        :class="{ active: editValue === item.dictLabel }"
                        @click="() => { editValue = item.dictLabel; showEducationPicker = false; }"
                      >
                        {{ item.dictLabel }}
                      </div>
                    </div>
                  </div>
                  
                  <!-- 日期选择器 -->
                  <div v-else-if="editField === 'birthDate' || editField === 'startWorkDate' || editField === 'enrollmentDate'" class="date-picker">
                    <div class="date-display" @click="showDatePicker = !showDatePicker">
                      <span>{{ editValue || '请选择日期' }}</span>
                      <span class="date-icon">📅</span>
                    </div>
                    <div v-if="showDatePicker" class="date-dropdown">
                      <div class="date-header">
                        <button class="date-nav" @click="prevMonth">◀</button>
                        <span class="date-title">{{ currentYear }}年 {{ monthNames[currentMonth] }}</span>
                        <button class="date-nav" @click="nextMonth">▶</button>
                      </div>
                      <div class="date-weekdays">
                        <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
                      </div>
                      <div class="date-days">
                        <div 
                          v-for="(day, index) in calendarDays" 
                          :key="index"
                          class="date-day"
                          :class="{ 
                            'other-month': !day.currentMonth,
                            'today': day.isToday,
                            'selected': day.date === editValue,
                            'disabled': !day.currentMonth || day.date > todayStr
                          }"
                          @click="selectDate(day)"
                        >
                          {{ day.day }}
                        </div>
                      </div>
                      <div class="date-footer">
                        <button class="date-btn" @click="clearDate">清空</button>
                        <button class="date-btn date-btn-primary" @click="selectToday">今天</button>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 文本域 -->
                  <textarea 
                    v-else-if="editField === 'bio'"
                    v-model="editValue" 
                    rows="4"
                    class="modal-textarea"
                  ></textarea>
                  
                  <!-- 普通文本输入 -->
                  <input 
                    v-else
                    v-model="editValue" 
                    type="text"
                    class="modal-input"
                  />
                </div>
                <div class="custom-modal-footer">
                  <button class="modal-btn modal-btn-cancel" @click="closeEditModal">取消</button>
                  <button class="modal-btn modal-btn-confirm" @click="saveEdit">确定</button>
                </div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/api'
import { ElMessage, ElMessageBox, ElTooltip } from 'element-plus'
import { QuestionFilled, Download, User, MapLocation, Plus, ArrowDown, Setting, Document, ChatLineRound, Grid, UserFilled, DataAnalysis, DocumentChecked, Money, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const activeMenu = ref('content')
const userStats = ref({
  original: 0,
  likes: 0,
  collections: 0
})
const unreadMessageCount = ref(0)
const loading = ref(false)
const loadingComments = ref(false)
const activeCommentTab = ref('my')

// 地址管理相关
const addresses = ref([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const showAddressPickerModal = ref(false)
const isEditing = ref(false)
const selectedAddress = ref(null)

// 分页相关
const addressCurrentPage = ref(1)
const addressPageSize = ref(10)
const addressTotal = ref(0)
const openedFromPicker = ref(false)
const formData = ref({
  id: null,
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

// 地址管理专用地区选择器
const showAddressLocationPicker = ref(false)
const addressProvinces = ref([
  { name: '北京', cities: ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '通州区', '顺义区'] },
  { name: '天津', cities: ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区'] },
  { name: '河北省', cities: ['石家庄市', '唐山市', '秦皇岛市', '邯郸市', '邢台市', '保定市', '张家口市', '承德市'] },
  { name: '山西省', cities: ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市', '晋中市', '运城市'] },
  { name: '内蒙古', cities: ['呼和浩特市', '包头市', '乌海市', '赤峰市', '通辽市', '鄂尔多斯市', '呼伦贝尔市'] },
  { name: '辽宁省', cities: ['沈阳市', '大连市', '鞍山市', '抚顺市', '本溪市', '丹东市', '锦州市', '营口市'] },
  { name: '吉林省', cities: ['长春市', '吉林市', '四平市', '辽源市', '通化市', '白山市', '松原市', '白城市'] },
  { name: '黑龙江省', cities: ['哈尔滨市', '齐齐哈尔市', '鸡西市', '鹤岗市', '双鸭山市', '大庆市', '伊春市'] },
  { name: '上海市', cities: ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区'] },
  { name: '江苏省', cities: ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市', '连云港市', '淮安市'] },
  { name: '浙江省', cities: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市'] },
  { name: '安徽省', cities: ['合肥市', '芜湖市', '蚌埠市', '淮南市', '马鞍山市', '淮北市', '铜陵市', '安庆市'] },
  { name: '福建省', cities: ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市'] },
  { name: '江西省', cities: ['南昌市', '景德镇市', '萍乡市', '九江市', '新余市', '鹰潭市', '赣州市'] },
  { name: '山东省', cities: ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市', '潍坊市', '济宁市'] },
  { name: '河南省', cities: ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市', '新乡市', '焦作市'] },
  { name: '湖北省', cities: ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市', '荆门市', '孝感市'] },
  { name: '湖南省', cities: ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市', '常德市', '张家界市'] },
  { name: '广东省', cities: ['广州市', '韶关市', '深圳市', '珠海市', '汕头市', '佛山市', '江门市', '湛江市'] },
  { name: '广西', cities: ['南宁市', '柳州市', '桂林市', '梧州市', '北海市', '防城港市', '钦州市', '贵港市'] },
  { name: '海南省', cities: ['海口市', '三亚市', '三沙市', '儋州市'] },
  { name: '重庆市', cities: ['万州区', '涪陵区', '渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区'] },
  { name: '四川省', cities: ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市'] },
  { name: '贵州省', cities: ['贵阳市', '六盘水市', '遵义市', '安顺市', '毕节市', '铜仁市'] },
  { name: '云南省', cities: ['昆明市', '曲靖市', '玉溪市', '保山市', '昭通市', '丽江市', '普洱市', '临沧市'] },
  { name: '西藏', cities: ['拉萨市', '日喀则市', '昌都市', '林芝市'] },
  { name: '陕西省', cities: ['西安市', '铜川市', '宝鸡市', '咸阳市', '渭南市', '延安市', '汉中市', '榆林市'] },
  { name: '甘肃省', cities: ['兰州市', '嘉峪关市', '金昌市', '白银市', '天水市', '酒泉市'] },
  { name: '青海省', cities: ['西宁市', '海东市'] },
  { name: '宁夏', cities: ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'] },
  { name: '新疆', cities: ['乌鲁木齐市', '克拉玛依市', '吐鲁番市', '哈密市'] }
])
const addressCities = ref([])

// 昵称编辑相关
const editingNickname = ref(false)
const editNickname = ref('')

const startEditNickname = () => {
  editNickname.value = authStore.user?.nickname || ''
  editingNickname.value = true
}

const saveNickname = async () => {
  if (!editNickname.value.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  
  try {
    await api.put('/api/users/info', { nickname: editNickname.value })
    authStore.user.nickname = editNickname.value
    localStorage.setItem('user', JSON.stringify(authStore.user))
    editingNickname.value = false
    ElMessage.success('昵称更新成功')
  } catch (error) {
    console.error('Failed to update nickname:', error)
    ElMessage.error('更新失败')
  }
}

const cancelEditNickname = () => {
  editingNickname.value = false
  editNickname.value = ''
}

// 通用编辑弹窗相关
const editDialogVisible = ref(false)
const editField = ref('')
const editFieldLabel = ref('')
const editValue = ref('')

// 修改密码相关
const showChangePasswordModal = ref(false)
const isResetPassword = ref(false)
const changePasswordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  email: '',
  emailCode: ''
})
const resetPasswordCodeCountdown = ref(0)
const resetPasswordCodeDisabled = computed(() => resetPasswordCodeCountdown.value > 0)
const resetPasswordCodeText = computed(() => {
  if (resetPasswordCodeCountdown.value > 0) {
    return `${resetPasswordCodeCountdown.value}秒后重新获取`
  }
  return '获取验证码'
})

// 修改手机号相关
const showChangePhoneModal = ref(false)
const changePhoneForm = ref({
  phone: '',
  code: ''
})
const phoneCodeButtonText = ref('获取验证码')
const phoneCodeButtonDisabled = ref(false)
const phoneCodeCountdown = ref(0)

// 修改邮箱相关
const showChangeEmailModal = ref(false)
const changeEmailForm = ref({
  email: '',
  code: ''
})
const emailCodeButtonText = ref('获取验证码')
const emailCodeButtonDisabled = ref(false)
const emailCodeCountdown = ref(0)

// 登录记录相关
const showLoginRecordsModal = ref(false)
const loginRecords = ref([])

// 头像上传相关函数
const triggerAvatarUpload = () => {
  document.getElementById('avatar-upload')?.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('avatar', file)
  
  try {
    const response = await api.post('/api/users/avatar', formData)
    
    if (response && response.url) {
      authStore.user.avatar = response.url
      localStorage.setItem('user', JSON.stringify(authStore.user))
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
  }
  
  event.target.value = ''
}

// 分页事件处理
const handleAddressPageChange = (page) => {
  addressCurrentPage.value = page
  loadAddresses(page, addressPageSize.value)
}

// 地址管理相关函数
const loadAddresses = async (page = 1, size = 10) => {
  try {
    const response = await api.get('/api/addresses', {
      params: {
        page: page,
        size: size
      }
    })
    addresses.value = response.content || response || []
    addressTotal.value = response.totalElements || response.total || 0
    addressCurrentPage.value = page
    if (addresses.value.length > 0 && !selectedAddress.value) {
      const defaultAddress = addresses.value.find(a => a.isDefault === 1)
      selectedAddress.value = defaultAddress || addresses.value[0]
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
  }
}

const selectAddressFromPicker = (address) => {
  selectedAddress.value = address
  showAddressPickerModal.value = false
}

const editAddressFromPicker = (address) => {
  showAddressPickerModal.value = false
  openedFromPicker.value = true
  editAddress(address)
}

const openAddModalFromPicker = () => {
  showAddressPickerModal.value = false
  openedFromPicker.value = true
  openAddModal()
}

const openAddModal = () => {
  isEditing.value = false
  formData.value = {
    id: null,
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  }
  showAddModal.value = true
}

const editAddress = (address) => {
  isEditing.value = true
  formData.value = {
    id: address.id,
    name: address.name,
    phone: address.phone,
    province: address.province || '',
    city: address.city || '',
    district: address.district || '',
    detail: address.detail || '',
    isDefault: address.isDefault === 1
  }
  showEditModal.value = true
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  showAddressLocationPicker.value = false
}

const selectAddressProvince = (province) => {
  formData.value.province = province.name
  addressCities.value = province.cities
  formData.value.city = ''
  formData.value.district = ''
}

const selectAddressCity = (city) => {
  formData.value.city = city
  formData.value.district = city
  showAddressLocationPicker.value = false
}

const submitAddress = async () => {
  try {
    if (!formData.value.phone || !/^1[3-9]\d{9}$/.test(formData.value.phone)) {
      ElMessage.error('请输入正确的手机号码')
      return
    }
    
    const addressData = {
      ...formData.value,
      isDefault: formData.value.isDefault ? 1 : 0
    }
    if (isEditing.value) {
      await api.put(`/api/addresses/${formData.value.id}`, addressData)
    } else {
      await api.post('/api/addresses', addressData)
    }
    closeModal()
    await loadAddresses()
    ElMessage.success('保存成功')
    if (openedFromPicker.value) {
      openedFromPicker.value = false
      showAddressPickerModal.value = true
      if (addresses.value.length > 0) {
        selectedAddress.value = addresses.value[addresses.value.length - 1]
      }
    }
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

const setDefault = async (addressId) => {
  try {
    await api.post(`/api/addresses/${addressId}/default`)
    await loadAddresses()
    ElMessage.success('设置默认地址成功')
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置失败，请重试')
  }
}

const deleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.delete(`/api/addresses/${addressId}`)
    await loadAddresses()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 计算属性：处理头像URL
const userAvatar = computed(() => {
  const avatar = authStore.user?.avatar
  console.log('userAvatar computed, 原始 avatar:', avatar)
  
  if (!avatar) {
    console.log('使用默认头像')
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  // 如果是完整URL，直接返回
  if (avatar.startsWith('http')) {
    console.log('使用完整URL:', avatar)
    return avatar
  }
  // 如果是相对路径，添加后端地址
  if (avatar.startsWith('/uploads/')) {
    console.log('使用相对路径:', avatar)
    return `http://localhost:8081${avatar}`
  }
  // 其他情况
  console.log('使用其他情况:', avatar)
  return avatar
})

const fieldLabels = {
  gender: '性别',
  bio: '个人简介',
  location: '所在地区',
  birthDate: '出生日期',
  startWorkDate: '开始工作时间',
  school: '学校名称',
  major: '专业',
  enrollmentDate: '入学时间',
  education: '学历'
}

const startEdit = (field) => {
  console.log('=== startEdit called ===')
  console.log('Field:', field)
  console.log('authStore.user:', authStore.user)
  
  editField.value = field
  editFieldLabel.value = fieldLabels[field] || field
  editValue.value = authStore.user?.[field] || ''
  
  console.log('Before setting editDialogVisible:', editDialogVisible.value)
  editDialogVisible.value = true
  console.log('After setting editDialogVisible:', editDialogVisible.value)
  
  // 强制触发 Vue 更新
  setTimeout(() => {
    console.log('After timeout - editDialogVisible:', editDialogVisible.value)
    if (typeof window !== 'undefined') {
      const dialog = document.querySelector('.el-dialog')
      console.log('Dialog element exists:', !!dialog)
      if (dialog) {
        console.log('Dialog style:', dialog.style.display)
      }
    }
  }, 0)
}

// 挂载到 window 供原生 onclick 使用
if (typeof window !== 'undefined') {
  window.__startEdit = startEdit
}

// 地区选择器相关
const showLocationPicker = ref(false)
const selectedProvince = ref('')
const selectedCity = ref('')

const provinces = ref([
  { name: '北京', cities: ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '通州区', '顺义区'] },
  { name: '天津', cities: ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区'] },
  { name: '河北省', cities: ['石家庄市', '唐山市', '秦皇岛市', '邯郸市', '邢台市', '保定市', '张家口市', '承德市'] },
  { name: '山西省', cities: ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市', '晋中市', '运城市'] },
  { name: '内蒙古', cities: ['呼和浩特市', '包头市', '乌海市', '赤峰市', '通辽市', '鄂尔多斯市', '呼伦贝尔市'] },
  { name: '辽宁省', cities: ['沈阳市', '大连市', '鞍山市', '抚顺市', '本溪市', '丹东市', '锦州市', '营口市'] },
  { name: '吉林省', cities: ['长春市', '吉林市', '四平市', '辽源市', '通化市', '白山市', '松原市', '白城市'] },
  { name: '黑龙江省', cities: ['哈尔滨市', '齐齐哈尔市', '鸡西市', '鹤岗市', '双鸭山市', '大庆市', '伊春市'] },
  { name: '上海市', cities: ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区'] },
  { name: '江苏省', cities: ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市', '连云港市', '淮安市'] },
  { name: '浙江省', cities: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市'] },
  { name: '安徽省', cities: ['合肥市', '芜湖市', '蚌埠市', '淮南市', '马鞍山市', '淮北市', '铜陵市', '安庆市'] },
  { name: '福建省', cities: ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市'] },
  { name: '江西省', cities: ['南昌市', '景德镇市', '萍乡市', '九江市', '新余市', '鹰潭市', '赣州市'] },
  { name: '山东省', cities: ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市', '潍坊市', '济宁市'] },
  { name: '河南省', cities: ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市', '新乡市', '焦作市'] },
  { name: '湖北省', cities: ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市', '荆门市', '孝感市'] },
  { name: '湖南省', cities: ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市', '常德市', '张家界市'] },
  { name: '广东省', cities: ['广州市', '韶关市', '深圳市', '珠海市', '汕头市', '佛山市', '江门市', '湛江市'] },
  { name: '广西', cities: ['南宁市', '柳州市', '桂林市', '梧州市', '北海市', '防城港市', '钦州市', '贵港市'] },
  { name: '海南省', cities: ['海口市', '三亚市', '三沙市', '儋州市'] },
  { name: '重庆市', cities: ['万州区', '涪陵区', '渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区'] },
  { name: '四川省', cities: ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市'] },
  { name: '贵州省', cities: ['贵阳市', '六盘水市', '遵义市', '安顺市', '毕节市', '铜仁市'] },
  { name: '云南省', cities: ['昆明市', '曲靖市', '玉溪市', '保山市', '昭通市', '丽江市', '普洱市', '临沧市'] },
  { name: '西藏', cities: ['拉萨市', '日喀则市', '昌都市', '林芝市'] },
  { name: '陕西省', cities: ['西安市', '铜川市', '宝鸡市', '咸阳市', '渭南市', '延安市', '汉中市', '榆林市'] },
  { name: '甘肃省', cities: ['兰州市', '嘉峪关市', '金昌市', '白银市', '天水市', '酒泉市'] },
  { name: '青海省', cities: ['西宁市', '海东市'] },
  { name: '宁夏', cities: ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'] },
  { name: '新疆', cities: ['乌鲁木齐市', '克拉玛依市', '吐鲁番市', '哈密市'] }
])

const cities = ref([])

const selectProvince = (province) => {
  selectedProvince.value = province.name
  cities.value = province.cities
  selectedCity.value = ''
}

const selectCity = (city) => {
  selectedCity.value = city
  editValue.value = selectedProvince.value + ' / ' + selectedCity.value
}

// 学历选择器相关
const showEducationPicker = ref(false)
const educationOptions = ref([])

const fetchEducationOptions = async () => {
  try {
    const data = await api.get('/api/dictionary/type/education')
    if (data && data.length > 0) {
      educationOptions.value = data
    }
  } catch (error) {
    console.error('Failed to fetch education options:', error)
  }
}

const selectEducation = (item) => {
  editValue.value = item.dictLabel
  showEducationPicker.value = false
}

// 日期选择器相关
const showDatePicker = ref(false)
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
const today = new Date()
const todayStr = today.toISOString().split('T')[0]

const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth())

const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const days = []
  
  const startDay = firstDay.getDay()
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  
  for (let i = startDay - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const date = new Date(year, month - 1, day)
    days.push({
      day,
      date: date.toISOString().split('T')[0],
      currentMonth: false,
      isToday: false
    })
  }
  
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(year, month, i)
    const dateStr = date.toISOString().split('T')[0]
    days.push({
      day: i,
      date: dateStr,
      currentMonth: true,
      isToday: dateStr === todayStr
    })
  }
  
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      day: i,
      date: date.toISOString().split('T')[0],
      currentMonth: false,
      isToday: false
    })
  }
  
  return days
})

const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const selectDate = (day) => {
  if (!day.currentMonth || day.date > todayStr) return
  editValue.value = day.date
  showDatePicker.value = false
}

const clearDate = () => {
  editValue.value = ''
}

const selectToday = () => {
  editValue.value = todayStr
  showDatePicker.value = false
}

const closeEditModal = () => {
  console.log('closeEditModal called')
  editDialogVisible.value = false
}

// 修改密码相关方法
const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false
  isResetPassword.value = false
  changePasswordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    email: '',
    emailCode: ''
  }
  resetPasswordCodeCountdown.value = 0
}

const switchToResetPassword = () => {
  isResetPassword.value = true
  changePasswordForm.value.oldPassword = ''
}

const switchToChangePassword = () => {
  isResetPassword.value = false
  changePasswordForm.value.email = ''
  changePasswordForm.value.emailCode = ''
}

const sendResetPasswordCode = async () => {
  const email = changePasswordForm.value.email
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.error('请输入正确的邮箱')
    return
  }
  
  resetPasswordCodeCountdown.value = 60
  
  try {
    await api.post('/api/users/password/reset/code', { email })
    ElMessage.success('验证码已发送到您的邮箱')
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
    resetPasswordCodeCountdown.value = 0
  }
  
  const timer = setInterval(() => {
    resetPasswordCodeCountdown.value--
    if (resetPasswordCodeCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleChangePassword = async () => {
  if (!changePasswordForm.value.oldPassword) {
    ElMessage.error('请输入原密码')
    return
  }
  if (!changePasswordForm.value.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (changePasswordForm.value.newPassword.length < 6) {
    ElMessage.error('新密码至少需要6位')
    return
  }
  if (changePasswordForm.value.newPassword !== changePasswordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  try {
    await api.put('/api/users/password', {
      oldPassword: changePasswordForm.value.oldPassword,
      newPassword: changePasswordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    closeChangePasswordModal()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.response?.data?.message || '修改密码失败，请检查原密码是否正确')
  }
}

const handleResetPassword = async () => {
  const email = changePasswordForm.value.email
  const emailCode = changePasswordForm.value.emailCode
  const newPassword = changePasswordForm.value.newPassword
  const confirmPassword = changePasswordForm.value.confirmPassword
  
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.error('请输入正确的邮箱')
    return
  }
  if (!emailCode) {
    ElMessage.error('请输入邮箱验证码')
    return
  }
  if (!newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (newPassword.length < 6) {
    ElMessage.error('新密码至少需要6位')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  try {
    await api.post('/api/users/password/reset', {
      email,
      code: emailCode,
      newPassword
    })
    ElMessage.success('密码重置成功，请重新登录')
    closeChangePasswordModal()
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  } catch (error) {
    console.error('密码重置失败:', error)
    ElMessage.error(error.response?.data?.message || '密码重置失败')
  }
}

// 修改手机号相关方法
const closeChangePhoneModal = () => {
  showChangePhoneModal.value = false
  changePhoneForm.value = {
    phone: '',
    code: ''
  }
  phoneCodeButtonText.value = '获取验证码'
  phoneCodeButtonDisabled.value = false
  phoneCodeCountdown.value = 0
}

const sendPhoneCode = async () => {
  const phone = changePhoneForm.value.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  phoneCodeButtonDisabled.value = true
  phoneCodeCountdown.value = 60
  phoneCodeButtonText.value = '60秒后重新获取'
  
  try {
    await api.post('/api/users/phone/code', { phone })
    ElMessage.success('验证码已发送')
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
    phoneCodeButtonDisabled.value = false
    phoneCodeCountdown.value = 0
    phoneCodeButtonText.value = '获取验证码'
    return
  }
  
  const timer = setInterval(() => {
    phoneCodeCountdown.value--
    if (phoneCodeCountdown.value > 0) {
      phoneCodeButtonText.value = `${phoneCodeCountdown.value}秒后重新获取`
    } else {
      clearInterval(timer)
      phoneCodeButtonDisabled.value = false
      phoneCodeButtonText.value = '获取验证码'
    }
  }, 1000)
}

const handleChangePhone = async () => {
  const phone = changePhoneForm.value.phone
  const code = changePhoneForm.value.code
  
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  if (!code) {
    ElMessage.error('请输入验证码')
    return
  }
  
  try {
    await api.put('/api/users/phone', { phone, code })
    ElMessage.success('手机号修改成功')
    authStore.user.phone = phone
    localStorage.setItem('user', JSON.stringify(authStore.user))
    closeChangePhoneModal()
  } catch (error) {
    console.error('修改手机号失败:', error)
    ElMessage.error(error.response?.data?.message || '修改手机号失败，请检查验证码是否正确')
  }
}

// 修改邮箱相关方法
const closeChangeEmailModal = () => {
  showChangeEmailModal.value = false
  changeEmailForm.value = {
    email: '',
    code: ''
  }
  emailCodeButtonText.value = '获取验证码'
  emailCodeButtonDisabled.value = false
  emailCodeCountdown.value = 0
}

const sendEmailCode = async () => {
  const email = changeEmailForm.value.email
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.error('请输入正确的邮箱')
    return
  }
  
  emailCodeButtonDisabled.value = true
  emailCodeCountdown.value = 60
  emailCodeButtonText.value = '60秒后重新获取'
  
  try {
    await api.post('/api/users/email/code', { email })
    ElMessage.success('验证码已发送到您的邮箱')
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
    emailCodeButtonDisabled.value = false
    emailCodeCountdown.value = 0
    emailCodeButtonText.value = '获取验证码'
    return
  }
  
  const timer = setInterval(() => {
    emailCodeCountdown.value--
    if (emailCodeCountdown.value > 0) {
      emailCodeButtonText.value = `${emailCodeCountdown.value}秒后重新获取`
    } else {
      clearInterval(timer)
      emailCodeButtonDisabled.value = false
      emailCodeButtonText.value = '获取验证码'
    }
  }, 1000)
}

const handleChangeEmail = async () => {
  const email = changeEmailForm.value.email
  const code = changeEmailForm.value.code
  
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.error('请输入正确的邮箱')
    return
  }
  if (!code) {
    ElMessage.error('请输入验证码')
    return
  }
  
  try {
    await api.put('/api/users/email', { email, code })
    ElMessage.success('邮箱修改成功')
    authStore.user.email = email
    localStorage.setItem('user', JSON.stringify(authStore.user))
    closeChangeEmailModal()
  } catch (error) {
    console.error('修改邮箱失败:', error)
    ElMessage.error(error.response?.data?.message || '修改邮箱失败，请检查验证码是否正确')
  }
}

// 登录记录相关方法
const openLoginRecordsModal = async () => {
  showLoginRecordsModal.value = true
  await loadLoginRecords()
}

const closeLoginRecordsModal = () => {
  showLoginRecordsModal.value = false
}

const loadLoginRecords = async () => {
  try {
    const response = await api.get('/api/users/login-records')
    loginRecords.value = response || []
  } catch (error) {
    console.error('加载登录记录失败:', error)
    ElMessage.error('加载登录记录失败')
  }
}

const formatLoginTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}



const saveEdit = async () => {
  console.log('=== saveEdit called ===')
  console.log('editField.value:', editField.value)
  console.log('editValue.value:', editValue.value)
  console.log('editFieldLabel.value:', editFieldLabel.value)
  
  if (!editValue.value || !editValue.value.trim()) {
    ElMessage.warning(`请输入${editFieldLabel.value}`)
    return
  }
  
  try {
    const data = {}
    data[editField.value] = editValue.value
    console.log('Saving data:', JSON.stringify(data))
    console.log('Request URL:', '/users/info')
    console.log('Request method: PUT')
    console.log('Request data:', data)
    
    const response = await api.put('/api/users/info', data)
    console.log('Save response:', response)
    
    // 使用 authStore.updateUser 来正确触发响应式更新
    const updates = {}
    updates[editField.value] = editValue.value
    authStore.updateUser(updates)
    
    editDialogVisible.value = false
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('Failed to update:', error)
    console.error('Error message:', error.message)
    if (error.response) {
      console.error('Response status:', error.response.status)
      console.error('Response data:', error.response.data)
    }
    ElMessage.error('更新失败')
  }
}

// 作品数据相关变量
const workStats = ref({
  totalArticles: 0,
  totalLikes: 0,
  totalComments: 0,
  totalViews: 0,
  totalCollections: 0
})
const trendDataList = ref([])
const dateRange = ref([])
const selectedDataTypes = ref(['浏览量', '评论数', '粉丝数', '收藏数'])
const activeTrendTab = ref('chart')
const activeTimeRange = ref('7days')

// 悬停提示相关变量
const tooltipVisible = ref(false)
const tooltipData = ref({
  type: '',
  value: 0,
  date: '',
  x: 0,
  y: 0
})

// 趋势数据相关计算属性
const maxValue = computed(() => {
  if (trendDataList.value.length === 0) return 35
  const maxViews = Math.max(...trendDataList.value.map(item => item.views || 0))
  const maxComments = Math.max(...trendDataList.value.map(item => item.comments || 0))
  const maxFollowers = Math.max(...trendDataList.value.map(item => item.followers || 0))
  const maxCollections = Math.max(...trendDataList.value.map(item => item.collections || 0))
  const maxVal = Math.max(maxViews, maxComments, maxFollowers, maxCollections)
  return maxVal > 0 ? Math.ceil(maxVal * 1.2) : 35
})



const totalViews = computed(() => {
  return trendDataList.value.reduce((sum, item) => sum + (item.views || 0), 0)
})

const totalComments = computed(() => {
  return trendDataList.value.reduce((sum, item) => sum + (item.comments || 0), 0)
})

const totalFollowers = computed(() => {
  return trendDataList.value.reduce((sum, item) => sum + (item.followers || 0), 0)
})

const totalCollections = computed(() => {
  return trendDataList.value.reduce((sum, item) => sum + (item.collections || 0), 0)
})

// 数据类型颜色映射
const colorMap = {
  '浏览量': '#ff6b6b',
  '评论数': '#4ecdc4',
  '粉丝数': '#95e1d3',
  '收藏数': '#f38181'
}

// 数据类型字段映射
const fieldMap = {
  '浏览量': 'views',
  '评论数': 'comments',
  '粉丝数': 'followers',
  '收藏数': 'collections'
}

// 获取数据类型对应的颜色
const getColor = (type) => {
  return colorMap[type] || '#666'
}

// 获取指定类型的图表点
const getChartPoints = (type) => {
  if (trendDataList.value.length === 0) {
    return [
      { x: 0, y: 160 },
      { x: 114, y: 148 },
      { x: 228, y: 145 },
      { x: 342, y: 142 },
      { x: 457, y: 140 },
      { x: 571, y: 100 },
      { x: 685, y: 130 },
      { x: 800, y: 30 }
    ]
  }
  const maxVal = maxValue.value
  const data = trendDataList.value
  const stepX = 800 / (data.length - 1 || 1)
  const field = fieldMap[type] || 'views'
  return data.map((item, index) => ({
    x: index * stepX,
    y: maxVal > 0 ? 200 - (item[field] || 0) * (200 / maxVal) : 160
  }))
}

// 获取指定类型的折线路径
const getLinePath = (type) => {
  const points = getChartPoints(type)
  if (points.length < 2) return ''
  let path = `M ${points[0].x},${points[0].y}`
  for (let i = 1; i < points.length; i++) {
    const prev = points[i - 1]
    const curr = points[i]
    const cpX = (prev.x + curr.x) / 2
    path += ` Q ${cpX},${prev.y} ${curr.x},${curr.y}`
  }
  return path
}

// 获取指定类型的总计
const getTotal = (type) => {
  const field = fieldMap[type] || 'views'
  return trendDataList.value.reduce((sum, item) => sum + (item[field] || 0), 0)
}

// 日期选择器配置
const datePickerOptions = {
  disabledDate: (time) => {
    const oneYearAgo = new Date()
    oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1)
    const today = new Date()
    today.setHours(23, 59, 59, 999)
    return time.getTime() < oneYearAgo.getTime() || time.getTime() > today.getTime()
  }
}

// 选择时间范围
const selectTimeRange = async (range) => {
  activeTimeRange.value = range
  dateRange.value = []
  
  let days = 7
  if (range === '30days') {
    days = 30
  }
  
  await loadTrendData(days)
}

// 处理日期范围变化
const handleDateRangeChange = async (val) => {
  if (!val || val.length !== 2) {
    return
  }
  
  const startDate = val[0]
  const endDate = val[1]
  
  // 重置快捷选择按钮状态
  activeTimeRange.value = ''
  
  // 验证日期范围
  const validation = validateDateRange(startDate, endDate)
  if (!validation.valid) {
    ElMessage.warning(validation.message)
    dateRange.value = []
    return
  }
  
  // 加载数据
  const startStr = formatDateToStr(startDate)
  const endStr = formatDateToStr(endDate)
  await loadTrendDataByDateRange(startStr, endStr)
}

// 验证日期范围
const validateDateRange = (startDate, endDate) => {
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  // 计算天数差
  const diffTime = Math.abs(end.getTime() - start.getTime())
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  // 验证日期区间最多30天
  if (diffDays > 30) {
    return { valid: false, message: '日期区间最多显示一个月（30天）' }
  }
  
  // 验证开始日期不能早于一年前
  const oneYearAgo = new Date()
  oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1)
  if (start.getTime() < oneYearAgo.getTime()) {
    return { valid: false, message: '只能访问最近一年的数据' }
  }
  
  // 验证开始日期不能大于结束日期
  if (start.getTime() > end.getTime()) {
    return { valid: false, message: '开始日期不能大于结束日期' }
  }
  
  return { valid: true, message: '' }
}

// 格式化日期为字符串
const formatDateToStr = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 显示悬停提示
const showTooltip = (type, x, y, index) => {
  if (trendDataList.value.length === 0) return
  
  const dataItem = trendDataList.value[index]
  if (!dataItem) return
  
  const field = fieldMap[type] || 'views'
  const value = dataItem[field] || 0
  const date = dataItem.date || ''
  
  tooltipData.value = {
    type,
    value,
    date,
    x: x + 60, // 偏移量，避免提示框覆盖数据点
    y: y - 50
  }
  tooltipVisible.value = true
}

// 隐藏悬停提示
const hideTooltip = () => {
  tooltipVisible.value = false
}

// 按日期范围加载趋势数据
const loadTrendDataByDateRange = async (startDate, endDate) => {
  try {
    const response = await api.get('/api/trend/data', {
      params: {
        startDate: startDate,
        endDate: endDate
      }
    })
    if (response && Array.isArray(response)) {
      trendDataList.value = response
    } else {
      trendDataList.value = []
    }
  } catch (error) {
    console.error('Failed to load trend data:', error)
    trendDataList.value = []
    ElMessage.error('获取趋势数据失败，请检查登录状态')
  }
}

// 加载趋势数据
const loadTrendData = async (days = 7) => {
  try {
    console.log(`开始加载趋势数据，天数: ${days}`)
    const token = localStorage.getItem('token')
    console.log('当前token:', token ? '存在' : '不存在')
    
    const response = await api.get(`/api/trend/data/${days}`)
    console.log('趋势数据响应:', response)
    
    if (response && Array.isArray(response)) {
      trendDataList.value = response
      console.log('趋势数据加载成功，共', response.length, '条记录')
    } else {
      trendDataList.value = []
      console.log('趋势数据为空')
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    trendDataList.value = []
    // 如果是认证错误，提示用户重新登录
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('获取趋势数据失败，请检查登录状态')
    }
  }
}




const myComments = ref([])
const receivedComments = ref([])
const commentCurrentPage = ref(1)
const commentPageSize = ref(10)
const myCommentsTotal = ref(0)
const receivedCommentsTotal = ref(0)
const loadingTags = ref(false)
const loadingLabels = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  tagName: '',
  category: ''
})
const categories = ref([])

// 加载分类字典
const loadCategories = async () => {
  try {
    const response = await api.get('/api/dictionary/type/article_category')
    if (Array.isArray(response)) {
      categories.value = response.map(item => ({
        value: item.dictCode,
        label: item.dictLabel
      }))
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
    // 加载失败时不使用默认分类，让后端处理
    categories.value = []
  }
}

// 获取分类中文名称
const getCategoryName = (category) => {
  const categoryItem = categories.value.find(item => item.value === category)
  return categoryItem ? categoryItem.label : category
}

// 内容列表
const contents = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tags = ref([])
const currentTagPage = ref(1)
const tagPageSize = ref(10)
const totalTags = ref(0)

// 粉丝管理
const activeFollowerTab = ref('followers')
const myFollowers = ref([])
const myFollowing = ref([])
const myFriends = ref([])
const myBlacklist = ref([])

const labels = ref([])

const incomeStats = ref({
  totalIncome: 0,
  monthlyIncome: 0,
  dailyIncome: 0
})

const followerStats = ref({
  totalFollowers: 0,
  monthlyGrowth: 0,
  dailyGrowth: 0
})

const summaryStats = ref({
  weekArticles: 0,
  weekViews: 0,
  weekLikes: 0,
  weekFollowers: 0
})

// 计算属性：获取当前菜单的标签
const activeMenuLabel = computed(() => {
  const menuLabels = {
    'content': '内容管理',
    'comments': '评论管理',
    'tags': '标签管理',
    'labels': '粉丝管理',
    'works': '作品数据',
    'income': '收益数据',
    'followers': '粉丝数据',
    'summary': '一周小结',
    'profile': '个人资料',
    'address': '地址管理',
    'account': '账号设置'
  }
  return menuLabels[activeMenu.value] || '内容管理'
})

const createNewDocument = () => {
  router.push('/editor')
}

const editDocument = (id) => {
  router.push(`/editor/${id}`)
}

const viewDocument = (id) => {
  router.push(`/document/${id}`)
}

const deleteDocument = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
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

const deleteComment = async (id) => {
  console.log('开始删除评论，评论ID:', id)
  try {
    console.log('显示确认对话框')
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('用户确认删除，调用API删除评论')
    await api.delete(`/api/comments/${id}`)
    console.log('API调用成功，显示成功消息')
    ElMessage.success('删除成功')
    // 重新加载评论数据
    console.log('重新加载评论数据，当前标签页:', activeCommentTab.value)
    if (activeCommentTab.value === 'my') {
      loadMyComments()
    } else {
      loadReceivedComments()
    }
  } catch (error) {
    console.log('捕获到错误:', error)
    if (error.message !== 'cancel') {
      console.error('Failed to delete comment:', error)
      ElMessage.error('删除失败')
    }
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    // 从后端获取数据，传递分页参数
    const response = await api.get('/api/documents/my', {
      params: {
        page: currentPage.value - 1, // 后端使用从0开始的页码
        size: pageSize.value
      }
    })
    console.log('Documents response:', response)
    // 处理分页数据
    if (response && response.records) {
      contents.value = response.records
      total.value = response.total || 0
    } else if (Array.isArray(response)) {
      contents.value = response
      total.value = response.length
    } else {
      // 如果后端没有返回数据，设置为空数组
      console.log('No data from backend')
      contents.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('Failed to load documents:', error)
    // 发生错误时设置为空数组
    contents.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadMyComments = async () => {
  loadingComments.value = true
  try {
    console.log('开始加载我的评论...')
    const response = await api.get('/api/comments/my', {
      params: {
        page: commentCurrentPage.value - 1,
        size: commentPageSize.value
      }
    })
    console.log('我的评论响应:', response)
    if (response && response.records) {
      myComments.value = response.records
      myCommentsTotal.value = response.total || 0
    } else if (Array.isArray(response)) {
      myComments.value = response
      myCommentsTotal.value = response.length
    } else {
      myComments.value = []
      myCommentsTotal.value = 0
    }
    console.log('我的评论数据:', myComments.value)
    console.log('我的评论数量:', myComments.value.length)
    console.log('我的评论总数:', myCommentsTotal.value)
  } catch (error) {
    console.error('加载我的评论失败:', error)
    myComments.value = []
    myCommentsTotal.value = 0
  } finally {
    loadingComments.value = false
  }
}

const loadReceivedComments = async () => {
  loadingComments.value = true
  try {
    console.log('开始加载收到的评论...')
    const response = await api.get('/api/comments/received', {
      params: {
        page: commentCurrentPage.value - 1,
        size: commentPageSize.value
      }
    })
    console.log('收到的评论响应:', response)
    if (response && response.records) {
      receivedComments.value = response.records
      receivedCommentsTotal.value = response.total || 0
    } else if (Array.isArray(response)) {
      receivedComments.value = response
      receivedCommentsTotal.value = response.length
    } else {
      receivedComments.value = []
      receivedCommentsTotal.value = 0
    }
    console.log('收到的评论数据:', receivedComments.value)
    console.log('收到的评论数量:', receivedComments.value.length)
    console.log('收到的评论总数:', receivedCommentsTotal.value)
  } catch (error) {
    console.error('加载收到的评论失败:', error)
    receivedComments.value = []
    receivedCommentsTotal.value = 0
  } finally {
    loadingComments.value = false
  }
}

const handleCommentTabClick = (tab) => {
  commentCurrentPage.value = 1
  if (tab.props.name === 'my') {
    loadMyComments()
  } else if (tab.props.name === 'received') {
    loadReceivedComments()
  }
}

// 处理评论分页变化
const handleCommentPageChange = (page) => {
  commentCurrentPage.value = page
  if (activeCommentTab.value === 'my') {
    loadMyComments()
  } else if (activeCommentTab.value === 'received') {
    loadReceivedComments()
  }
}

// 处理评论每页大小变化
const handleCommentSizeChange = (size) => {
  commentPageSize.value = size
  commentCurrentPage.value = 1
  if (activeCommentTab.value === 'my') {
    loadMyComments()
  } else if (activeCommentTab.value === 'received') {
    loadReceivedComments()
  }
}

// 点击评论进入文章详情页
const navigateToDocument = (documentId) => {
  router.push(`/document/${documentId}`)
}

const loadTags = async () => {
  loadingTags.value = true
  try {
    const response = await api.get('/api/tags')
    tags.value = Array.isArray(response) ? response : []
    totalTags.value = tags.value.length
  } catch (error) {
    console.error('Failed to load tags:', error)
    tags.value = []
    totalTags.value = 0
  } finally {
    loadingTags.value = false
  }
}

// 编辑标签
const editTag = (tag) => {
  dialogTitle.value = '编辑标签'
  form.value = { ...tag }
  dialogVisible.value = true
}

// 新建标签
const createTag = () => {
  dialogTitle.value = '新建标签'
  form.value = {
    id: null,
    tagName: '',
    category: categories.value.length > 0 ? categories.value[0].value : ''
  }
  dialogVisible.value = true
}

// 保存标签
const saveTag = async () => {
  try {
    if (!form.value.tagName || !form.value.category) {
      ElMessage.warning('请填写标签名称和分类')
      return
    }
    
    if (form.value.id) {
      // 编辑标签
      await api.put(`/api/tags/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      // 新建标签
      await api.post('/tags', form.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadTags()
  } catch (error) {
    console.error('Failed to save tag:', error)
    // 检查后端返回的错误信息
    if (error.response && error.response.data) {
      // 检查error.response.data是否是字符串
      if (typeof error.response.data === 'string') {
        ElMessage.error(error.response.data)
      } else if (error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('保存失败')
      }
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('保存失败')
    }
  }
}

// 删除标签
const deleteTag = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个标签吗？', '删除标签', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.delete(`/api/tags/${id}`)
    ElMessage.success('删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete tag:', error)
    }
  }
}

const loadLabels = async () => {
  loadingLabels.value = true
  try {
    const response = await api.get('/api/labels')
    labels.value = Array.isArray(response) ? response : []
  } catch (error) {
    console.error('Failed to load labels:', error)
    labels.value = []
  } finally {
    loadingLabels.value = false
  }
}

// 加载我的粉丝
const loadFollowers = async () => {
  await loadFollowersWithStatus()
}

// 加载我的关注
const loadFollowing = async () => {
  try {
    console.log('开始加载关注列表...')
    const response = await api.get('/api/following')
    console.log('获取关注列表响应:', response)
    // 检查response是否是数组
    const following = Array.isArray(response) ? response : []
    console.log('关注列表:', following)
    
    // 检查每个关注用户的拉黑状态
    for (const user of following) {
      console.log(`处理关注用户: ${user.username} (${user.id})`)
      try {
        const blockResponse = await api.get(`/api/chat/blacklist/check?userId=${user.id}`)
        console.log(`拉黑状态响应 for ${user.id}:`, blockResponse)
        // 响应拦截器已经返回了boolean值
        user.isBlocked = blockResponse
        console.log(`拉黑状态 for ${user.id}:`, user.isBlocked)
      } catch (error) {
        console.error(`Failed to check block status for user ${user.id}:`, error)
        user.isBlocked = false
      }
    }
    
    console.log('处理后的关注列表:', following)
    // 直接更新列表数据，避免页面闪烁
    myFollowing.value = following
    console.log('最终关注列表:', myFollowing.value)
  } catch (error) {
    console.error('Failed to load following:', error)
    myFollowing.value = []
  }
}

// 加载好友列表
const loadFriends = async () => {
  try {
    console.log('开始加载好友列表...')
    const response = await api.get('/api/friends')
    console.log('获取好友列表响应:', response)
    // 检查response是否是数组
    const friends = Array.isArray(response) ? response : []
    console.log('好友列表:', friends)
    
    // 检查每个好友的拉黑状态
    for (const friend of friends) {
      console.log(`处理好友: ${friend.username} (${friend.id})`)
      try {
        const blockResponse = await api.get(`/api/chat/blacklist/check?userId=${friend.id}`)
        console.log(`拉黑状态响应 for ${friend.id}:`, blockResponse)
        // 响应拦截器已经返回了boolean值
        friend.isBlocked = blockResponse
        console.log(`拉黑状态 for ${friend.id}:`, friend.isBlocked)
      } catch (error) {
        console.error(`Failed to check block status for user ${friend.id}:`, error)
        friend.isBlocked = false
      }
    }
    
    console.log('处理后的好友列表:', friends)
    // 直接更新列表数据，避免页面闪烁
    myFriends.value = friends
    console.log('最终好友列表:', myFriends.value)
  } catch (error) {
    console.error('Failed to load friends:', error)
    myFriends.value = []
  }
}

// 加载黑名单列表
const loadBlacklist = async () => {
  try {
    console.log('开始加载黑名单列表...')
    const response = await api.get('/api/chat/blacklist')
    console.log('获取黑名单列表响应:', response)
    // 检查response是否是数组
    const blacklist = Array.isArray(response) ? response : []
    console.log('黑名单列表:', blacklist)
    
    console.log('处理后的黑名单列表:', blacklist)
    // 直接更新列表数据，避免页面闪烁
    myBlacklist.value = blacklist
    console.log('最终黑名单列表:', myBlacklist.value)
  } catch (error) {
    console.error('Failed to load blacklist:', error)
    myBlacklist.value = []
  }
}

const loadFollowerStats = async () => {
  try {
    console.log('开始加载粉丝统计数据...')
    const token = localStorage.getItem('token')
    console.log('当前token:', token ? '存在' : '不存在')
    
    const response = await api.get('/api/follow/stats')
    console.log('获取粉丝统计数据响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应内容:', JSON.stringify(response))
    
    if (response && typeof response === 'object') {
      followerStats.value = {
        totalFollowers: response.totalFollowers !== undefined ? response.totalFollowers : 0,
        monthlyGrowth: response.monthlyGrowth !== undefined ? response.monthlyGrowth : 0,
        dailyGrowth: response.dailyGrowth !== undefined ? response.dailyGrowth : 0
      }
      console.log('粉丝统计数据加载成功:', followerStats.value)
    } else {
      console.warn('响应数据格式不正确:', response)
      followerStats.value = {
        totalFollowers: 0,
        monthlyGrowth: 0,
        dailyGrowth: 0
      }
    }
  } catch (error) {
    console.error('Failed to load follower stats:', error)
    console.error('错误详情:', error.response ? error.response.data : error.message)
    followerStats.value = {
      totalFollowers: 0,
      monthlyGrowth: 0,
      dailyGrowth: 0
    }
  }
}

const loadSummaryStats = async () => {
  try {
    console.log('开始加载一周小结数据...')
    const token = localStorage.getItem('token')
    console.log('当前token:', token ? '存在' : '不存在')
    
    const response = await api.get('/api/summary')
    console.log('获取一周小结响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应内容:', JSON.stringify(response))
    
    if (response && typeof response === 'object') {
      summaryStats.value = {
        weekArticles: response.weekArticles !== undefined ? response.weekArticles : 0,
        weekViews: response.weekViews !== undefined ? response.weekViews : 0,
        weekLikes: response.weekLikes !== undefined ? response.weekLikes : 0,
        weekFollowers: response.weekFollowers !== undefined ? response.weekFollowers : 0
      }
      console.log('一周小结数据加载成功:', summaryStats.value)
    } else {
      console.warn('响应数据格式不正确:', response)
      summaryStats.value = {
        weekArticles: 0,
        weekViews: 0,
        weekLikes: 0,
        weekFollowers: 0
      }
    }
  } catch (error) {
    console.error('Failed to load summary stats:', error)
    console.error('错误详情:', error.response ? error.response.data : error.message)
    summaryStats.value = {
      weekArticles: 0,
      weekViews: 0,
      weekLikes: 0,
      weekFollowers: 0
    }
  }
}

// 监听粉丝标签页变化，加载对应数据
watch(activeFollowerTab, (newTab) => {
  if (newTab === 'followers') {
    loadFollowers()
  } else if (newTab === 'following') {
    loadFollowing()
  } else if (newTab === 'friends') {
    loadFriends()
  } else if (newTab === 'blacklist') {
    loadBlacklist()
  }
})

// 格式化时间
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 回关用户
const followUser = async (userId) => {
  try {
    console.log(`开始关注用户: ${userId}`)
    const response = await api.post(`/api/users/${userId}/follow`)
    console.log('关注用户响应:', response)
    // 无论响应是什么，只要没有抛出异常，就认为操作成功
    ElMessage.success('关注成功')
    // 重新加载关注列表、粉丝列表和好友列表
    console.log('重新加载关注列表...')
    await loadFollowing()
    console.log('重新加载粉丝列表...')
    await loadFollowers()
    console.log('重新加载好友列表...')
    await loadFriends()
    console.log('加载完成')
  } catch (error) {
    console.error('Failed to follow user:', error)
    // 检查错误消息，如果是"已经关注了该用户"，则显示成功提示
    if (error.message === '已经关注了该用户') {
      ElMessage.success('已经关注了该用户')
      // 重新加载关注列表、粉丝列表和好友列表
      await loadFollowing()
      await loadFollowers()
      await loadFriends()
    } else {
      ElMessage.error(error.response?.data?.message || '关注失败')
    }
  }
}

// 取消关注用户
const unfollowUser = async (userId) => {
  try {
    console.log(`开始取消关注用户: ${userId}`)
    const response = await api.delete(`/api/users/${userId}/follow`)
    console.log('取消关注用户响应:', response)
    // 响应拦截器已经处理了success字段，直接使用响应
    ElMessage.success('取消关注成功')
    // 重新加载关注列表、粉丝列表和好友列表
    console.log('重新加载关注列表...')
    await loadFollowing()
    console.log('重新加载粉丝列表...')
    await loadFollowers()
    console.log('重新加载好友列表...')
    await loadFriends()
    console.log('加载完成')
  } catch (error) {
    console.error('Failed to unfollow user:', error)
    ElMessage.error(error.response?.data?.message || '取消关注失败')
  }
}

// 移除粉丝
const removeFollower = async (userId) => {
  try {
    console.log(`开始移除粉丝: ${userId}`)
    const response = await api.delete(`/api/users/followers/${userId}`)
    console.log('移除粉丝响应:', response)
    // 响应拦截器已经处理了success字段，直接使用响应
    ElMessage.success('移除成功')
    // 重新加载粉丝列表和好友列表
    console.log('重新加载粉丝列表...')
    await loadFollowers()
    console.log('重新加载好友列表...')
    await loadFriends()
    console.log('加载完成')
  } catch (error) {
    console.error('Failed to remove follower:', error)
    ElMessage.error(error.response?.data?.message || '移除失败')
  }
}

// 拉黑用户
const blockUser = async (userId) => {
  try {
    console.log(`开始拉黑用户: ${userId}`)
    const response = await api.post(`/api/users/${userId}/block`)
    console.log('拉黑用户响应:', response)
    // 响应拦截器已经处理了success字段，直接使用响应
    ElMessage.success('拉黑成功')
    // 重新加载粉丝列表、关注列表、好友列表和黑名单列表
    console.log('重新加载粉丝列表...')
    await loadFollowers()
    console.log('重新加载关注列表...')
    await loadFollowing()
    console.log('重新加载好友列表...')
    await loadFriends()
    console.log('重新加载黑名单列表...')
    await loadBlacklist()
    console.log('加载完成')
  } catch (error) {
    console.error('Failed to block user:', error)
    ElMessage.error(error.response?.data?.message || '拉黑失败')
  }
}

// 取消拉黑用户
const unblockUser = async (userId) => {
  try {
    console.log(`开始取消拉黑用户: ${userId}`)
    const response = await api.delete(`/api/chat/blacklist?blockedUserId=${userId}`)
    console.log('取消拉黑用户响应:', response)
    // 响应拦截器已经处理了success字段，直接使用响应
    ElMessage.success('取消拉黑成功')
    // 重新加载粉丝列表、关注列表、好友列表和黑名单列表
    console.log('重新加载粉丝列表...')
    await loadFollowers()
    console.log('重新加载关注列表...')
    await loadFollowing()
    console.log('重新加载好友列表...')
    await loadFriends()
    console.log('重新加载黑名单列表...')
    await loadBlacklist()
    console.log('加载完成')
  } catch (error) {
    console.error('Failed to unblock user:', error)
    ElMessage.error(error.response?.data?.message || '取消拉黑失败')
  }
}

// 发送私信
const sendMessage = (userId) => {
  console.log(`跳转到与用户 ${userId} 的聊天界面`)
  // 跳转到消息中心并指定聊天用户
  router.push(`/messages?userId=${userId}`)
}

// 查看用户主页
const viewUser = (userId) => {
  console.log(`跳转到用户 ${userId} 的主页`)
  // 跳转到用户主页
  router.push(`/user/${userId}`)
}

// 检查关注状态
const checkFollowStatus = async (userId) => {
  try {
    const response = await api.get(`/api/users/${userId}/follow/status`)
    return response.data
  } catch (error) {
    console.error('Failed to check follow status:', error)
    return false
  }
}

// 检查拉黑状态
const checkBlockStatus = async (userId) => {
  try {
    const response = await api.get(`/api/chat/blacklist/check?userId=${userId}`)
    return response.data
  } catch (error) {
    console.error('Failed to check block status:', error)
    return false
  }
}

// 加载粉丝列表并检查状态
const loadFollowersWithStatus = async () => {
  try {
    console.log('开始加载粉丝列表...')
    const response = await api.get('/api/followers')
    console.log('获取粉丝列表响应:', response)
    // 检查response是否是数组
    const followers = Array.isArray(response) ? response : []
    console.log('粉丝列表:', followers)
    
    // 检查每个粉丝的关注状态和拉黑状态
    for (const follower of followers) {
      console.log(`处理粉丝: ${follower.username} (${follower.id})`)
      // 检查关注状态
      try {
        const followResponse = await api.get(`/api/users/${follower.id}/follow/status`)
        console.log(`关注状态响应 for ${follower.id}:`, followResponse)
        // 响应拦截器已经返回了boolean值
        follower.isFollowing = followResponse
        console.log(`关注状态 for ${follower.id}:`, follower.isFollowing)
      } catch (error) {
        console.error(`Failed to check follow status for user ${follower.id}:`, error)
        follower.isFollowing = false
      }
      
      // 检查拉黑状态
      try {
        const blockResponse = await api.get(`/api/chat/blacklist/check?userId=${follower.id}`)
        console.log(`拉黑状态响应 for ${follower.id}:`, blockResponse)
        // 响应拦截器已经返回了boolean值
        follower.isBlocked = blockResponse
        console.log(`拉黑状态 for ${follower.id}:`, follower.isBlocked)
      } catch (error) {
        console.error(`Failed to check block status for user ${follower.id}:`, error)
        follower.isBlocked = false
      }
    }
    
    console.log('处理后的粉丝列表:', followers)
    // 直接更新列表数据，避免页面闪烁
    myFollowers.value = followers
    console.log('最终粉丝列表:', myFollowers.value)
  } catch (error) {
    console.error('Failed to load followers:', error)
    myFollowers.value = []
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadDocuments()
}

// 处理每页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
  loadDocuments()
}

// 处理标签分页变化
const handleTagPageChange = (page) => {
  currentTagPage.value = page
}

// 处理标签每页大小变化
const handleTagSizeChange = (size) => {
  tagPageSize.value = size
  currentTagPage.value = 1 // 重置到第一页
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
    setTimeout(() => {
      activeMenu.value = 'content'
    }, 100)
  } else if (command === 'profile') {
    router.push('/content-management')
    setTimeout(() => {
      activeMenu.value = 'profile'
    }, 100)
  }
}

const handleMenuSelect = (key) => {
  activeMenu.value = key
  // 根据选择的菜单项加载对应数据
  if (key === 'content') {
    loadDocuments()
  } else if (key === 'comments') {
    activeCommentTab.value = 'my'
    loadMyComments()
  } else if (key === 'tags') {
    loadTags()
  } else if (key === 'labels') {
    // 加载粉丝列表
    activeFollowerTab.value = 'followers'
    loadFollowers()
  } else if (key === 'profile') {
    loadAddresses()
  } else if (key === 'address') {
    // 地址管理，加载地址列表
    loadAddresses()
  } else if (key === 'account') {
    // 账号设置
  }
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
    userStats.value = {
      original: 0,
      likes: 0,
      collections: 0
    }
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

const loadUserInfo = async () => {
  try {
    console.log('开始加载用户信息...')
    const response = await api.get('/api/users/info')
    console.log('用户信息响应:', response)
    
    // 同时更新authStore中的用户信息，包括头像
    if (response && authStore.user) {
      const updates = {}
      
      // 只更新头像，不覆盖其他数据
      if (response.avatar !== null && response.avatar !== undefined) {
        updates.avatar = response.avatar
      }
      
      // 同时更新其他个人资料字段，只更新有值的字段
      if (response.gender !== null && response.gender !== undefined) updates.gender = response.gender
      if (response.bio !== null && response.bio !== undefined) updates.bio = response.bio
      if (response.location !== null && response.location !== undefined) updates.location = response.location
      if (response.birthDate !== null && response.birthDate !== undefined) updates.birthDate = response.birthDate
      if (response.startWorkDate !== null && response.startWorkDate !== undefined) updates.startWorkDate = response.startWorkDate
      if (response.school !== null && response.school !== undefined) updates.school = response.school
      if (response.major !== null && response.major !== undefined) updates.major = response.major
      if (response.enrollmentDate !== null && response.enrollmentDate !== undefined) updates.enrollmentDate = response.enrollmentDate
      if (response.education !== null && response.education !== undefined) updates.education = response.education
      
      if (Object.keys(updates).length > 0) {
        console.log('准备更新用户:', updates)
        authStore.updateUser(updates)
      }
      
      console.log('更新后的用户:', authStore.user)
      console.log('头像:', authStore.user.avatar)
    }
  } catch (error) {
    console.error('Failed to load user info:', error)
  }
}

// 加载作品数据
const loadWorkStats = async () => {
  try {
    const response = await api.get('/api/users/stats')
    if (response) {
      workStats.value = {
        totalArticles: response.original || 0,
        totalLikes: response.likes || 0,
        totalComments: response.comments || 0,
        totalViews: response.views || 0,
        totalCollections: response.collections || 0
      }
    }
  } catch (error) {
    console.error('Failed to load work stats:', error)
  }
}

onMounted(async () => {
  const startTime = performance.now()
  
  try {
    // 调试：检查 localStorage 和 authStore 中的用户数据
    console.log('=== 页面初始化调试 ===')
    const localStorageUser = JSON.parse(localStorage.getItem('user') || 'null')
    console.log('localStorage 中的用户:', localStorageUser)
    console.log('authStore.user:', authStore.user)
    console.log('authStore.user?.avatar:', authStore.user?.avatar)
    
    // 第一步：并行加载关键数据（用于页面初始渲染）
    const keyDataPromise = Promise.all([
      loadCategories(),
      loadUserInfo()  // 从后端获取完整用户资料
    ])
    
    // 第二步：并行加载文档和标签数据
    const contentDataPromise = Promise.all([
      loadDocuments(),
      loadTags(),
      loadLabels()
    ])
    
    // 等待关键数据加载完成
    await keyDataPromise
    
    // 第三步：在后台异步加载剩余数据，不阻塞页面渲染
    Promise.all([
      contentDataPromise,
      loadUserStats(),
      loadUnreadMessageCount(),
      loadFollowers(),
      loadFollowing(),
      loadFriends(),
      loadBlacklist(),
      loadWorkStats(),
      loadTrendData(7),
      loadFollowerStats(),
      loadSummaryStats(),
      fetchEducationOptions()
    ]).catch(err => {
      console.error('加载后台数据失败:', err)
    })
    
    const endTime = performance.now()
    console.log(`内容管理页关键数据加载完成，耗时: ${(endTime - startTime).toFixed(2)}ms`)
  } catch (err) {
    console.error('加载初始化数据失败:', err)
  }
})
</script>

<style scoped lang="scss">
.content-management-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  
  .el-container {
    flex: 1;
    display: flex;
  }
  
  .el-container:first-child {
    flex-shrink: 0;
  }
  
  .el-container:nth-child(2) {
    flex: 1;
    overflow: hidden;
  }
}

.el-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  min-height: 60px;
  height: auto;
  
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
      flex-wrap: wrap;
    }
  }
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
      
      .el-sub-menu {
        .el-sub-menu__title {
          height: 48px;
          line-height: 48px;
          margin: 0 10px;
          border-radius: 8px;
          
          &:hover {
            background-color: #f5f7fa;
          }
        }
        
        .el-menu-item {
          height: 40px;
          line-height: 40px;
          padding-left: 50px !important;
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

.content-management-content {
  padding: 20px;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  
  h2 {
    margin: 0 0 20px 0;
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }
  
  .management-content {
    min-height: 400px;
    width: 100%;
    height: 100%;
  }
}
  
.content-section {
  margin-bottom: 30px;
}
  
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
  
.bold-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}
  
.title-text {
  font-size: 16px;
  font-weight: 500;
  color: #667eea;
  text-decoration: none;
  
  &:hover {
    color: #5a67d8;
  }
}
  
.article-time {
  font-size: 14px;
  color: #999;
}
  
.el-table th {
  font-size: 14px;
  font-weight: 600;
}
  
.el-table td {
  font-size: 14px;
}
  
.el-button {
  font-size: 13px;
}
  
.works-data,
.income-data,
.followers-data,
.summary-data {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.works-data-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.works-data-grid .stat-item {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100px;
}

.works-data-grid .stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.works-data-grid .stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.info-icon {
  margin-left: 5px;
  font-size: 14px;
  color: #999;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.mt-4 {
  margin-top: 20px;
}

.trend-section {
  margin-top: 20px;
}

.trend-tabs {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.trend-tabs :deep(.el-tabs) {
  width: 100%;
}

.trend-tabs :deep(.el-tabs__content) {
  width: 100%;
}

.trend-tabs :deep(.el-tab-pane) {
  width: 100%;
}

.trend-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 20px;
  width: 100%;
}

.time-range {
  margin-bottom: 20px;
  width: 100%;
}

.data-types {
  margin-bottom: 20px;
  width: 100%;
}

.chart-container {
  height: 300px;
  background-color: #f9f9f9;
  border-radius: 8px;
  position: relative;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.chart-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.y-axis {
  position: absolute;
  left: 20px;
  top: 20px;
  bottom: 60px;
  width: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  
  .y-label {
    font-size: 11px;
    color: #999;
    text-align: right;
    padding-right: 8px;
  }
}

.chart-area {
  position: relative;
  flex: 1;
  margin-left: 50px;
  margin-bottom: 10px;
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  
  .grid-line {
    height: 1px;
    background-color: #e8e8e8;
  }
}

.line-chart {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: calc(100% - 30px);
}

.x-axis {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  display: flex;
  justify-content: space-between;
  
  .x-label {
    font-size: 11px;
    color: #999;
    text-align: center;
  }
}

.chart-legend {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  padding-left: 50px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-text {
  font-size: 12px;
  color: #666;
}

.legend-value {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

.data-list-container {
  width: 100%;
}




  
.label-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}
  
.article-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
  
.label {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
  
.label.original {
  background-color: #fff2e8;
  color: #ff7a45;
}
  
.label.high-quality {
  background-color: #f6ffed;
  color: #52c41a;
}
  
.label.vip {
  background-color: #e6f7ff;
  color: #1890ff;
}
  
.label.fan-only {
  background-color: #f9f0ff;
  color: #722ed1;
}
  
.title-text {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
  
.article-time {
  font-size: 12px;
  color: #999;
}
  
.el-table .cell {
  white-space: normal;
  line-height: 1.4;
}
  
.el-table__row {
  height: auto;
}
  
.el-table__row td {
  padding: 12px 0;
}
  
.article-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
  
.label {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
  
.label.original {
  background-color: #fff2e8;
  color: #ff7a45;
}
  
.label.high-quality {
  background-color: #f6ffed;
  color: #52c41a;
}
  
.label.vip {
  background-color: #e6f7ff;
  color: #1890ff;
}
  
.label.fan-only {
  background-color: #f9f0ff;
  color: #722ed1;
}
  
.title-text {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
  
.article-time {
  font-size: 12px;
  color: #999;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.comment-content, .document-title {
  cursor: pointer;
  color: #667eea;
  
  &:hover {
    text-decoration: underline;
  }
}

.document-stats-panel {
  padding: 20px;
  
  h3 {
    margin-top: 0;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .stat-item {
    text-align: center;
    
    .stat-label {
      display: block;
      font-size: 12px;
      color: #999;
      margin-bottom: 8px;
    }
    
    .stat-value {
      display: block;
      font-size: 20px;
      font-weight: 600;
      color: #333;
    }
  }
}

.dropdown-item-with-badge {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-header {
  margin: 0 0 16px 0;
  padding: 0;
  text-align: left;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.trend-tabs {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.trend-tabs :deep(.el-tabs) {
  width: 100%;
}

.trend-tabs :deep(.el-tabs__content) {
  width: 100%;
}

.trend-tabs :deep(.el-tab-pane) {
  width: 100%;
}

.trend-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 20px;
  width: 100%;
}

.time-range {
  margin-bottom: 20px;
  width: 100%;
}

.data-types {
  margin-bottom: 20px;
  width: 100%;
}

.chart-container {
  height: 300px;
  background-color: #f9f9f9;
  border-radius: 8px;
  position: relative;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.chart-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.y-axis {
  position: absolute;
  left: 20px;
  top: 20px;
  bottom: 60px;
  width: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  
  .y-label {
    font-size: 11px;
    color: #999;
    text-align: right;
    padding-right: 8px;
  }
}

.chart-area {
  position: relative;
  flex: 1;
  margin-left: 50px;
  margin-bottom: 10px;
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  
  .grid-line {
    height: 1px;
    background-color: #e8e8e8;
  }
}

.line-chart {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: calc(100% - 30px);
}

.x-axis {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  display: flex;
  justify-content: space-between;
  
  .x-label {
    font-size: 11px;
    color: #999;
    text-align: center;
  }
}

.chart-legend {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  padding-left: 50px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-text {
  font-size: 12px;
  color: #666;
}

.legend-value {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

.data-list-container {
  width: 100%;
}

.message-badge {
  margin-left: 8px;
}

.data-point {
  cursor: pointer;
  transition: r 0.2s ease;
}

.data-point:hover {
  r: 6;
}

.data-point-hover {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.data-point:hover + .data-point-hover {
  opacity: 1;
}

.chart-tooltip {
  position: absolute;
  background: white;
  border: 1px solid;
  border-radius: 6px;
  padding: 8px 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  font-size: 12px;
  pointer-events: none;
  z-index: 100;
  min-width: 100px;
}

.tooltip-date {
  color: #999;
  font-size: 11px;
  margin-bottom: 4px;
}

.tooltip-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.tooltip-type {
  color: #666;
}

.tooltip-value {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.personal-tabs {
  margin-top: 20px;
}

.profile-section,
.account-section {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
}

/* 头像区域样式 - 行内 */
.avatar-inline-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  transition: all 0.3s ease;
}

.avatar-inline-wrapper:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-avatar-inline {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-inline-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
  font-size: 24px;
}

.avatar-inline-wrapper:hover .avatar-inline-overlay {
  opacity: 1;
}

.avatar-upload-input {
  display: none;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  cursor: pointer;
}

.user-avatar-inline {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.upload-icon {
  font-size: 24px;
  color: white;
}

.upload-text {
  font-size: 12px;
  color: white;
  margin-top: 4px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  position: relative;
  z-index: 1;
  border-bottom: 1px solid #f5f7fa;
}

.info-label {
  width: 100px;
  color: #606266;
  flex-shrink: 0;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}

.account-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.account-label {
  width: 120px;
  color: #606266;
  flex-shrink: 0;
}

.account-value {
  flex: 1;
  color: #303133;
}

.edit-input {
  width: 200px;
  margin-right: 10px;
}

.edit-btn {
  padding: 4px 12px;
  background: #e8f4fd;
  color: #409eff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.edit-btn:hover {
  background: #d4eafd;
}

/* 地区选择器样式 */
.location-picker {
  margin-top: 15px;
}

.location-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.location-display:hover {
  border-color: #667eea;
  background-color: #fff;
}

.location-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.location-picker.open .location-arrow {
  transform: rotate(180deg);
}

.location-dropdown {
  margin-top: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.location-columns {
  display: flex;
  height: 240px;
}

.location-column {
  flex: 1;
  overflow-y: auto;
  border-right: 1px solid #e4e7ed;
}

.location-column:last-child {
  border-right: none;
}

.location-item {
  padding: 12px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
}

.location-item:hover {
  background-color: #f5f7fa;
}

.location-item.active {
  background-color: #667eea;
  color: #fff;
}

/* 学历选择器样式 */
.education-picker {
  margin-top: 15px;
}

.education-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.education-display:hover {
  border-color: #667eea;
  background-color: #fff;
}

.education-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s ease;
}

.education-picker.open .education-arrow {
  transform: rotate(180deg);
}

.education-dropdown {
  margin-top: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-height: 240px;
  overflow-y: auto;
}

.education-item {
  padding: 12px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
}

.education-item:hover {
  background-color: #f5f7fa;
}

.education-item.active {
  background-color: #667eea;
  color: #fff;
}

/* 日期选择器样式 */
.date-picker {
  margin-top: 15px;
}

.date-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.date-display:hover {
  border-color: #667eea;
  background-color: #fff;
}

.date-icon {
  font-size: 18px;
}

.date-dropdown {
  margin-top: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.date-nav {
  background: none;
  border: none;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.date-nav:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.date-title {
  font-weight: 600;
  font-size: 15px;
}

.date-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  padding: 8px 0;
  background-color: #f5f7fa;
}

.weekday {
  text-align: center;
  padding: 8px;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.date-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  padding: 8px;
}

.date-day {
  text-align: center;
  padding: 10px 4px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  color: #606266;
}

.date-day:hover:not(.other-month):not(.disabled) {
  background-color: #e8ecf0;
}

.date-day.other-month {
  color: #c0c4cc;
}

.date-day.today {
  background-color: #667eea;
  color: #fff;
}

.date-day.selected {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
}

.date-day.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.date-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
}

.date-btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background-color: #fff;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.date-btn:hover {
  background-color: #f5f7fa;
}

.date-btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;
}

.date-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 性别选项样式 */
.gender-options {
  display: flex;
  gap: 40px;
  margin-top: 15px;
}

.gender-option {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  padding: 12px 24px;
  border-radius: 8px;
  transition: all 0.3s ease;
  min-width: 80px;
  justify-content: center;
}

.gender-option:hover {
  background-color: #f5f7fa;
}

.gender-option input[type="radio"] {
  width: 22px;
  height: 22px;
  accent-color: #ff6b6b;
  cursor: pointer;
}

.gender-option span {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

/* 自定义弹窗样式 */
.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.custom-modal {
  background-color: #fff;
  border-radius: 12px;
  width: 420px;
  max-width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.custom-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  padding: 0;
  line-height: 1;
  transition: color 0.3s ease;
}

.modal-close:hover {
  color: #fff;
}

.custom-modal-body {
  padding: 24px;
}

.custom-modal-body label {
  display: block;
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.modal-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.modal-input:focus {
  outline: none;
  border-color: #667eea;
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.modal-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  resize: vertical;
  transition: all 0.3s ease;
  background-color: #fafafa;
  min-height: 100px;
}

.modal-textarea:focus {
  outline: none;
  border-color: #667eea;
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.custom-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  background-color: #fafafa;
}

.modal-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.modal-btn-cancel {
  background-color: #fff;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.modal-btn-cancel:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

.modal-btn-confirm {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.modal-btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

/* 地址管理样式 */
.address-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-btn {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn:hover {
  background: #67b8ff;
}

.address-list {
  display: grid;
  gap: 16px;
}

.address-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: white;
  transition: all 0.3s;
}

.address-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.address-card.default {
  border-color: #409eff;
  background: #f0f7ff;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  gap: 16px;
}

.name {
  font-weight: bold;
  font-size: 16px;
}

.phone {
  color: #666;
}

.default-tag {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.address-detail {
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.default-btn {
  background: #fff7e6;
  color: #e6a23c;
}

.default-btn:hover {
  background: #ffeeba;
}

.delete-btn {
  background: #fef0f0;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #fde2e2;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #fafafa;
  border-radius: 8px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  margin-bottom: 16px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.modal {
  background: linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  width: 90%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(255, 255, 255, 0.8);
  animation: modalFadeIn 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.close-btn {
  font-size: 28px;
  cursor: pointer;
  color: #909399;
  line-height: 1;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  color: #606266;
  background: #f5f7fa;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  letter-spacing: 0.3px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  font-size: 15px;
  box-sizing: border-box;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  font-family: inherit;
}

.form-group input:hover,
.form-group textarea:hover {
  border-color: #d0d0d0;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.12), 0 0 20px rgba(102, 126, 234, 0.08);
  background: white;
  transform: translateY(-1px);
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input-wrapper input {
  flex: 1;
}

.code-button {
  padding: 12px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.code-button:hover:not(:disabled) {
  background: #f5f7fa;
  border-color: #667eea;
}

.code-button:disabled {
  cursor: not-allowed;
  color: #c0c4cc;
  background: #f5f7fa;
}

.form-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
}

.form-actions button {
  flex: 1;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.form-actions button[type="primary"] {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 18px rgba(102, 126, 234, 0.45);
}

.form-actions button[type="primary"]:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 22px rgba(102, 126, 234, 0.5);
}

.form-actions button[type="primary"]:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.4);
}

.form-actions button[type="button"] {
  background: white;
  color: #606266;
  border: 2px solid #e8e8e8;
}

.form-actions button[type="button"]:hover {
  background: #fafafa;
  border-color: #d0d0d0;
  transform: translateY(-1px);
}

.form-actions button[type="button"]:active {
  transform: translateY(0);
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-group input {
  width: auto;
}

.forgot-password-link {
  display: inline-block;
  color: #667eea;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
  padding: 6px 0;
}

.forgot-password-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.login-records-modal {
  max-width: 600px;
}

.login-records-list {
  max-height: 400px;
  overflow-y: auto;
}

.login-record-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.login-record-item:hover {
  background-color: #fafafa;
}

.login-record-item:last-child {
  border-bottom: none;
}

.record-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
}

.record-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.record-label {
  font-size: 13px;
  color: #909399;
}

.record-value {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  max-width: 200px;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.address-display {
  min-width: 200px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.address-content {
  flex: 1;
}

.address-display-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.address-display-name {
  font-weight: 500;
}

.address-display-phone {
  color: #999;
  font-size: 14px;
}

.address-display-default {
  background: #409eff;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
}

.address-display-detail {
  color: #999;
  font-size: 13px;
  line-height: 1.4;
}

.address-selector-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 300px;
  overflow-y: auto;
}

.address-selector-item {
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;
}

.address-selector-item:last-child {
  border-bottom: none;
}

.address-selector-item:hover {
  background: #f5f7fa;
}

.address-selector-item.active {
  background: #e8f4fd;
}

.address-selector-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.address-selector-name {
  font-weight: 500;
}

.address-selector-phone {
  color: #999;
  font-size: 14px;
}

.address-selector-default {
  background: #409eff;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
}

.address-selector-item-detail {
  color: #999;
  font-size: 13px;
  line-height: 1.4;
}

/* 个人资料页面地址选择弹窗样式 */
.picker-address-card {
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s;
  cursor: pointer;
}

.picker-address-card:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.picker-address-card.active {
  border-color: #409eff;
  background: #e8f4fd;
}

.picker-address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.picker-address-name {
  font-weight: 500;
  font-size: 15px;
}

.picker-address-phone {
  color: #666;
  font-size: 14px;
}

.picker-address-default {
  background: #409eff;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: auto;
}

.picker-address-detail {
  color: #999;
  font-size: 14px;
  line-height: 1.5;
}

.picker-modal {
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.picker-modal .modal-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.picker-address-list {
  flex: 1;
  overflow-y: auto;
  max-height: 300px;
}

</style>