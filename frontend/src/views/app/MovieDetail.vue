<template>
  <div v-loading="loading">
    <!-- 影片信息 -->
    <section class="film-head">
      <div class="film-poster">
        <MoviePoster :movie="movie" />
      </div>
      <div class="film-info">
        <div class="film-title">
          <h1>{{ movie.name }}</h1>
          <span v-if="movie.nameEn">{{ movie.nameEn }}</span>
        </div>
        <div class="film-meta">
          <span>{{ movie.genre }}</span>
          <i></i>
          <span>{{ movie.duration }} 分钟</span>
          <i></i>
          <span>{{ movie.releaseDate }} 上映</span>
          <i></i>
          <span>{{ movie.region }} / {{ movie.language }}</span>
        </div>

        <div class="film-score" v-if="movie.movieStatus === 1">
          <b>{{ Number(movie.score || 0).toFixed(1) }}</b>
          <span>观众评分</span>
        </div>
        <div class="film-score coming" v-else-if="movie.movieStatus === 0">
          <b>待映</b>
          <span>{{ movie.releaseDate }} 上映</span>
        </div>

        <dl class="credits">
          <div>
            <dt>导演</dt>
            <dd>{{ movie.director || '—' }}</dd>
          </div>
          <div>
            <dt>主演</dt>
            <dd>{{ movie.actors || '—' }}</dd>
          </div>
        </dl>

        <p class="synopsis">{{ movie.synopsis }}</p>
      </div>
    </section>

    <!-- 场次 -->
    <section v-if="movie.movieStatus === 1" class="schedule">
      <div class="schedule-head">
        <h2>选择场次</h2>
        <span class="hint">影厅座位有限，建议提前购票</span>
      </div>

      <div class="date-strip">
        <button
          v-for="d in dates"
          :key="d.value"
          class="date-chip"
          :class="{ active: d.value === date }"
          @click="selectDate(d.value)"
        >
          <span class="d-label">{{ d.label }}</span>
          <span class="d-date">{{ d.short }}</span>
        </button>
      </div>

      <div v-if="schedules.length" class="schedule-list">
        <div v-for="row in schedules" :key="row.id" class="schedule-row">
          <div class="s-time">
            <b>{{ row.startTime?.substring(11, 16) }}</b>
            <span>{{ row.versionLang }}</span>
          </div>
          <div class="s-hall">
            <span class="hall-name">{{ row.hallName }}</span>
            <span class="hall-sub"
              >{{ row.startTime?.substring(5, 10) }} · 散场约 {{ endTime(row) }}</span
            >
          </div>
          <div class="s-seat">
            <span :class="row.remainSeats > 0 ? 'enough' : 'none'">
              {{ row.remainSeats > 0 ? `余 ${row.remainSeats} 座` : '已满场' }}
            </span>
          </div>
          <div class="s-price">
            <span class="cur">￥</span><b>{{ row.price }}</b>
          </div>
          <div class="s-action">
            <el-button
              type="danger"
              size="small"
              :disabled="row.remainSeats <= 0"
              @click="selectSeat(row)"
            >
              选座购票
            </el-button>
          </div>
        </div>
      </div>
      <div v-else class="schedule-empty">
        <p>当日暂无场次</p>
        <span>可切换上方日期查看其他场次</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMovieDetail, getSchedulesByMovie } from '@/api/movie'
import MoviePoster from '@/components/MoviePoster.vue'

const route = useRoute()
const router = useRouter()
const movie = ref({})
const schedules = ref([])
const loading = ref(false)

const pad = (n) => String(n).padStart(2, '0')
const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 影院排片以今天起 7 天为主
const dates = Array.from({ length: 7 }, (_, i) => {
  const d = new Date()
  d.setDate(d.getDate() + i)
  return {
    value: fmt(d),
    label: i === 0 ? '今天' : i === 1 ? '明天' : week[d.getDay()],
    short: `${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  }
})
const date = ref(dates[0].value)

async function loadMovie() {
  loading.value = true
  try {
    movie.value = (await getMovieDetail(route.params.id)).data
  } finally {
    loading.value = false
  }
}
async function loadSchedule() {
  const res = await getSchedulesByMovie(route.params.id, date.value)
  schedules.value = res.data || []
}
function selectDate(v) {
  if (date.value === v) return
  date.value = v
  loadSchedule()
}
function selectSeat(row) {
  router.push('/app/seat/' + row.id)
}
function endTime(row) {
  if (!row.startTime || !movie.value.duration) return '—'
  const d = new Date(row.startTime.replace('T', ' ').replace(/-/g, '/'))
  d.setMinutes(d.getMinutes() + movie.value.duration + 10)
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(() => {
  loadMovie()
  loadSchedule()
})
</script>

<style scoped>
/* 影片头部：影院惯用的深色背景 */
.film-head {
  display: flex;
  gap: 26px;
  padding: 26px;
  background: var(--cms-ink);
  border-radius: var(--cms-radius);
  color: #fff;
}
.film-poster {
  width: 176px;
  flex-shrink: 0;
}
.film-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.film-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}
.film-title h1 {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #f5f2ec;
}
.film-title span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}
.film-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  flex-wrap: wrap;
}
.film-meta i {
  width: 1px;
  height: 10px;
  background: rgba(255, 255, 255, 0.2);
}
.film-score {
  margin-top: 18px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.film-score b {
  font-size: 34px;
  font-weight: 600;
  color: #e0a33e;
  letter-spacing: -0.5px;
}
.film-score span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}
.film-score.coming b {
  font-size: 22px;
  color: #d9b26a;
}
.credits {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px 32px;
}
.credits > div {
  display: flex;
  gap: 10px;
  font-size: 13px;
}
.credits dt {
  color: rgba(255, 255, 255, 0.4);
}
.credits dd {
  color: rgba(255, 255, 255, 0.8);
}
.synopsis {
  margin-top: 16px;
  max-width: 720px;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.6);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 场次 */
.schedule {
  margin-top: 24px;
  padding: 20px 24px 8px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.schedule-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.schedule-head h2 {
  font-size: 16px;
  font-weight: 600;
}
.hint {
  font-size: 12px;
  color: var(--cms-muted);
}
.date-strip {
  display: flex;
  gap: 8px;
  margin: 14px 0 4px;
  overflow-x: auto;
}
.date-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  min-width: 62px;
  padding: 6px 10px;
  border: 1px solid var(--cms-line);
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.2s;
}
.date-chip:hover {
  border-color: var(--cms-line-strong);
}
.date-chip.active {
  border-color: var(--cms-brand);
  background: #fbf4f5;
}
.date-chip.active .d-label {
  color: var(--cms-brand);
}
.d-label {
  font-size: 12px;
  color: var(--cms-text);
}
.d-date {
  font-size: 11px;
  color: var(--cms-muted);
}

.schedule-list {
  margin-top: 10px;
}
.schedule-row {
  display: grid;
  grid-template-columns: 110px 1fr 100px 100px 110px;
  align-items: center;
  gap: 16px;
  padding: 14px 4px;
  border-top: 1px solid #f1f2f4;
}
.schedule-row:first-child {
  border-top: none;
}
.s-time b {
  font-size: 19px;
  font-weight: 600;
  color: var(--cms-ink);
  letter-spacing: -0.3px;
  font-variant-numeric: tabular-nums;
}
.s-time span {
  display: block;
  font-size: 11px;
  color: var(--cms-muted);
  margin-top: 1px;
}
.hall-name {
  font-size: 13px;
  color: var(--cms-text);
}
.hall-sub {
  display: block;
  margin-top: 2px;
  font-size: 11px;
  color: var(--cms-muted);
}
.s-seat {
  font-size: 12px;
}
.s-seat .enough {
  color: var(--cms-sub);
}
.s-seat .none {
  color: var(--cms-muted);
}
.s-price {
  color: var(--cms-brand);
}
.s-price .cur {
  font-size: 12px;
}
.s-price b {
  font-size: 18px;
  font-weight: 600;
}
.s-action {
  text-align: right;
}

.schedule-empty {
  padding: 48px 0;
  text-align: center;
}
.schedule-empty p {
  font-size: 13px;
  color: var(--cms-sub);
}
.schedule-empty span {
  font-size: 12px;
  color: var(--cms-muted);
}

@media (max-width: 820px) {
  .film-head {
    gap: 16px;
    padding: 18px;
  }
  .film-poster {
    width: 110px;
  }
  .film-title h1 {
    font-size: 20px;
  }
  .film-meta i:nth-of-type(n + 2),
  .film-meta span:nth-of-type(n + 3) {
    display: none;
  }
  .schedule-row {
    grid-template-columns: 84px 1fr auto;
    gap: 10px;
  }
  .s-seat {
    display: none;
  }
  .s-price {
    text-align: right;
  }
  .s-action {
    grid-column: 2 / 4;
    text-align: left;
    padding-bottom: 4px;
  }
}
</style>
