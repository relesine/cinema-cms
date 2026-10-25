<template>
  <div>
    <div class="list-head">
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab"
          :class="{ active: status === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="search">
        <el-input
          v-model="keyword"
          placeholder="搜索影片名称"
          clearable
          @keyup.enter="reload"
          @clear="reload"
        />
        <el-button type="primary" @click="reload">搜索</el-button>
      </div>
    </div>

    <div v-if="!loading && !list.length" class="empty">
      <p>没有找到符合条件的影片</p>
      <el-button size="small" @click="reset">重置筛选条件</el-button>
    </div>

    <div v-else class="movie-grid" v-loading="loading">
      <article v-for="m in list" :key="m.id" class="movie-card" @click="goDetail(m.id)">
        <MoviePoster :movie="m" />
        <h4 class="movie-name">{{ m.name }}</h4>
        <div class="movie-sub">{{ m.genre }} · {{ m.duration }} 分钟</div>
        <div class="movie-foot">
          <template v-if="m.movieStatus === 1">
            <span class="rate">{{ Number(m.score).toFixed(1) }}</span>
            <span class="buy">购票</span>
          </template>
          <template v-else>
            <span class="date">{{ m.releaseDate }} 上映</span>
            <span class="buy muted">详情</span>
          </template>
        </div>
      </article>
    </div>

    <div class="pager">
      <span class="pager-total">共 {{ total }} 部影片</span>
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="current"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMovieList } from '@/api/movie'
import MoviePoster from '@/components/MoviePoster.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(12)
const status = ref(1)
const keyword = ref('')
const loading = ref(false)

const tabs = [
  { label: '正在热映', value: 1 },
  { label: '即将上映', value: 0 }
]

async function load() {
  loading.value = true
  try {
    const res = await getMovieList({
      current: current.value,
      size: size.value,
      movieStatus: status.value,
      keyword: keyword.value
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
function reload() {
  current.value = 1
  load()
}
function switchTab(v) {
  status.value = v
  reload()
}
function reset() {
  keyword.value = ''
  current.value = 1
  load()
}
function onPage(p) {
  current.value = p
  load()
}
const goDetail = (id) => router.push('/app/movie/' + id)
onMounted(load)
</script>

<style scoped>
.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 12px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--cms-line);
}
.tabs {
  display: flex;
  gap: 24px;
}
.tab {
  position: relative;
  padding: 4px 0;
  border: 0;
  background: none;
  font-size: 15px;
  color: var(--cms-sub);
  cursor: pointer;
  font-family: inherit;
}
.tab:hover {
  color: var(--cms-ink);
}
.tab.active {
  color: var(--cms-ink);
  font-weight: 600;
}
.tab.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -13px;
  height: 2px;
  background: var(--cms-brand);
}
.search {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 300px;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px 18px;
  min-height: 120px;
}
.movie-card {
  cursor: pointer;
}
.movie-card :deep(.poster) {
  transition: filter 0.2s;
}
.movie-card:hover :deep(.poster) {
  filter: brightness(1.06);
}
.movie-name {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.movie-sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--cms-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.movie-foot {
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rate {
  font-size: 12px;
  color: #c07c1c;
  font-variant-numeric: tabular-nums;
}
.rate::before {
  content: '评分 ';
  color: var(--cms-muted);
}
.date {
  font-size: 12px;
  color: var(--cms-brand);
}
.buy {
  font-size: 12px;
  color: var(--cms-brand);
}
.buy.muted {
  color: var(--cms-sub);
}
.movie-card:hover .buy {
  text-decoration: underline;
}

.empty {
  padding: 80px 0;
  text-align: center;
}
.empty p {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--cms-muted);
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px solid var(--cms-line);
}
.pager-total {
  font-size: 12px;
  color: var(--cms-muted);
}

@media (max-width: 1100px) {
  .movie-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 760px) {
  .list-head {
    flex-direction: column;
    align-items: stretch;
  }
  .search {
    width: 100%;
  }
  .movie-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 14px 12px;
  }
  .pager {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
