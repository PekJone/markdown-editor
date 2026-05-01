<template>
  <div class="editor-container">
    <div class="editor-header">
      <div class="header-left">
        <el-button @click="goBack" circle>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <el-input
          v-model="document.title"
          placeholder="文档标题"
          class="title-input"
          @change="autoSave"
        />
      </div>
      <div class="header-right">
        <el-select v-model="document.category" placeholder="分类" @change="handleCategoryChange" style="width: 120px">
          <el-option v-for="category in categories" :key="category.dictCode" :label="category.dictLabel" :value="category.dictCode" />
        </el-select>
        <div class="tags-container">
          <el-select 
            v-model="document.tags" 
            placeholder="选择标签"
            style="width: 150px"
            @change="autoSave"
          >
            <el-option 
              v-for="tag in userTags" 
              :key="tag.id" 
              :label="tag.tagName" 
              :value="tag.tagName" 
            />
          </el-select>
          <el-button 
            type="primary" 
            size="small" 
            @click="showTagDialog = true"
            :disabled="userTags.length >= 20"
          >
            新建标签
          </el-button>
          <span v-if="userTags.length >= 20" class="tag-limit">
            已达标签上限(20个)
          </span>
        </div>
        <el-select v-model="document.permission" placeholder="权限" @change="autoSave" style="width: 150px">
          <el-option label="公开（所有人可见）" value="public" />
          <el-option label="私密（所有登录用户可见）" value="private" />
          <el-option label="绝密（仅自己可见）" value="secret" />
        </el-select>
        <el-dropdown @command="handleExport">
          <el-button type="primary">
            导出 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="html">导出为 HTML</el-dropdown-item>
              <el-dropdown-item command="pdf">导出为 PDF</el-dropdown-item>
              <el-dropdown-item command="markdown">导出为 Markdown</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="success" @click="saveDocument(true, true)">保存</el-button>
      </div>
    </div>

    <div class="editor-main">
      <div class="toolbar">
        <el-button-group>
          <el-button @click="undo" title="撤销">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
          <el-button @click="redo" title="重做">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
          <el-button @click="showHistory" title="历史">
            <el-icon><Timer /></el-icon>
          </el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="handleShowFormatMenu" title="格式">
            <el-icon><FontSize /></el-icon>
          </el-button>
          <el-button @click="insertFormat('**', '**')" title="加粗">
            <strong>B</strong>
          </el-button>
          <el-button @click="insertFormat('*', '*')" title="斜体">
            <em>I</em>
          </el-button>
          <el-button @click="insertFormat('~~', '~~')" title="删除线">
            <s>S</s>
          </el-button>
          <el-button @click="insertFormat('`', '`')" title="行内代码">
            &lt;/&gt;
          </el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="insertLine('# ')" title="一级标题">H1</el-button>
          <el-button @click="insertLine('## ')" title="二级标题">H2</el-button>
          <el-button @click="insertLine('### ')" title="三级标题">H3</el-button>
          <el-button @click="insertLine('#### ')" title="四级标题">H4</el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="insertLine('- ')" title="无序列表">
            <el-icon><List /></el-icon>
          </el-button>
          <el-button @click="insertLine('1. ')" title="有序列表">
            <el-icon><Finished /></el-icon>
          </el-button>
          <el-button @click="insertLine('> ')" title="引用">
            <el-icon><ChatLineSquare /></el-icon>
          </el-button>
          <el-button @click="insertCodeBlock" title="代码块">
            <el-icon><Document /></el-icon>
          </el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="handleShowColorMenu" title="颜色">
            <el-icon><Palette /></el-icon>
          </el-button>
          <el-button @click="handleShowBackgroundMenu" title="背景">
            <el-icon><Brush /></el-icon>
          </el-button>
          <el-button @click="handleShowAlignMenu" title="对齐">
            <el-icon><Position /></el-icon>
          </el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="insertLine('---')" title="分割线">
            <el-icon><Minus /></el-icon>
          </el-button>
          <el-button @click="insertTable" title="表格">
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button @click="insertLink" title="链接">
            <el-icon><Link /></el-icon>
          </el-button>
        </el-button-group>

        <el-button-group>
          <el-button @click="insertImage" title="图像">
            <el-icon><Picture /></el-icon>
          </el-button>
          <el-button @click="insertVideo" title="视频">
            <el-icon><Video /></el-icon>
          </el-button>
          <el-button @click="insertFormula" title="公式">
            <el-icon><Math /></el-icon>
          </el-button>
          <el-button @click="handleShowTemplateMenu" title="模板">
            <el-icon><Collection /></el-icon>
          </el-button>
          <el-button @click="insertTOC" title="目录">
            <el-icon><Menu /></el-icon>
          </el-button>
        </el-button-group>
      </div>

      <div class="editor-content">
        <div class="editor-pane">
          <div class="editor-toolbar">
            <el-button @click="toggleEditorMode" type="primary" size="small">
              {{ editorMode === 'edit' ? '预览' : '编辑' }}
            </el-button>
          </div>
          <div v-if="editorMode === 'edit'" class="edit-area">
            <textarea
              ref="editorRef"
              v-model="document.content"
              class="markdown-editor"
              placeholder="开始编写你的 Markdown 文档..."
              @input="handleInput"
            ></textarea>
          </div>
          <div v-else class="preview-content" v-html="renderedContent"></div>
        </div>
      </div>
    </div>
    
    <!-- 新建标签对话框 -->
    <el-dialog
      v-model="showTagDialog"
      title="新建标签"
      width="400px"
    >
      <el-form :model="newTag" label-width="80px">
        <el-form-item label="标签名称">
          <el-input 
            v-model="newTag.tagName" 
            placeholder="请输入标签名称"
            maxlength="20"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTagDialog = false">取消</el-button>
          <el-button type="primary" @click="createTag">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 表格配置对话框 -->
    <el-dialog
      v-model="showTableDialog"
      title="插入表格"
      width="400px"
    >
      <el-form :model="tableConfig" label-width="80px">
        <el-form-item label="行数">
          <el-input-number 
            v-model="tableConfig.rows" 
            :min="1" 
            :max="20"
            :step="1"
          />
        </el-form-item>
        <el-form-item label="列数">
          <el-input-number 
            v-model="tableConfig.cols" 
            :min="1" 
            :max="10"
            :step="1"
          />
        </el-form-item>
        <el-form-item label="表格标题">
          <el-input v-model="tableConfig.title" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTableDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmInsertTable">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 表格编辑器对话框 -->
    <el-dialog
      v-model="showTableEditorDialog"
      title="编辑表格"
      width="600px"
      destroy-on-close
    >
      <div class="table-editor">
        <table class="visual-table">
          <thead v-if="tableData.length > 0">
            <tr>
              <th v-for="(col, index) in tableData[0]" :key="index">
                <el-input v-model="tableData[0][index]" size="small" />
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, rowIndex) in tableData.slice(1)" :key="rowIndex">
              <td v-for="(cell, colIndex) in row" :key="colIndex">
                <el-input v-model="tableData[rowIndex + 1][colIndex]" size="small" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTableEditorDialog = false">取消</el-button>
          <el-button type="primary" @click="insertVisualTable">插入表格</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import api from '@/utils/api'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
// 图标已在main.js中全局注册，不需要单独导入
import 'highlight.js/styles/github.css'

const route = useRoute()
const router = useRouter()
const editorRef = ref(null)
const document = ref({
  id: null,
  title: '未命名文档',
  content: '',
  category: '',
  tags: '',
  permission: 'private'
})
const autoSaveTimer = ref(null)
// 初始化为默认分类，确保下拉框有选项
const categories = ref([
  { dictCode: 'tech', dictLabel: '技术' },
  { dictCode: 'life', dictLabel: '生活' },
  { dictCode: 'work', dictLabel: '工作' },
  { dictCode: 'entertainment', dictLabel: '娱乐' },
  { dictCode: 'other', dictLabel: '其他' }
])
const userTags = ref([])
const showTagDialog = ref(false)
const newTag = ref({
  tagName: ''
})
const showTableDialog = ref(false)
const showTableEditorDialog = ref(false)
const tableConfig = ref({
  rows: 3,
  cols: 3,
  title: ''
})
const tableData = ref([])
const editorMode = ref('edit') // 'edit' 或 'preview'

// 历史记录
const history = ref([])
const historyIndex = ref(-1)

// 菜单状态
const showFormatMenu = ref(false)
const showColorMenu = ref(false)
const showBackgroundMenu = ref(false)
const showAlignMenu = ref(false)
const showTemplateMenu = ref(false)

// 颜色选项
const colors = [
  '#000000', '#FF0000', '#00FF00', '#0000FF',
  '#FFFF00', '#FF00FF', '#00FFFF', '#808080',
  '#FFA500', '#800080', '#008000', '#000080'
]

// 模板选项
const templates = [
  { name: '空文档', content: '' },
  { name: '技术博客', content: '# 技术博客\n\n## 简介\n\n## 实现\n\n## 总结\n' },
  { name: '读书笔记', content: '# 读书笔记\n\n## 书籍信息\n\n## 核心观点\n\n## 个人感悟\n' },
  { name: '项目计划', content: '# 项目计划\n\n## 项目概述\n\n## 时间安排\n\n## 任务分配\n' }
]

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
  console.log('开始加载文档，文档ID:', id)
  
  // 如果没有文档ID，说明是新建文档
  if (!id) {
    console.log('新建文档，初始化默认值')
    // 初始化一个新的文档对象
    document.value = {
      id: null,
      title: '未命名文档',
      content: '',
      category: '',
      tags: '',
      permission: 'private'
    }
    console.log('初始化新文档:', document.value)
    return
  }
  
  try {
    console.log('发送请求获取文档数据...')
    const docData = await api.get(`/api/documents/${id}`)
    console.log('获取文档数据成功，数据:', docData)
    
    console.log('加载文档数据:', docData)
    console.log('当前分类列表:', categories.value)
    
    // 检查当前用户是否是文章的创建者
    console.log('发送请求获取用户信息...')
    const userData = await api.get('/api/users/info')
    console.log('获取用户信息成功，数据:', userData)
    const currentUserId = userData.id
    
    console.log('当前用户ID:', currentUserId)
    console.log('文档创建者ID:', docData.userId)
    
    if (docData.userId !== currentUserId) {
      console.error('用户不是文档的创建者')
      ElMessage.error('只有自己创建的文章才可以编辑')
      router.push(`/document/${id}`)
      return
    }
    
    // 先创建一个新对象，然后替换整个document.value
    const newDocument = {
      id: docData.id,
      title: docData.title || '未命名文档',
      content: docData.content || '',
      category: docData.category || '',
      tags: docData.tags || '',
      permission: docData.permission || 'private',
      userId: docData.userId,
      viewCount: docData.viewCount,
      likeCount: docData.likeCount,
      createdAt: docData.createdAt,
      updatedAt: docData.updatedAt
    }
    
    // 替换整个document.value
    document.value = newDocument
    
    console.log('设置后document.value:', document.value)
    console.log('document.value.category:', document.value.category)
    console.log('document.value.permission:', document.value.permission)
    console.log('categories.value:', categories.value)
    
    // 检查分类是否匹配
    if (document.value.category) {
      const matchedCategory = categories.value.find(cat => cat.dictCode === document.value.category)
      console.log('匹配的分类:', matchedCategory)
    }
    
    // 检查权限是否匹配
    const permissions = ['public', 'private', 'secret']
    if (document.value.permission) {
      const matchedPermission = permissions.includes(document.value.permission)
      console.log('匹配的权限:', matchedPermission)
    }
    
    // 使用nextTick确保DOM更新
    await nextTick()
    console.log('DOM更新完成，分类值:', document.value.category)
    console.log('DOM更新完成，权限值:', document.value.permission)
  } catch (error) {
    console.error('加载文档失败:', error)
    console.error('错误响应:', error.response)
    console.error('错误消息:', error.message)
    
    // 根据错误类型显示不同的错误提示
    if (error.response) {
      // 服务器返回了错误响应
      if (error.response.status === 404) {
        ElMessage.error('文档不存在或已删除')
      } else if (error.response.status === 403) {
        ElMessage.error('无权限访问该文档')
      } else {
        ElMessage.error('加载文档失败: ' + (error.response.data?.message || '服务器错误'))
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('加载文档失败: 服务器无响应')
    } else {
      // 其他错误
      ElMessage.error('加载文档失败: ' + error.message)
    }
    
    // 跳转到文档列表页面
    router.push('/documents')
  }
}

const loadCategories = async () => {
  console.log('开始加载分类列表...')
  try {
    // 直接使用默认分类，跳过API调用
    categories.value = [
      { dictCode: 'tech', dictLabel: '技术' },
      { dictCode: 'life', dictLabel: '生活' },
      { dictCode: 'work', dictLabel: '工作' },
      { dictCode: 'entertainment', dictLabel: '娱乐' },
      { dictCode: 'other', dictLabel: '其他' }
    ]
    console.log('使用默认分类:', categories.value)
  } catch (error) {
    console.error('加载分类失败:', error)
    // 确保categories有默认值
    categories.value = [
      { dictCode: 'tech', dictLabel: '技术' },
      { dictCode: 'life', dictLabel: '生活' },
      { dictCode: 'work', dictLabel: '工作' },
      { dictCode: 'entertainment', dictLabel: '娱乐' },
      { dictCode: 'other', dictLabel: '其他' }
    ]
    console.log('使用默认分类:', categories.value)
  }
}

const loadTags = async (category) => {
  if (!category) {
    userTags.value = []
    return
  }
  
  try {
    const tagsData = await api.get(`/api/tags/category/${category}`)
    userTags.value = tagsData
  } catch (error) {
    console.error('Failed to load tags:', error)
    userTags.value = []
    // 标签加载失败不显示错误提示，避免干扰用户
  }
}

const handleCategoryChange = (category) => {
  loadTags(category)
  // 当切换分类时，清空标签，因为标签是分类特定的
  document.value.tags = ''
  // 不自动保存，让用户手动保存
}

const createTag = async () => {
  if (!newTag.value.tagName.trim()) {
    ElMessage.error('请输入标签名称')
    return
  }
  
  if (userTags.value.length >= 20) {
    ElMessage.error('每个分类最多只能创建20个标签')
    return
  }
  
  try {
    const response = await api.post('/api/tags', {
      category: document.value.category,
      tagName: newTag.value.tagName.trim()
    })
    
    ElMessage.success('标签创建成功')
    await loadTags(document.value.category)
    showTagDialog.value = false
    newTag.value.tagName = ''
  } catch (error) {
    console.error('Failed to create tag:', error)
    ElMessage.error(error.response?.data?.message || '标签创建失败')
  }
}

const saveDocument = async (showMessage = true, redirectToDetail = false) => {
  try {
    // 准备要发送的数据
    const documentData = { ...document.value }
    
    // 检查标签是否存在
    if (documentData.tags && documentData.tags.trim() !== '') {
      try {
        // 检查标签是否存在
        const existingTags = await api.get(`/api/tags/category/${documentData.category}`)
        const tagExists = existingTags.some(tag => tag.tagName === documentData.tags)
        
        if (!tagExists) {
          // 标签不存在，询问用户是否新建
          try {
            await ElMessageBox.confirm(
              `标签 "${documentData.tags}" 不存在，是否新建该标签？`,
              '标签不存在',
              {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }
            )
            
            // 用户点击了确定，创建新标签
            await api.post('/api/tags', {
              category: documentData.category,
              tagName: documentData.tags.trim()
            })
            
            ElMessage.success('标签创建成功')
          } catch (confirmError) {
            // 用户点击了取消，终止保存操作
            return Promise.reject(new Error('用户取消保存'))
          }
        }
      } catch (tagError) {
        console.error('标签检查失败:', tagError)
        ElMessage.error('标签检查失败，请重试')
        return Promise.reject(tagError)
      }
    }
    
    if (document.value.id) {
      // 更新已有文档
      await api.put(`/api/documents/${document.value.id}`, documentData)
    } else {
      // 保存新文档
      const response = await api.post('/api/documents', documentData)
      document.value.id = response.id
    }
    
    if (showMessage) {
      ElMessage.success('保存成功')
    }
    
    // 只有在明确要求跳转时才跳转到详情页面
    if (redirectToDetail) {
      router.push(`/document/${document.value.id}`)
    }
    return Promise.resolve()
  } catch (error) {
    console.error('保存文档失败:', error)
    if (showMessage && error.message !== '用户取消保存') {
      ElMessage.error('保存失败')
    }
    return Promise.reject(error)
  }
}

const autoSave = () => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
  autoSaveTimer.value = setTimeout(() => {
    if (document.value.id && (document.value.title || document.value.content)) {
      // 自动保存不显示消息，不跳转
      saveDocument(false, false).catch(error => {
        console.error('自动保存失败:', error)
        // 自动保存失败不显示错误提示，避免干扰用户
      })
    }
  }, 2000)
}

const saveToHistory = () => {
  // 移除当前索引之后的历史记录
  history.value = history.value.slice(0, historyIndex.value + 1)
  // 添加新的历史记录
  history.value.push(document.value.content)
  // 限制历史记录长度
  if (history.value.length > 50) {
    history.value.shift()
  } else {
    historyIndex.value++
  }
}

const handleInput = () => {
  // 保存到历史记录
  saveToHistory()
  autoSave()
}

const insertFormat = (prefix, suffix) => {
  const textarea = editorRef.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = document.value.content
  const selectedText = text.substring(start, end)
  
  document.value.content = text.substring(0, start) + prefix + selectedText + suffix + text.substring(end)
  
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, end + prefix.length)
  }, 0)
  
  autoSave()
}

const insertLine = (prefix) => {
  const textarea = editorRef.value
  const start = textarea.selectionStart
  const text = document.value.content
  
  const lineStart = text.lastIndexOf('\n', start - 1) + 1
  document.value.content = text.substring(0, lineStart) + prefix + text.substring(lineStart)
  
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(lineStart + prefix.length, lineStart + prefix.length)
  }, 0)
  
  autoSave()
}

const insertCodeBlock = () => {
  const code = '```\n你的代码\n```'
  insertAtCursor(code)
}

const insertTable = () => {
  // 重置表格配置为默认值
  tableConfig.value = {
    rows: 3,
    cols: 3,
    title: ''
  }
  // 显示表格配置对话框
  showTableDialog.value = true
}

const confirmInsertTable = () => {
  try {
    const { rows, cols } = tableConfig.value
    
    // 初始化表格数据
    const data = []
    
    // 添加表头
    const headerRow = []
    for (let j = 0; j < cols; j++) {
      headerRow.push(`列${j + 1}`)
    }
    data.push(headerRow)
    
    // 添加数据行
    for (let i = 0; i < rows; i++) {
      const row = []
      for (let j = 0; j < cols; j++) {
        row.push(`内容${i + 1}-${j + 1}`)
      }
      data.push(row)
    }
    
    tableData.value = data
    showTableDialog.value = false
    showTableEditorDialog.value = true
  } catch (error) {
    console.error('生成表格数据失败:', error)
    ElMessage.error('生成表格数据失败')
  }
}

const insertVisualTable = () => {
  try {
    const { title } = tableConfig.value
    
    // 生成 Markdown 表格
    let tableMarkdown = ''
    
    // 添加表格标题
    if (title) {
      tableMarkdown += `### ${title}\n\n`
    }
    
    if (tableData.value.length > 0) {
      // 生成表头行
      tableMarkdown += '|'
      for (const cell of tableData.value[0]) {
        tableMarkdown += ` ${cell} |`
      }
      tableMarkdown += '\n'
      
      // 生成分隔行
      tableMarkdown += '|'
      for (let j = 0; j < tableData.value[0].length; j++) {
        tableMarkdown += ' --- |'
      }
      tableMarkdown += '\n'
      
      // 生成数据行
      for (let i = 1; i < tableData.value.length; i++) {
        tableMarkdown += '|'
        for (const cell of tableData.value[i]) {
          tableMarkdown += ` ${cell} |`
        }
        tableMarkdown += '\n'
      }
    }
    
    insertAtCursor(tableMarkdown)
    showTableEditorDialog.value = false
    // 清空表格数据
    tableData.value = []
  } catch (error) {
    console.error('插入表格失败:', error)
    ElMessage.error('插入表格失败')
  }
}

const insertLink = () => {
  const link = '[链接文本](URL)'
  insertAtCursor(link)
}

const insertImage = () => {
  const image = '![图片描述](图片URL)'
  insertAtCursor(image)
}

const insertVideo = () => {
  const video = '<video src="视频URL" controls width="600"></video>'
  insertAtCursor(video)
}

const insertFormula = () => {
  const formula = '$$E=mc^2$$'
  insertAtCursor(formula)
}

const insertTOC = () => {
  const toc = '[TOC]'
  insertAtCursor(toc)
}

const undo = () => {
  if (historyIndex.value > 0) {
    historyIndex.value--
    document.value.content = history.value[historyIndex.value]
  }
}

const redo = () => {
  if (historyIndex.value < history.value.length - 1) {
    historyIndex.value++
    document.value.content = history.value[historyIndex.value]
  }
}

const showHistory = () => {
  ElMessage.info('历史记录功能开发中...')
}

const handleShowFormatMenu = () => {
  ElMessage.info('格式菜单功能开发中...')
}

const handleShowColorMenu = () => {
  ElMessage.info('颜色选择功能开发中...')
}

const handleShowBackgroundMenu = () => {
  ElMessage.info('背景色选择功能开发中...')
}

const handleShowAlignMenu = () => {
  ElMessage.info('对齐方式功能开发中...')
}

const handleShowTemplateMenu = () => {
  ElMessage.info('模板选择功能开发中...')
}

const toggleEditorMode = () => {
  editorMode.value = editorMode.value === 'edit' ? 'preview' : 'edit'
}

const insertAtCursor = (text) => {
  try {
    const textarea = editorRef.value
    if (!textarea) {
      console.error('编辑器引用未找到')
      ElMessage.error('编辑器初始化失败')
      return
    }
    
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    
    document.value.content = document.value.content.substring(0, start) + text + document.value.content.substring(end)
    
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + text.length, start + text.length)
    }, 0)
    
    autoSave()
  } catch (error) {
    console.error('插入文本失败:', error)
    ElMessage.error('插入内容失败')
  }
}

const handleExport = (command) => {
  switch (command) {
    case 'html':
      exportAsHtml()
      break
    case 'pdf':
      exportAsPdf()
      break
    case 'markdown':
      exportAsMarkdown()
      break
  }
}

const exportAsHtml = () => {
  const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>${document.value.title}</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/highlight.js@11.8.0/styles/github.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
    pre { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; }
    code { font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace; }
    table { border-collapse: collapse; width: 100%; }
    th, td { border: 1px solid #dfe2e5; padding: 8px 12px; }
    th { background: #f6f8fa; }
    img { max-width: 100%; }
  </style>
</head>
<body>
  <h1>${document.value.title}</h1>
  ${renderedContent.value}
</body>
</html>`
  
  downloadFile(html, `${document.value.title}.html`, 'text/html')
}

const exportAsPdf = () => {
  ElMessage.info('PDF导出功能需要后端支持，当前导出为HTML格式')
  exportAsHtml()
}

const exportAsMarkdown = () => {
  downloadFile(document.value.content, `${document.value.title}.md`, 'text/markdown')
}

const downloadFile = (content, filename, mimeType) => {
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

const goBack = () => {
  router.push('/documents')
}

onMounted(async () => {
  console.log('开始加载数据...')
  
  try {
    // 先加载分类列表
    console.log('1. 加载分类列表...')
    await loadCategories()
    console.log('分类列表加载完成:', categories.value)
    
    // 再加载文档
    console.log('2. 加载文档...')
    await loadDocument()
    console.log('文档加载完成:', document.value)
    
    // 如果文档有分类，加载对应的标签列表
    if (document.value.category) {
      console.log('3. 加载标签列表...')
      await loadTags(document.value.category)
      console.log('标签列表加载完成:', userTags.value)
    }
    
    // 检查分类和权限的值
    console.log('最终分类值:', document.value.category)
    console.log('最终权限值:', document.value.permission)
    console.log('分类选项:', categories.value.map(cat => cat.dictCode))
    console.log('权限选项:', ['public', 'private', 'secret'])
    
    console.log('数据加载完成!')
  } catch (error) {
    console.error('加载数据失败:', error)
  }
  
  window.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault()
      // Ctrl+S保存后跳转到详情页面
      saveDocument(true, true)
    }
  })
})

onBeforeUnmount(() => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
})
</script>

<style scoped lang="scss">
.editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.editor-header {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .title-input {
      width: 300px;
      
      :deep(.el-input__inner) {
        font-size: 18px;
        font-weight: 500;
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .tags-container {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .tag-limit {
    font-size: 12px;
    color: #999;
  }
}

.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 10px 20px;
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.editor-toolbar {
  background: #fafafa;
  border-bottom: 1px solid #e0e0e0;
  padding: 8px 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.editor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

.editor-pane, .preview-pane {
  flex: 1;
  overflow: auto;
  background: white;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.editor-pane {
  border-right: 1px solid #e0e0e0;
}

.edit-area {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

.markdown-editor {
  flex: 1;
  width: 100%;
  padding: 20px;
  border: none;
  outline: none;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  
  &:focus {
    background: #fafafa;
  }
}

.preview-pane {
  padding: 20px;
}

.preview-content {
  max-width: 800px;
  margin: 0 auto;
  
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

/* 可视化表格编辑器 */
.table-editor {
  margin: 20px 0;
  overflow-x: auto;
}

.visual-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #e8e8e8;
  
  th, td {
    border: 1px solid #e8e8e8;
    padding: 4px;
    min-width: 100px;
    height: 32px;
  }
  
  th {
    background-color: #fafafa;
  }
  
  .el-input {
    width: 100%;
    margin: 0;
    
    .el-input__inner {
      border: none;
      border-radius: 0;
      padding: 4px 8px;
      font-size: 14px;
      height: 24px;
      line-height: 24px;
      
      &:focus {
        border: none;
        box-shadow: none;
        background-color: #f0f9ff;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .visual-table {
    th, td {
      min-width: 80px;
    }
  }
}
</style>
