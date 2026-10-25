import request from '@/utils/request'

// 认证相关接口
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getInfo = () => request.get('/auth/info')
export const logout = () => request.post('/auth/logout')
