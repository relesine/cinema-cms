<template>
  <div>
    <div class="page-head">
      <div>
        <h2>订单管理</h2>
        <p>全量订单检索与详情核对</p>
      </div>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input
          v-model="orderNo"
          placeholder="订单号"
          clearable
          style="width: 240px"
          @keyup.enter="reload"
        />
        <el-select v-model="status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(t, v) in statusMap" :key="v" :label="t" :value="Number(v)" />
        </el-select>
        <el-button type="primary" @click="reload">查询</el-button>
        <span class="text-muted">共 {{ total }} 笔</span>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="订单号" prop="orderNo" width="230" />
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ row.nickname || row.username }}</template>
        </el-table-column>
        <el-table-column label="电影" prop="movieName" min-width="150" />
        <el-table-column label="场次" width="150">
          <template #default="{ row }">{{ fmtTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="座位" min-width="150">
          <template #default="{ row }">{{ (row.seatNames || []).join('、') }}</template>
        </el-table-column>
        <el-table-column label="原价" width="90">
          <template #default="{ row }"
            ><span class="money">￥{{ row.totalAmount }}</span></template
          >
        </el-table-column>
        <el-table-column label="实付" width="90">
          <template #default="{ row }"
            ><span class="money">￥{{ row.payAmount }}</span></template
          >
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" disable-transitions>{{ row.orderStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" @click="detail = row">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          layout="prev,pager,next,total"
          :total="total"
          :page-size="size"
          :current-page="current"
          @current-change="onPage"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <el-descriptions :column="1" border v-if="detail">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="影片 / 场次">
          {{ detail.movieName }} / {{ fmtTime(detail.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="影厅 / 版本">
          {{ detail.hallName }} / {{ detail.versionLang }}
        </el-descriptions-item>
        <el-descriptions-item label="座位">
          {{ (detail.seatNames || []).join('、') }}
        </el-descriptions-item>
        <el-descriptions-item label="金额">
          原价￥{{ detail.totalAmount }}，优惠￥{{ detail.discountAmount }}，实付￥{{
            detail.payAmount
          }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.orderStatusText }}</el-descriptions-item>
        <el-descriptions-item label="下单 / 支付">
          {{ fmtTime(detail.createTime) }} / {{ fmtTime(detail.payTime) || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { adminOrderPage } from '@/api/admin'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const orderNo = ref('')
const status = ref(null)
const detail = ref(null)
const detailVisible = ref(false)
watch(detail, (v) => {
  if (v) detailVisible.value = true
})
const statusMap = { 0: '待支付', 1: '已支付', 2: '已退票', 3: '已取消', 4: '已完成' }
async function load() {
  loading.value = true
  try {
    const r = await adminOrderPage({
      current: current.value,
      size: size.value,
      orderNo: orderNo.value,
      status: status.value
    })
    list.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}
function reload() {
  current.value = 1
  load()
}
function onPage(p) {
  current.value = p
  load()
}
function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}
load()
</script>

<style scoped>
.panel {
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
