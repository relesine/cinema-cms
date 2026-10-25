<template>
  <div v-loading="loading" class="seat-page">
    <!-- 场次信息 -->
    <div class="order-strip">
      <div class="back" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <div class="strip-main">
        <h1>{{ schedule.movieName }}</h1>
        <div class="strip-meta">
          <span>{{ fmtTime(schedule.startTime) }}</span>
          <i></i>
          <span>{{ schedule.hallName }}</span>
          <i></i>
          <span>{{ schedule.versionLang }}</span>
          <i></i>
          <span>余票 {{ schedule.remainSeats }} 张</span>
        </div>
      </div>
      <div class="strip-price">
        <span>单价</span>
        <b>￥{{ schedule.price }}</b>
      </div>
    </div>

    <!-- 座位图 -->
    <div class="seat-panel">
      <div class="screen-wrap">
        <div class="screen">SCREEN</div>
        <div class="screen-hint">银幕方向</div>
      </div>

      <div class="seat-area">
        <div class="seat-row" v-for="row in seatRows" :key="row.rowNo">
          <span class="row-label">{{ row.rowNo }}</span>
          <span
            v-for="seat in row.seats"
            :key="seat.seatId"
            class="seat"
            :class="seatClass(seat)"
            @click="toggleSeat(seat)"
          >
            {{ seat.colNo }}
          </span>
          <span class="row-label">{{ row.rowNo }}</span>
        </div>
      </div>

      <div class="legend">
        <span><i class="seat s-optional"></i>可选</span>
        <span><i class="seat s-vip"></i>VIP 座</span>
        <span><i class="seat s-kid"></i>儿童座</span>
        <span><i class="seat s-selected"></i>已选</span>
        <span><i class="seat s-lock"></i>临时锁定</span>
        <span><i class="seat s-sold"></i>已售</span>
        <span><i class="seat s-repair"></i>维修</span>
      </div>
    </div>

    <!-- 结算栏 -->
    <div class="settle-bar">
      <div class="picked">
        <span class="label">已选座位</span>
        <template v-if="selected.length">
          <el-tag
            v-for="s in selected"
            :key="s.seatId"
            closable
            size="small"
            class="pick-tag"
            @close="toggleSeat(s)"
          >
            {{ s.seatName }}
          </el-tag>
        </template>
        <span v-else class="placeholder">请在上方座位图选座，单次最多 6 张</span>
      </div>
      <div class="checkout">
        <div class="total">
          <span>合计</span>
          <b>￥{{ total }}</b>
          <em>会员折扣以支付为准</em>
        </div>
        <el-button type="danger" :disabled="!selected.length" @click="submitOrder">
          确认选座
        </el-button>
      </div>
    </div>

    <!-- 支付 -->
    <el-dialog v-model="payVisible" title="确认支付" width="420px">
      <div class="pay-summary">
        <div class="pay-row">
          <span>订单号</span><b>{{ current.orderNo }}</b>
        </div>
        <div class="pay-row">
          <span>座位</span><b>{{ (current.seatNames || []).join('、') }}</b>
        </div>
        <div class="pay-row">
          <span>原价</span><b>￥{{ current.totalAmount }}</b>
        </div>
        <div class="pay-row">
          <span>会员优惠</span><b>-￥{{ current.discountAmount }}</b>
        </div>
        <div class="pay-row highlight">
          <span>实付</span><b>￥{{ current.payAmount }}</b>
        </div>
      </div>
      <div class="pay-type-label">支付方式（模拟）</div>
      <el-radio-group v-model="payType" class="pay-types">
        <el-radio :value="1">微信支付</el-radio>
        <el-radio :value="2">支付宝</el-radio>
        <el-radio :value="3">会员余额</el-radio>
      </el-radio-group>
      <div class="pay-tip">座位将在 15 分钟内保留，超时订单自动取消</div>
      <template #footer>
        <el-button @click="goOrders">稍后支付</el-button>
        <el-button type="danger" :loading="paying" @click="doPay">立即支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScheduleDetail, getSeatMap } from '@/api/movie'
import { createOrder, payOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const schedule = ref({})
const seats = ref([])
const selected = ref([])
const payVisible = ref(false)
const current = ref({})
const payType = ref(1)
const paying = ref(false)
let timer = null

const seatRows = computed(() => {
  const map = new Map()
  seats.value.forEach((s) => {
    if (!map.has(s.rowNo)) map.set(s.rowNo, [])
    map.get(s.rowNo).push(s)
  })
  return [...map.entries()].map(([rowNo, list]) => ({ rowNo, seats: list }))
})
const total = computed(() => (schedule.value.price || 0) * selected.value.length)

// 后端时间形如 2026-09-07T10:00:00，展示为 09-07 10:00
function fmtTime(t) {
  if (!t) return ''
  const s = String(t).replace('T', ' ')
  return s.length >= 16 ? `${s.slice(5, 10)} ${s.slice(11, 16)}` : s
}

function seatClass(seat) {
  if (selected.value.find((s) => s.seatId === seat.seatId)) return 's-selected'
  if (seat.seatStatus === 0 || seat.sellStatus === 3) return 's-repair'
  switch (seat.sellStatus) {
    case 1:
      return 's-lock'
    case 2:
      return 's-sold'
    default:
      return seat.seatType === 2 ? 's-vip' : seat.seatType === 3 ? 's-kid' : 's-optional'
  }
}

function toggleSeat(seat) {
  if (
    seat.sellStatus === 1 ||
    seat.sellStatus === 2 ||
    seat.seatStatus === 0 ||
    seat.sellStatus === 3
  ) {
    if (seat.sellStatus === 2) ElMessage.warning('该座位已售出')
    else if (seat.sellStatus === 1) ElMessage.warning('该座位被临时锁定')
    else ElMessage.warning('该座位暂不可选')
    return
  }
  const idx = selected.value.findIndex((s) => s.seatId === seat.seatId)
  if (idx >= 0) selected.value.splice(idx, 1)
  else {
    if (selected.value.length >= 6) return ElMessage.warning('单次最多选6个座位')
    selected.value.push(seat)
  }
}

async function loadAll() {
  const id = route.params.scheduleId
  const [d, sm] = await Promise.all([getScheduleDetail(id), getSeatMap(id)])
  schedule.value = d.data
  seats.value = sm.data || []
  const okIds = seats.value
    .filter((s) => s.sellStatus === 0 && s.seatStatus === 1)
    .map((s) => s.seatId)
  selected.value = selected.value.filter((s) => okIds.includes(s.seatId))
}

async function submitOrder() {
  loading.value = true
  try {
    const res = await createOrder({
      scheduleId: Number(route.params.scheduleId),
      seatIds: selected.value.map((s) => s.seatId)
    })
    current.value = res.data
    payVisible.value = true
  } finally {
    loading.value = false
  }
}

async function doPay() {
  paying.value = true
  try {
    await payOrder({ orderNo: current.value.orderNo, payType: payType.value })
    ElMessage.success('支付成功')
    payVisible.value = false
    router.push('/app/orders?status=1')
  } finally {
    paying.value = false
  }
}
const goOrders = () => router.push('/app/orders?status=0')

onMounted(async () => {
  loading.value = true
  try {
    await loadAll()
  } finally {
    loading.value = false
  }
  timer = setInterval(loadAll, 15000)
})
onBeforeUnmount(() => timer && clearInterval(timer))
</script>

<style scoped>
.seat-page {
  padding-bottom: 12px;
}

/* 场次信息 */
.order-strip {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.back {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: var(--cms-sub);
  cursor: pointer;
  padding-right: 18px;
  border-right: 1px solid var(--cms-line);
}
.back:hover {
  color: var(--cms-brand);
}
.strip-main {
  flex: 1;
  min-width: 0;
}
.strip-main h1 {
  font-size: 18px;
  font-weight: 600;
}
.strip-meta {
  margin-top: 3px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--cms-muted);
}
.strip-meta i {
  width: 1px;
  height: 9px;
  background: var(--cms-line-strong);
}
.strip-price {
  text-align: right;
  flex-shrink: 0;
}
.strip-price span {
  display: block;
  font-size: 11px;
  color: var(--cms-muted);
}
.strip-price b {
  font-size: 20px;
  font-weight: 600;
  color: var(--cms-brand);
}

/* 座位面板 */
.seat-panel {
  margin-top: 16px;
  padding: 26px 20px 20px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.screen-wrap {
  text-align: center;
  margin-bottom: 22px;
}
.screen {
  width: 58%;
  height: 26px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  letter-spacing: 6px;
  color: #9a9fa6;
  background: #f4f5f6;
  border-bottom: 2px solid #dcdfe3;
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
}
.screen-hint {
  margin-top: 6px;
  font-size: 10px;
  letter-spacing: 2px;
  color: #b0b5bb;
}

.seat-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 820px;
  margin: 0 auto;
  max-height: 54vh;
  overflow: auto;
  padding: 4px 12px;
}
.seat-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}
.row-label {
  width: 28px;
  flex-shrink: 0;
  text-align: center;
  font-size: 11px;
  color: #b0b5bb;
  font-variant-numeric: tabular-nums;
}
.seat {
  width: 25px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  font-size: 10px;
  border-radius: 3px 3px 2px 2px;
  cursor: pointer;
  user-select: none;
  transition:
    background-color 0.15s,
    border-color 0.15s;
}
.s-optional {
  background: #f2f3f5;
  border: 1px solid #e1e4e8;
  color: #8b9198;
}
.s-optional:hover {
  border-color: var(--cms-brand);
  color: var(--cms-brand);
}
.s-vip {
  background: #fbf6ec;
  border: 1px solid #e8dbc2;
  color: #9a7b3f;
}
.s-kid {
  background: #f1f7f3;
  border: 1px solid #d6e6dc;
  color: #4d7a5f;
}
.s-selected {
  background: var(--cms-brand);
  border: 1px solid var(--cms-brand);
  color: #fff;
}
.s-lock {
  background: #eceef1;
  border: 1px solid #e3e6ea;
  color: #b6bbc1;
  cursor: not-allowed;
}
.s-sold {
  background: #d9dce0;
  border: 1px solid #d9dce0;
  color: #9aa0a6;
  cursor: not-allowed;
}
.s-repair {
  background: repeating-linear-gradient(45deg, #f5f6f7, #f5f6f7 3px, #e9ebee 3px, #e9ebee 6px);
  border: 1px solid #eceef1;
  color: #c2c6cb;
  cursor: not-allowed;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
  justify-content: center;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid #f1f2f4;
  font-size: 12px;
  color: var(--cms-sub);
}
.legend span {
  display: flex;
  align-items: center;
  gap: 5px;
}
.legend .seat {
  width: 14px;
  height: 14px;
  border-radius: 2px;
  cursor: default;
}

/* 结算栏 */
.settle-bar {
  position: sticky;
  bottom: 12px;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 20px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
  box-shadow: 0 -2px 10px rgba(15, 18, 25, 0.04);
}
.picked {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}
.picked .label {
  font-size: 13px;
  color: var(--cms-sub);
  margin-right: 2px;
}
.pick-tag {
  margin: 0;
}
.picked .placeholder {
  font-size: 12px;
  color: var(--cms-muted);
}
.checkout {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}
.total {
  text-align: right;
  line-height: 1.2;
}
.total span {
  font-size: 12px;
  color: var(--cms-sub);
}
.total b {
  margin-left: 6px;
  font-size: 22px;
  font-weight: 600;
  color: var(--cms-brand);
}
.total em {
  display: block;
  margin-top: 2px;
  font-style: normal;
  font-size: 11px;
  color: var(--cms-muted);
}

/* 支付弹窗 */
.pay-summary {
  border: 1px solid var(--cms-line);
  border-radius: 3px;
  padding: 4px 14px;
}
.pay-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 0;
  font-size: 13px;
  border-bottom: 1px solid #f1f2f4;
}
.pay-row:last-child {
  border-bottom: none;
}
.pay-row span {
  color: var(--cms-sub);
}
.pay-row b {
  font-weight: 500;
  color: var(--cms-text);
}
.pay-row.highlight b {
  font-size: 18px;
  font-weight: 600;
  color: var(--cms-brand);
}
.pay-type-label {
  margin: 16px 0 8px;
  font-size: 12px;
  color: var(--cms-sub);
}
.pay-types {
  display: flex;
  gap: 24px;
}
.pay-tip {
  margin-top: 14px;
  font-size: 11px;
  color: var(--cms-muted);
}
</style>
