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
                  <el-icon><Currency /></el-icon>
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
import { QuestionFilled, Download } from '@element-plus/icons-vue'

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
    'summary': '一周小结'
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
      await api.post('/api/tags', form.value)
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
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
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
    const response = await api.get('/api/users/info')
    // 同时更新authStore中的用户信息，包括头像
    if (response && authStore.user) {
      authStore.user.avatar = response.avatar
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
    // 第一步：并行加载关键数据（用于页面初始渲染）
    const keyDataPromise = Promise.all([
      loadUserInfo(),
      loadCategories()
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
      loadSummaryStats()
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
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.stat-item {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
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
</style>