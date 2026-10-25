<template>
  <div>
    <div class="page-head">
      <div>
        <h2>订单查询</h2>
        <p>按订单号或状态检索，用于前台协助处理</p>
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
        <el-table-column label="电影" prop="movieName" min-width="160" />
        <el-table-column label="场次" width="150">
          <template #default="{ row }">{{ fmtT(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="座位" min-width="150">
          <template #default="{ row }">{{ (row.seatNames || []).join('、') }}</template>
        </el-table-column>
        <el-table-column label="实付" width="100">
          <template #default="{ row }"
            ><span class="money">￥{{ row.payAmount }}</span></template
          >
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" disable-transitions>{{ row.orderStatusText }}</el-tag>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { staffOrderPage } from '@/api/staff'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const orderNo = ref('')
const status = ref(null)
const loading = ref(false)
const statusMap = { 0: '待支付', 1: '已支付', 2: '已退票', 3: '已取消', 4: '已完成' }
async function load() {
  loading.value = true
  try {
    const res = await staffOrderPage({
      current: current.value,
      size: size.value,
      orderNo: orderNo.value,
      status: status.value
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
function fmtT(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}
function reload() {
  current.value = 1
  load()
}
function onPage(p) {
  current.value = p
  load()
}
onMounted(load)
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
