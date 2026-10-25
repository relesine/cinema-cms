<template>
  <div>
    <div class="page-head">
      <div>
        <h2>公告管理</h2>
        <p>发布后的公告会展示在用户购票端首页</p>
      </div>
      <el-button type="primary" @click="openAdd">发布公告</el-button>
    </div>

    <div class="panel">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="标题" prop="title" width="240" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.noticeType === 1 ? 'info' : 'warning'"
              disable-transitions
            >
              {{ row.noticeType === 1 ? '系统通知' : '活动优惠' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'" disable-transitions>
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" prop="publishTime" width="170" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑公告' : '发布公告'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.noticeType">
            <el-radio :value="1">系统通知</el-radio><el-radio :value="2">活动优惠</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="发布"
            inactive-text="草稿"
          />
        </el-form-item>
        <el-form-item label="内容"
          ><el-input v-model="form.content" type="textarea" :rows="4"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticePage, noticeSave, noticeDelete } from '@/api/admin'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const dialog = ref(false)
const form = reactive({})
async function load() {
  loading.value = true
  try {
    const r = await noticePage({ current: current.value, size: size.value })
    list.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}
function onPage(p) {
  current.value = p
  load()
}
function openAdd() {
  Object.assign(form, { id: null, title: '', content: '', noticeType: 1, status: 1 })
  dialog.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.value = true
}
async function save() {
  if (!form.title) return ElMessage.warning('请填写标题')
  await noticeSave({ ...form })
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
  await noticeDelete(row.id)
  ElMessage.success('已删除')
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
