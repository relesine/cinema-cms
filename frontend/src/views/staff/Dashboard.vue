<template>
  <div v-loading="loading">
    <div class="page-head">
      <div>
        <h2>今日工作台</h2>
        <p>影院日常运营数据与常用操作入口</p>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card" v-for="c in cards" :key="c.label">
        <div class="label">{{ c.label }}</div>
        <div class="value">{{ c.value }}</div>
      </div>
    </div>

    <div class="work-row">
      <section class="panel">
        <div class="panel-head"><h3>快捷操作</h3></div>
        <div class="quick-list">
          <button class="quick" @click="$router.push('/staff/check')">
            <span class="q-title">检票核销</span>
            <span class="q-desc">扫描或输入订单号，核销入场</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
          <button class="quick" @click="$router.push('/staff/schedule')">
            <span class="q-title">场次查询</span>
            <span class="q-desc">按日期查看当日排片</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
          <button class="quick" @click="$router.push('/staff/order')">
            <span class="q-title">订单查询</span>
            <span class="q-desc">按订单号查询购票信息</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
          <button class="quick" @click="$router.push('/staff/member')">
            <span class="q-title">会员办理</span>
            <span class="q-desc">开卡、充值、积分查询</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </section>

      <section class="panel duty">
        <div class="panel-head"><h3>当班须知</h3></div>
        <ol>
          <li>开场前 10 分钟开始检票，散场清场后引导观众离场。</li>
          <li>退票仅支持开场前 30 分钟，超时请引导至值班经理。</li>
          <li>会员开卡需核对手机号，充值金额当面确认。</li>
          <li>如遇座位重复售卖，先安排就近空位并登记补偿。</li>
        </ol>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { staffOverview } from '@/api/staff'

const loading = ref(false)
const data = ref({})
const fmt = (n) => Number(n || 0).toLocaleString('zh-CN')

const cards = computed(() => [
  { label: '累计票房（元）', value: fmt(data.value.totalAmount) },
  { label: '累计订单', value: fmt(data.value.orderCount) },
  { label: '累计出票', value: fmt(data.value.ticketCount) },
  { label: '今日场次', value: fmt(data.value.todaySchedules) }
])

onMounted(async () => {
  loading.value = true
  try {
    data.value = (await staffOverview()).data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.work-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 12px;
  margin-top: 12px;
}
.panel {
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
  padding: 16px 18px;
}
.panel-head h3 {
  font-size: 14px;
  font-weight: 600;
}
.quick-list {
  margin-top: 10px;
}
.quick {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 11px 0;
  border: 0;
  border-bottom: 1px solid #f2f3f5;
  background: none;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
}
.quick:last-child {
  border-bottom: none;
}
.q-title {
  width: 84px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--cms-text);
}
.q-desc {
  flex: 1;
  font-size: 12px;
  color: var(--cms-muted);
}
.quick .el-icon {
  font-size: 12px;
  color: var(--cms-muted);
}
.quick:hover .q-title {
  color: var(--cms-brand);
}
.quick:hover .el-icon {
  color: var(--cms-brand);
}

.duty ol {
  margin-top: 10px;
  padding-left: 18px;
}
.duty li {
  padding: 5px 0;
  font-size: 12px;
  line-height: 1.7;
  color: var(--cms-sub);
}

@media (max-width: 1000px) {
  .work-row {
    grid-template-columns: 1fr;
  }
}
</style>
