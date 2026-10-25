<template>
  <div>
    <div class="page-head">
      <div>
        <h2>场次查询</h2>
        <p>按影片与日期检索当日排片</p>
      </div>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="电影名关键字"
          clearable
          style="width: 200px"
          @keyup.enter="reload"
        />
        <el-date-picker
          v-model="date"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          clearable
          style="width: 150px"
          @change="reload"
        />
        <el-button type="primary" @click="reload">查询</el-button>
        <span class="text-muted">共 {{ total }} 个场次</span>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="电影" prop="movieName" min-width="180" />
        <el-table-column label="影厅" prop="hallName" width="140" />
        <el-table-column label="开场" width="150">
          <template #default="{ row }">{{ fmtT(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="散场" width="100">
          <template #default="{ row }">{{ fmtT(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="版本" prop="versionLang" width="90" />
        <el-table-column label="票价" width="90">
          <template #default="{ row }"
            ><span class="money">￥{{ row.price }}</span></template
          >
        </el-table-column>
        <el-table-column label="余票 / 总座" width="110">
          <template #default="{ row }">{{ row.remainSeats }} / {{ row.totalSeats }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" disable-transitions>
              {{ row.status === 1 ? '正常' : '取消' }}
            </el-tag>
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
import { staffSchedulePage } from '@/api/staff'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const keyword = ref('')
const date = ref('')
const loading = ref(false)
async function load() {
  loading.value = true
  try {
    const res = await staffSchedulePage({
      current: current.value,
      size: size.value,
      keyword: keyword.value,
      date: date.value
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
