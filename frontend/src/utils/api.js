import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true,
  paramsSerializer: function(params) {
    return Object.keys(params).map(key => {
      if (Array.isArray(params[key])) {
        return params[key].map(value => `${key}=${encodeURIComponent(value)}`).join('&')
      } else {
        return `${key}=${encodeURIComponent(params[key])}`
      }
    }).join('&')
  }
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('当前 token:', token ? '存在' : '不存在')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('添加 Authorization header:', `Bearer ${token.substring(0, 20)}...`)
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    console.log('收到响应:', response)
    // 检查响应是否是一个对象，并且包含 success 字段
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      if (response.data.success) {
        // 如果 success 是 true，返回 data 字段
        console.log('响应成功，返回 response.data.data:', response.data.data)
        return response.data.data
      } else {
        // 如果 success 是 false，抛出异常
        console.log('响应失败，message:', response.data.message)
        const error = new Error(response.data.message)
        error.response = response
        throw error
      }
    }
    // 如果响应不包含 success 字段，直接返回 response.data
    console.log('响应不包含 success 字段，返回 response.data:', response.data)
    return response.data
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
        case 403:
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
          break
        case 404:
          // ElMessage.error('请求的资源不存在')
          break
        case 500:
          // ElMessage.error('服务器错误')
          break
        default:
          // 不自动显示错误信息，让前端代码自己处理
          break
      }
    } else {
      // ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// 头像相关API
export const avatarApi = {
  // 更新头像
  updateAvatar: (avatar) => api.post('/api/users/avatar', { avatar }),
  // 获取头像
  getAvatar: (userId) => api.get(`/api/users/${userId}/avatar`)
}

export default api
