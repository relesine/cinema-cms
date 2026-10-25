<template>
  <div>
    <div class="list-head">
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="String(t.value)"
          class="tab"
          :class="{ active: status === t.value }"
          @click="switchTab(t.value)"
        >
          {{ t.label }}
        </button>
      </div>
      <span class="count">共 {{ total }} 笔订单</span>
    </div>

    <div v-if="list.length" class="order-list">
      <article v-for="o in list" :key="o.id" class="order-item">
        <div class="o-head">
          <span class="o-no">订单号 {{ o.orderNo }}</span>
          <span class="o-time">{{ fmtTime(o.createTime) }}</span>
          <el-tag :type="tagType(o.orderStatus)" size="small" class="o-status">
            {{ o.orderStatusText }}
          </el-tag>
        </div>
        <div class="o-body">
          <div class="o-main">
            <h3>{{ o.movieName }}</h3>
            <div class="o-meta">
              {{ fmtTime(o.startTime) }} · {{ o.hallName }} · {{ o.versionLang }}
            </div>
            <div class="o-seats">座位：{{ (o.seatNames || []).join('、') }}</div>
          </div>
          <div class="o-side">
            <div class="amount"><span>￥</span>{{ o.payAmount }}</div>
            <div class="o-actions">
              <template v-if="o.orderStatus === 0">
                <el-button type="danger" size="small" @click="toPay(o)">去支付</el-button>
                <el-button size="small" @click="cancel(o)">取消订单</el-button>
              </template>
              <template v-else-if="o.orderStatus === 1">
                <el-button size="small" @click="refund(o)">申请退票</el-button>
              </template>
              <el-button link size="small" @click="$router.push('/app/movie/' + o.movieId)">
                再买一张
              </el-button>
            </div>
          </div>
        </div>
      </article>
    </div>

    <div v-else class="empty">
      <p>当前分类下没有订单</p>
      <el-button size="small" type="danger" plain @click="$router.push('/app/movies')">
        去选片
      </el-button>
    </div>

    <div class="pager" v-if="total > size">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="current"
        @current-change="onPage"
      />
    </div>

    <el-dialog v-model="payVisible" title="确认支付" width="400px">
      <div class="pay-summary">
        <div class="pay-row">
          <span>订单号</span><b>{{ currentOrder.orderNo }}</b>
        </div>
        <div class="pay-row">
          <span>影片</span><b>{{ currentOrder.movieName }}</b>
        </div>
        <div class="pay-row">
          <span>优惠</span><b>-￥{{ currentOrder.discountAmount }}</b>
        </div>
        <div class="pay-row highlight">
          <span>实付</span><b>￥{{ currentOrder.payAmount }}</b>
        </div>
      </div>
      <div class="pay-type-label">支付方式（模拟）</div>
      <el-radio-group v-model="payType" class="pay-types">
        <el-radio :value="1">微信支付</el-radio>
        <el-radio :value="2">支付宝</el-radio>
        <el-radio :value="3">会员余额</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="danger" @click="doPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myOrders, payOrder, cancelOrder, refundOrder } from '@/api/order'

const route = useRoute()
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(8)
const status = ref(route.query.status !== undefined ? Number(route.query.status) : null)
const payVisible = ref(false)
const currentOrder = ref({})
const payType = ref(1)

const tabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已完成', value: 4 },
  { label: '已退票', value: 2 },
  { label: '已取消', value: 3 }
]

async function load() {
  const res = await myOrders({ current: current.value, size: size.value, status: status.value })
  list.value = res.data.records
  total.value = res.data.total
}
function switchTab(v) {
  status.value = v
  current.value = 1
  load()
}
function onPage(p) {
  current.value = p
  load()
}
function tagType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'info', 4: 'success' }[s] || ''
}
function fmtTime(t) {
  if (!t) return ''
  const s = String(t).replace(/[TZ]/g, ' ')
  const tail = s.slice(5, 16)
  return tail.endsWith(' ') ? s.slice(5, 10) + s.slice(10, 16) : tail.trim()
}
function toPay(o) {
  currentOrder.value = o
  payVisible.value = true
}
async function doPay() {
  await payOrder({ orderNo: currentOrder.value.orderNo, payType: payType.value })
  ElMessage.success('支付成功')
  payVisible.value = false
  load()
}
async function cancel(o) {
  await ElMessageBox.confirm('确定取消该订单并释放座位？', '取消订单', {
    confirmButtonText: '取消订单',
    cancelButtonText: '返回',
    type: 'warning'
  })
  await cancelOrder(o.orderNo)
  ElMessage.success('订单已取消')
  load()
}
async function refund(o) {
  await ElMessageBox.confirm('开场前 30 分钟以上可退票，确定申请退票？', '退票确认', {
    confirmButtonText: '确认退票',
    cancelButtonText: '返回',
    type: 'warning'
  })
  await refundOrder(o.orderNo)
  ElMessage.success('退票成功')
  load()
}
onMounted(load)
</script>

<style scoped>
.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--cms-line);
}
.tabs {
  display: flex;
  gap: 22px;
}
.tab {
  position: relative;
  padding: 3px 0;
  border: 0;
  background: none;
  font-family: inherit;
  font-size: 14px;
  color: var(--cms-sub);
  cursor: pointer;
}
.tab:hover {
  color: var(--cms-ink);
}
.tab.active {
  color: var(--cms-ink);
  font-weight: 600;
}
.tab.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -13px;
  height: 2px;
  background: var(--cms-brand);
}
.count {
  font-size: 12px;
  color: var(--cms-muted);
}

.order-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-item {
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
  background: #fff;
}
.order-item:hover {
  border-color: var(--cms-line-strong);
}
.o-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 9px 16px;
  border-bottom: 1px solid #f1f2f4;
  font-size: 12px;
  color: var(--cms-muted);
}
.o-no {
  color: var(--cms-sub);
}
.o-time {
  margin-left: auto;
}
.o-status {
  flex-shrink: 0;
}
.o-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px;
}
.o-main h3 {
  font-size: 16px;
  font-weight: 600;
}
.o-meta {
  margin-top: 4px;
  font-size: 13px;
  color: var(--cms-text);
}
.o-seats {
  margin-top: 3px;
  font-size: 12px;
  color: var(--cms-muted);
}
.o-side {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-shrink: 0;
}
.amount {
  font-size: 20px;
  font-weight: 600;
  color: var(--cms-ink);
  font-variant-numeric: tabular-nums;
}
.amount span {
  font-size: 12px;
  margin-right: 1px;
}
.o-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty {
  padding: 90px 0;
  text-align: center;
}
.empty p {
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--cms-muted);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

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

@media (max-width: 720px) {
  .list-head {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
  .tabs {
    flex-wrap: wrap;
    gap: 12px 18px;
  }
  .tab.active::after {
    bottom: -6px;
  }
  .o-body {
    flex-direction: column;
    align-items: flex-start;
  }
  .o-side {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
