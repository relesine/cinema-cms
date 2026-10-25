<template>
  <div>
    <div class="page-head">
      <div>
        <h2>用户与员工</h2>
        <p>账号启停、密码重置与角色分配</p>
      </div>
      <el-button type="primary" @click="openAdd">新增用户 / 员工</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="账号 / 昵称 / 手机号"
          clearable
          style="width: 240px"
          @keyup.enter="reload"
        />
        <el-button @click="reload">查询</el-button>
        <span class="text-muted">共 {{ total }} 个账号</span>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="账号" prop="username" width="130" />
        <el-table-column label="昵称" prop="nickname" width="130" />
        <el-table-column label="姓名" prop="realName" width="110" />
        <el-table-column label="手机号" prop="phone" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :active-value="true"
              :inactive-value="false"
              @change="(v) => toggleStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
            <el-button
              size="small"
              type="danger"
              plain
              :disabled="row.id === 1"
              @click="remove(row)"
            >
              删除
            </el-button>
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

    <el-dialog v-model="dialog" title="新增用户 / 员工" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.password" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="分配角色">
          <el-select v-model="form.roleId" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
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
import { userPage, roleList, userSave, userStatus, userResetPwd, userDelete } from '@/api/admin'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const keyword = ref('')
const roles = ref([])
const dialog = ref(false)
const form = reactive({})
async function load() {
  loading.value = true
  try {
    const r = await userPage({ current: current.value, size: size.value, keyword: keyword.value })
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
  Object.assign(form, { username: '', password: '123456', nickname: '', phone: '', roleId: 1 })
  dialog.value = true
}
async function save() {
  if (!form.username || !form.password) return ElMessage.warning('账号密码必填')
  await userSave({ ...form })
  ElMessage.success('新增成功')
  dialog.value = false
  load()
}
async function toggleStatus(row, v) {
  await userStatus(row.id, v ? 1 : 0)
  ElMessage.success(v ? '已启用' : '已禁用')
  load()
}
async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt('输入新密码', '重置密码', { inputValue: '123456' })
  await userResetPwd(row.id, value)
  ElMessage.success('密码已重置')
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await userDelete(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(async () => {
  load()
  roles.value = (await roleList()).data
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
</style>
