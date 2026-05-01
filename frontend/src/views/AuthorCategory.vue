<template>
  <div class="author-category-container">
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
            <span class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar">{{ authStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
              <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="documents">我的文档</el-dropdown-item>
                <el-dropdown-item command="public">公开文档</el-dropdown-item>
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
        <!-- 左侧边栏 -->
        <div class="content-left">
          <!-- 作者信息卡片 -->
          <el-card class="author-card">
            <template #header>
              <div class="card-header">
                <span>作者信息</span>
              </div>
            </template>
            <div class="author-profile">
              <el-avatar :size="64" class="author-avatar" :src="authorInfo.avatar">{{ authorName.charAt(0) }}</el-avatar>
              <h3 class="author-name">{{ authorName }}</h3>
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
              </div>
              <div class="author-follow-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ followStats.following || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ followStats.followers || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
              </div>
              <div class="author-actions" v-if="authStore.isAuthenticated && authStore.user?.id !== parseInt(route.params.userId)">
                <el-button
                  :type="isFollowing ? 'default' : 'primary'"
                  @click="handleFollow"
                  class="follow-button"
                >
                  {{ isFollowing ? '取消关注' : '关注' }}
                </el-button>
              </div>
            </div>
          </el-card>
          
          <!-- 文章分类 -->
          <el-card class="category-card">
            <template #header>
              <div class="card-header">
                <span>文章分类</span>
              </div>
            </template>
            <div class="category-list">
              <div class="category-item" v-for="item in authorDocuments" :key="item.category">
                <a :href="`/author/${route.params.userId}/category/${item.category}`" :class="['category-link', { active: item.category === category }]">
                  {{ item.category }}
                </a>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 右侧内容区 -->
        <div class="content-right">
          <div class="content">
            <h1 class="page-title">{{ authorName || '管理员' }}的{{ category }}文章</h1>
            
            <el-table :data="documents" v-loading="loading" style="width: 100%">
              <el-table-column prop="title" label="标题" min-width="200">
                <template #default="{ row }">
                  <router-link :to="`/document/${row.id}`" class="doc-link">
                    {{ row.title }}
                  </router-link>
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
                    :type="getPermissionType(row.permission)" 
                    size="small"
                  >
                    {{ getPermissionText(row.permission) }}
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
                  <el-button link type="primary" @click="viewDocument(row.id)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div v-if="documents.length === 0 && !loading" class="no-documents">
              暂无该分类的文章
            </div>
            
            <!-- 分页组件 -->
            <div class="pagination-container" v-if="documents.length > 0">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 生成用户头像URL
const getAvatarUrl = (seed) => {
  if (!seed) return ''
  return `https://api.dicebear.com/7.x/pixel-art/svg?seed=${seed}&backgroundColor=ff9ff3,ffeaa7,74b9ff,55efc4,feca57`
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const documents = ref([])
const authorName = ref('')
const category = ref('')
const authorInfo = ref({})
const authorDocuments = ref([])
const isFollowing = ref(false)
const followStats = ref({
  following: 0,
  followers: 0
})

// 计算属性，用于获取页面标题
const pageTitle = computed(() => {
  return `${authorName.value || '未知用户'}的${category.value}文章`
})

// 计算属性，用于判断当前登录用户是否与作者是同一人
const isCurrentUser = computed(() => {
  return authStore.user?.id === authorInfo.value.userId
})

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadDocuments = async () => {
  const userId = route.params.userId
  category.value = route.params.category
  
  if (!userId || !category.value) {
    ElMessage.error('参数错误')
    router.push('/documents')
    return
  }
  
  loading.value = true
  try {
    // 加载作者信息
    const authorResponse = await api.get(`/api/users/${userId}/stats`)
    authorName.value = authorResponse.nickname || authorResponse.username || '未知用户'
    authorInfo.value = authorResponse
    console.log('作者信息:', authorResponse)
    console.log('authorName:', authorName.value)
    
    // 加载关注统计
    const followStatsResponse = await api.get(`/api/users/${userId}/follow/count`)
    followStats.value = followStatsResponse
    console.log('关注统计:', followStatsResponse)
    
    // 检查关注状态
    if (authStore.isAuthenticated) {
      const followStatusResponse = await api.get(`/api/users/${userId}/follow/status`)
      isFollowing.value = followStatusResponse
      console.log('关注状态:', followStatusResponse)
    }
    
    // 加载作者该分类的文章（带分页）
    const documentsResponse = await api.get(`/api/documents/author/${userId}/category/${category.value}`, {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    console.log('文章数据:', documentsResponse)
    
    // 假设后端返回的数据结构为 { records: [...], total: 100 }
    if (documentsResponse.records) {
      documents.value = documentsResponse.records
      total.value = documentsResponse.total
    } else {
      // 兼容旧数据结构
      documents.value = documentsResponse
      total.value = documentsResponse.length
    }
    
    // 加载作者的其他文章（按分类）
    const authorDocsResponse = await api.get(`/api/documents/author/${userId}`)
    authorDocuments.value = authorDocsResponse
    console.log('作者其他文章:', authorDocsResponse)
  } catch (error) {
    console.error('加载作者分类文章失败:', error)
    console.error('错误详情:', error.response)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 分页处理方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadDocuments()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadDocuments()
}

const viewDocument = (id) => {
  router.push(`/document/${id}`)
}

const goBack = () => {
  router.back()
}

const handleCommand = (command) => {
  switch (command) {
    case 'documents':
      router.push('/documents')
      break
    case 'public':
      router.push('/public-documents')
      break
    case 'logout':
      authStore.logout()
      router.push('/login')
      break
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

const handleFollow = async () => {
  const userId = route.params.userId
  if (!authStore.isAuthenticated) {
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
      await api.delete(`/api/users/${userId}/follow`)
      isFollowing.value = false
      followStats.value.followers--
      ElMessage.success('取消关注成功')
    } else {
      await api.post(`/api/users/${userId}/follow`)
      isFollowing.value = true
      followStats.value.followers++
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped lang="scss">
/* 全局样式 */
.author-category-container {
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
    
    /* 左侧边栏 */
    .content-left {
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
          
          .author-avatar {
            margin: 0 auto 15px;
            border: 3px solid #f0f0f0;
          }
          
          .author-name {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin: 0 0 15px 0;
          }
          
          .author-stats {
            display: flex;
            justify-content: space-around;
            margin-bottom: 15px;
            
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
          
          .author-follow-stats {
            display: flex;
            justify-content: space-around;
            margin-bottom: 20px;
            
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
          
          .author-actions {
            display: flex;
            justify-content: center;
            
            .follow-button {
              border-radius: 20px;
              padding: 8px 24px;
            }
          }
        }
      }
      
      /* 文章分类卡片 */
      .category-card {
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
        
        .category-list {
          padding: 15px 16px;
          
          .category-item {
            margin-bottom: 10px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .category-link {
              color: #333;
              text-decoration: none;
              font-size: 14px;
              display: block;
              padding: 8px 12px;
              border-radius: 4px;
              transition: all 0.3s ease;
              
              &:hover {
                color: #1677ff;
                background: #f0f9ff;
              }
              
              &.active {
                color: #1677ff;
                background: #e6f7ff;
                font-weight: 500;
              }
            }
          }
        }
      }
    }
    
    /* 右侧内容区 */
    .content-right {
      flex: 1;
      
      .content {
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        padding: 30px;
        
        .page-title {
          margin: 0 0 30px 0;
          font-size: 24px;
          font-weight: 600;
          color: #333;
        }
        
        .no-documents {
          text-align: center;
          color: #999;
          padding: 50px 0;
          font-size: 16px;
        }
        
        .doc-link {
          color: #1677ff;
          text-decoration: none;
          
          &:hover {
            text-decoration: underline;
          }
        }
        
        .pagination-container {
          margin-top: 30px;
          display: flex;
          justify-content: flex-end;
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
      
      .content-left {
        width: 100%;
        
        .author-card {
          .author-profile {
            .author-avatar {
              margin: 0 auto 10px;
            }
            
            .author-name {
              font-size: 14px;
              margin: 0 0 10px 0;
            }
            
            .author-stats {
              .stat-item {
                .stat-value {
                  font-size: 16px;
                }
              }
            }
          }
        }
        
        .category-card {
          .category-list {
            .category-item {
              .category-link {
                font-size: 13px;
                padding: 6px 10px;
              }
            }
          }
        }
      }
      
      .content-right {
        .content {
          .page-title {
            font-size: 20px;
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
        .author-card {
          .author-profile {
            padding: 15px;
          }
        }
        
        .category-card {
          .category-list {
            padding: 10px 12px;
          }
        }
      }
      
      .content-right {
        .content {
          padding: 20px;
          
          .page-title {
            font-size: 18px;
          }
        }
      }
    }
  }
}
</style>