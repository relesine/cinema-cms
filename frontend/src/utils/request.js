import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例：统一前缀 /api，由 Vite 代理转发到 8080
const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带登录令牌
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cms_token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务状态码
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流等非标准结构直接返回
    if (res instanceof Blob) return res
    if (res.code === 200) {
      return res
    }
    // 401 未登录/过期：清理并跳登录
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已过期，请重新登录')
      localStorage.clear()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    // 403 无权限、500 业务错误统一弹提示
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    ElMessage.error('网络异常：' + error.message)
    return Promise.reject(error)
  }
)

export default service
