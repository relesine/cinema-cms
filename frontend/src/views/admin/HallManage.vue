<template>
  <div>
    <div class="page-head">
      <div>
        <h2>影厅管理</h2>
        <p>共 {{ list.length }} 个影厅，保存后按行 × 列自动生成座位</p>
      </div>
      <el-button type="primary" @click="openAdd">新增影厅</el-button>
    </div>

    <div class="panel">
      <el-table :data="list">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="影厅名" prop="name" min-width="160" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ typeText[row.hallType] }}</template>
        </el-table-column>
        <el-table-column label="行数" prop="rowCount" width="80" />
        <el-table-column label="列数" prop="colCount" width="80" />
        <el-table-column label="座位数" prop="seatCount" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'" disable-transitions>
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-button size="small" @click="viewSeats(row)">座位图</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑影厅' : '新增影厅'" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="影厅名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.hallType" style="width: 100%">
            <el-option v-for="(t, v) in typeText" :key="v" :label="t" :value="Number(v)" />
          </el-select>
        </el-form-item>
        <el-form-item label="行数"
          ><el-input-number v-model="form.rowCount" :min="1" :max="20"
        /></el-form-item>
        <el-form-item label="列数"
          ><el-input-number v-model="form.colCount" :min="1" :max="20"
        /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存并生成座位</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="seatDialog" :title="`${currentHall.name} 座位矩阵`" width="720px">
      <div class="seat-matrix">
        <div class="matrix-row" v-for="r in seatRows" :key="r.rowNo">
          <span class="rl">{{ r.rowNo }} 排</span>
          <span
            class="mini-seat"
            v-for="s in r.seats"
            :key="s.id"
            :class="s.status === 1 ? 'ok' : 'bad'"
            >{{ s.colNo }}</span
          >
        </div>
      </div>
      <p class="matrix-note">浅灰为可用座位，深灰为维修/停用座位。</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hallList, hallSave, hallDelete, hallSeats } from '@/api/admin'
const list = ref([])
const dialog = ref(false)
const form = reactive({})
const seatDialog = ref(false)
const currentHall = ref({})
const seats = ref([])
const typeText = { 1: '普通厅', 2: 'IMAX巨幕厅', 3: 'VIP厅', 4: '儿童厅' }
const seatRows = computed(() => {
  const m = new Map()
  seats.value.forEach((s) => {
    if (!m.has(s.rowNo)) m.set(s.rowNo, [])
    m.get(s.rowNo).push(s)
  })
  return [...m.entries()].map(([rowNo, arr]) => ({ rowNo, seats: arr }))
})
async function load() {
  list.value = (await hallList()).data
}
function openAdd() {
  Object.assign(form, { id: null, name: '', hallType: 1, rowCount: 8, colCount: 10, status: 1 })
  dialog.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.value = true
}
async function save() {
  if (!form.name) return ElMessage.warning('请填写影厅名')
  await hallSave({ ...form })
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除影厅「${row.name}」？`, '提示', { type: 'warning' })
  await hallDelete(row.id)
  ElMessage.success('已删除')
  load()
}
async function viewSeats(row) {
  currentHall.value = row
  seats.value = (await hallSeats(row.id)).data
  seatDialog.value = true
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
.seat-matrix {
  max-height: 60vh;
  overflow: auto;
}
.matrix-row {
  display: flex;
  gap: 4px;
  margin-bottom: 4px;
  align-items: center;
}
.rl {
  width: 46px;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--cms-muted);
}
.mini-seat {
  width: 22px;
  height: 22px;
  line-height: 20px;
  text-align: center;
  font-size: 10px;
  border-radius: 2px;
}
.mini-seat.ok {
  background: #f2f3f5;
  border: 1px solid var(--cms-line-strong);
  color: var(--cms-sub);
}
.mini-seat.bad {
  background: #e4e6e9;
  border: 1px solid #d5d8dc;
  color: #a3a9b0;
}
.matrix-note {
  margin-top: 12px;
  font-size: 11px;
  color: var(--cms-muted);
}
</style>
