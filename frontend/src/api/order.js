import request from '@/utils/request'

// ===== 用户端：订单交易 =====
export const createOrder = (data) => request.post('/app/order/create', data)
export const payOrder = (data) => request.post('/app/order/pay', data)
export const cancelOrder = (orderNo) => request.post(`/app/order/cancel/${orderNo}`)
export const refundOrder = (orderNo) => request.post(`/app/order/refund/${orderNo}`)
export const myOrders = (params) => request.get('/app/order/mine', { params })
export const orderDetail = (id) => request.get(`/app/order/${id}`)

// ===== 用户端：会员 =====
export const memberCenter = () => request.get('/app/member/center')
export const openMember = () => request.post('/app/member/open')
export const rechargeMember = (amount) =>
  request.post('/app/member/recharge', null, { params: { amount } })
export const memberLevels = () => request.get('/app/member/levels')
