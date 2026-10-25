import request from '@/utils/request'

// ===== 员工端接口 =====
export const staffScan = (orderNo) => request.post(`/staff/check/scan/${orderNo}`)
export const checkRecords = () => request.get('/staff/check/records')
export const staffSchedulePage = (params) => request.get('/staff/schedule/page', { params })
export const staffOrderPage = (params) => request.get('/staff/order/page', { params })
export const staffOrderDetail = (id) => request.get(`/staff/order/${id}`)
export const searchUser = (keyword) =>
  request.get('/staff/member/search-user', { params: { keyword } })
export const openMemberFor = (userId) => request.post(`/staff/member/open/${userId}`)
export const rechargeFor = (userId, amount) =>
  request.post('/staff/member/recharge', null, { params: { userId, amount } })
export const staffMemberPage = (params) => request.get('/staff/member/page', { params })
export const staffOverview = () => request.get('/staff/dashboard/overview')
