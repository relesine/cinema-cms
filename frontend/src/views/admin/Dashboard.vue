<template>
  <div v-loading="loading">
    <div class="page-head">
      <div>
        <h2>票房大盘</h2>
        <p>统计口径：已支付与已完成订单，金额单位元</p>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card" v-for="c in cards" :key="c.label">
        <div class="label">{{ c.label }}</div>
        <div class="value">{{ c.value }}</div>
        <div class="foot" v-if="c.foot">{{ c.foot }}</div>
      </div>
    </div>

    <div class="chart-row">
      <section class="panel trend">
        <div class="panel-head">
          <h3>近 7 天票房趋势</h3>
          <span>单位：元</span>
        </div>
        <div ref="trendRef" class="chart"></div>
      </section>
      <section class="panel rank">
        <div class="panel-head">
          <h3>影片票房排行</h3>
          <span>累计</span>
        </div>
        <div ref="rankRef" class="chart"></div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { statOverview, statTrend, statMovieRank } from '@/api/admin'

const loading = ref(false)
const overview = ref({})
const trendRef = ref(null)
const rankRef = ref(null)
let trendChart, rankChart

const fmt = (n) => Number(n || 0).toLocaleString('zh-CN')

const cards = computed(() => [
  { label: '总票房（元）', value: fmt(overview.value.totalAmount) },
  {
    label: '总订单',
    value: fmt(overview.value.orderCount),
    foot: `今日 ${fmt(overview.value.todayAmount)} 元`
  },
  { label: '总出票', value: fmt(overview.value.ticketCount) },
  { label: '注册用户', value: fmt(overview.value.userCount) },
  { label: '会员数', value: fmt(overview.value.memberCount) },
  { label: '今日票房（元）', value: fmt(overview.value.todayAmount) }
])

const axisStyle = {
  axisLine: { lineStyle: { color: '#e6e8eb' } },
  axisTick: { show: false },
  axisLabel: { color: '#98a0a8', fontSize: 11 },
  splitLine: { lineStyle: { color: '#f1f2f4' } }
}

async function load() {
  loading.value = true
  try {
    const [o, t, r] = await Promise.all([statOverview(), statTrend(7), statMovieRank()])
    overview.value = o.data
    await nextTick()

    trendChart = echarts.init(trendRef.value)
    trendChart.setOption({
      grid: { left: 56, right: 20, top: 24, bottom: 28 },
      tooltip: { trigger: 'axis', borderWidth: 0, textStyle: { fontSize: 12 } },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: t.data.map((i) => i.date.slice(5)),
        ...axisStyle,
        splitLine: { show: false }
      },
      yAxis: { type: 'value', ...axisStyle },
      series: [
        {
          name: '票房',
          type: 'line',
          smooth: false,
          symbol: 'circle',
          symbolSize: 4,
          lineStyle: { color: '#a5121b', width: 1.5 },
          itemStyle: { color: '#a5121b' },
          areaStyle: { color: 'rgba(165,18,27,0.05)' },
          data: t.data.map((i) => i.amount)
        }
      ]
    })

    const rank = r.data.slice().reverse()
    rankChart = echarts.init(rankRef.value)
    rankChart.setOption({
      grid: { left: 92, right: 32, top: 18, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, borderWidth: 0 },
      xAxis: { type: 'value', ...axisStyle },
      yAxis: {
        type: 'category',
        data: rank.map((i) => i.name),
        ...axisStyle,
        splitLine: { show: false }
      },
      series: [
        {
          type: 'bar',
          barWidth: 10,
          itemStyle: { color: '#8b9198', borderRadius: [0, 2, 2, 0] },
          data: rank.map((i) => Number(i.amount || 0))
        }
      ]
    })

    window.addEventListener('resize', resize)
  } finally {
    loading.value = false
  }
}
function resize() {
  trendChart?.resize()
  rankChart?.resize()
}
onMounted(load)
</script>

<style scoped>
.chart-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 12px;
  margin-top: 12px;
}
.panel {
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
  padding: 14px 16px 8px;
}
.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.panel-head h3 {
  font-size: 14px;
  font-weight: 600;
}
.panel-head span {
  font-size: 11px;
  color: var(--cms-muted);
}
.chart {
  height: 300px;
}

@media (max-width: 1100px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
