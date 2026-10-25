<template>
  <div>
    <div class="page-head">
      <div>
        <h2>排片管理</h2>
        <p>按影厅与日期编排场次，散场时间由片长自动计算</p>
      </div>
      <el-button type="primary" @click="openAdd">新增排片</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="电影名"
          clearable
          style="width: 160px"
          @keyup.enter="reload"
        />
        <el-select
          v-model="hallId"
          placeholder="全部影厅"
          clearable
          style="width: 150px"
          @change="reload"
        >
          <el-option v-for="h in halls" :key="h.id" :label="h.name" :value="h.id" />
        </el-select>
        <el-date-picker
          v-model="date"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="日期"
          clearable
          style="width: 150px"
          @change="reload"
        />
        <el-button @click="reload">查询</el-button>
        <span class="text-muted">共 {{ total }} 场</span>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="电影" prop="movieName" min-width="170" />
        <el-table-column label="影厅" prop="hallName" width="130" />
        <el-table-column label="开场" width="150">
          <template #default="{ row }">{{ fmtT(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="散场" width="100">
          <template #default="{ row }">{{ fmtT(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="版本" prop="versionLang" width="90" />
        <el-table-column label="票价" width="85">
          <template #default="{ row }"
            ><span class="money">￥{{ row.price }}</span></template
          >
        </el-table-column>
        <el-table-column label="余票" width="70">
          <template #default="{ row }">{{ row.remainSeats }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'" disable-transitions>
              {{ row.status === 1 ? '正常' : '取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.status === 0" @click="cancel(row)">
              取消场次
            </el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialog" title="新增排片" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="电影">
          <el-select v-model="form.movieId" filterable placeholder="选择电影" style="width: 100%">
            <el-option v-for="m in movies" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="影厅">
          <el-select
            v-model="form.hallId"
            placeholder="选择影厅"
            style="width: 100%"
            @change="onHallChange"
          >
            <el-option v-for="h in halls" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开场时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开场时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="票价(元)">
          <el-input-number v-model="form.price" :min="1" :precision="2" />
        </el-form-item>
        <el-form-item label="版本">
          <el-select v-model="form.versionLang" style="width: 100%">
            <el-option label="国语2D" value="国语2D" />
            <el-option label="国语3D" value="国语3D" />
            <el-option label="IMAX3D" value="IMAX3D" />
            <el-option label="VIP厅" value="VIP厅" />
          </el-select>
        </el-form-item>
      </el-form>
      <p class="dialog-tip">散场时间由后端按片长自动计算；同影厅时间重叠会被拦截。</p>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存排片</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  schedulePage,
  scheduleSave,
  scheduleCancel,
  scheduleDelete,
  movieAll,
  hallList
} from '@/api/admin'
const list = ref([])

function fmtT(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const keyword = ref('')
const hallId = ref(null)
const date = ref('')
const movies = ref([])
const halls = ref([])
const dialog = ref(false)
const form = reactive({})
async function load() {
  loading.value = true
  try {
    const r = await schedulePage({
      current: current.value,
      size: size.value,
      keyword: keyword.value,
      hallId: hallId.value,
      date: date.value
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
function openAdd() {
  Object.assign(form, {
    movieId: null,
    hallId: null,
    startTime: '',
    price: 35,
    versionLang: '国语2D'
  })
  dialog.value = true
}
// 选影厅时按类型给默认票价
function onHallChange(id) {
  const h = halls.value.find((i) => i.id === id)
  if (h) form.price = { 1: 35, 2: 55, 3: 78, 4: 40 }[h.hallType] || 35
}
async function save() {
  if (!form.movieId || !form.hallId || !form.startTime)
    return ElMessage.warning('请完整选择电影、影厅、时间')
  await scheduleSave({ ...form })
  ElMessage.success('排片成功')
  dialog.value = false
  load()
}
async function cancel(row) {
  await ElMessageBox.confirm('确定取消该场次？', '提示', { type: 'warning' })
  await scheduleCancel(row.id)
  ElMessage.success('已取消')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该排片记录？', '提示', { type: 'warning' })
  await scheduleDelete(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(async () => {
  load()
  movies.value = (await movieAll()).data
  halls.value = (await hallList()).data
})
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
.dialog-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--cms-muted);
}
</style>
