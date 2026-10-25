import request from '@/utils/request'

// ===== 管理后台接口 =====
// 票房大盘
export const statOverview = () => request.get('/admin/stat/overview')
export const statMovieRank = () => request.get('/admin/stat/movie-rank')
export const statTrend = (days) => request.get('/admin/stat/trend', { params: { days } })

// 电影
export const moviePage = (params) => request.get('/admin/movie/page', { params })
export const movieAll = () => request.get('/admin/movie/all')
export const movieSave = (data) => request.post('/admin/movie/save', data)
export const movieDelete = (id) => request.delete(`/admin/movie/${id}`)

// 影厅 / 座位
export const hallList = () => request.get('/admin/hall/list')
export const hallSave = (data) => request.post('/admin/hall/save', data)
export const hallDelete = (id) => request.delete(`/admin/hall/${id}`)
export const hallSeats = (hallId) => request.get(`/admin/hall/seats/${hallId}`)
export const seatUpdate = (data) => request.post('/admin/hall/seat/update', data)

// 排片
export const schedulePage = (params) => request.get('/admin/schedule/page', { params })
export const scheduleSave = (data) => request.post('/admin/schedule/save', data)
export const scheduleCancel = (id) => request.post(`/admin/schedule/cancel/${id}`)
export const scheduleDelete = (id) => request.delete(`/admin/schedule/${id}`)

// 订单
export const adminOrderPage = (params) => request.get('/admin/order/page', { params })
export const adminOrderDetail = (id) => request.get(`/admin/order/${id}`)

// 用户 / 角色
export const userPage = (params) => request.get('/admin/user/page', { params })
export const roleList = () => request.get('/admin/user/roles')
export const userSave = (data) => request.post('/admin/user/save', data)
export const userStatus = (id, status) =>
  request.post('/admin/user/status', null, { params: { id, status } })
export const userResetPwd = (id, password) =>
  request.post('/admin/user/reset-pwd', null, { params: { id, password } })
export const userDelete = (id) => request.delete(`/admin/user/${id}`)

// 会员
export const adminMemberPage = (params) => request.get('/admin/member/page', { params })

// 公告
export const noticePage = (params) => request.get('/admin/notice/page', { params })
export const noticeSave = (data) => request.post('/admin/notice/save', data)
export const noticeDelete = (id) => request.delete(`/admin/notice/${id}`)
