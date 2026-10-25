<template>
  <div>
    <div class="page-head">
      <div>
        <h2>电影管理</h2>
        <p>维护影片资料、海报与上映状态</p>
      </div>
      <el-button type="primary" @click="openAdd">新增电影</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="电影名"
          clearable
          style="width: 180px"
          @keyup.enter="reload"
        />
        <el-select
          v-model="movieStatus"
          placeholder="全部状态"
          clearable
          style="width: 130px"
          @change="reload"
        >
          <el-option label="热映" :value="1" />
          <el-option label="待映" :value="0" />
          <el-option label="下架" :value="2" />
        </el-select>
        <el-button @click="reload">查询</el-button>
        <span class="text-muted">共 {{ total }} 部影片</span>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column label="电影名" prop="name" min-width="160" />
        <el-table-column label="导演" prop="director" width="120" />
        <el-table-column label="类型" prop="genre" width="120" />
        <el-table-column label="片长" width="90">
          <template #default="{ row }">{{ row.duration }} 分</template>
        </el-table-column>
        <el-table-column label="评分" width="70">
          <template #default="{ row }">{{ Number(row.score || 0).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag
              size="small"
              effect="plain"
              :type="row.movieStatus === 1 ? 'danger' : row.movieStatus === 0 ? 'warning' : 'info'"
              disable-transitions
            >
              {{ { 1: '热映', 0: '待映', 2: '下架' }[row.movieStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑电影' : '新增电影'" width="660px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="中文名"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文名"><el-input v-model="form.nameEn" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="导演"><el-input v-model="form.director" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主演"><el-input v-model="form.actors" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型">
              <el-input v-model="form.genre" placeholder="如 科幻,冒险" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="片长(分)">
              <el-input-number v-model="form.duration" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上映日期">
              <el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地区"><el-input v-model="form.region" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="语言"><el-input v-model="form.language" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评分">
              <el-input-number v-model="form.score" :min="0" :max="10" :step="0.1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.movieStatus">
                <el-option label="热映" :value="1" />
                <el-option label="待映" :value="0" />
                <el-option label="下架" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="海报">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                name="file"
                :show-file-list="false"
                :on-success="onUploaded"
                accept="image/*"
              >
                <el-button>上传海报</el-button>
                <el-image
                  v-if="form.cover"
                  :src="fullImg(form.cover)"
                  class="cover-preview"
                  fit="cover"
                />
                <span class="upload-hint">不上传时使用系统排版海报</span>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介">
              <el-input v-model="form.synopsis" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { moviePage, movieSave, movieDelete } from '@/api/admin'
import { UPLOAD_URL, fullImg } from '@/api/upload'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const keyword = ref('')
const movieStatus = ref(null)
const loading = ref(false)
const dialog = ref(false)
const form = reactive({})
const uploadUrl = UPLOAD_URL
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('cms_token') }
function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    nameEn: '',
    director: '',
    actors: '',
    genre: '',
    duration: 120,
    releaseDate: '',
    region: '中国大陆',
    language: '汉语普通话',
    synopsis: '',
    score: 0,
    movieStatus: 1,
    cover: ''
  })
}
async function load() {
  loading.value = true
  try {
    const r = await moviePage({
      current: current.value,
      size: size.value,
      keyword: keyword.value,
      movieStatus: movieStatus.value
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
  resetForm()
  dialog.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialog.value = true
}
function onUploaded(res) {
  if (res.code === 200) {
    form.cover = res.data.url
    ElMessage.success('海报上传成功')
  } else ElMessage.error(res.message)
}
async function save() {
  if (!form.name) return ElMessage.warning('请填写电影名')
  await movieSave({ ...form })
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除电影《${row.name}》？`, '提示', { type: 'warning' })
  await movieDelete(row.id)
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
.cover-preview {
  width: 48px;
  height: 68px;
  margin-left: 12px;
  border-radius: 2px;
}
.upload-hint {
  margin-left: 10px;
  font-size: 11px;
  color: var(--cms-muted);
}
</style>
