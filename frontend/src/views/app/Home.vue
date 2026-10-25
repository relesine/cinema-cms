<template>
  <div class="home">
    <!-- 正在热映：主推影片 -->
    <section v-if="featured" class="hero">
      <div class="hero-poster">
        <MoviePoster :movie="featured" />
      </div>
      <div class="hero-info">
        <div class="kicker">正在热映 · NOW SHOWING</div>
        <h1>
          {{ featured.name }}
          <span v-if="featured.nameEn">{{ featured.nameEn }}</span>
        </h1>
        <div class="meta">
          <span>{{ featured.genre }}</span>
          <i></i>
          <span>{{ featured.duration }} 分钟</span>
          <i></i>
          <span>{{ featured.region }} / {{ featured.language }}</span>
        </div>
        <p class="synopsis">{{ featured.synopsis }}</p>
        <div class="hero-actions">
          <div class="score">
            <b>{{ Number(featured.score || 0).toFixed(1) }}</b>
            <span>观众评分</span>
          </div>
          <el-button type="danger" @click="goDetail(featured.id)">查看排片</el-button>
          <el-button class="ghost" @click="$router.push('/app/movies')">全部影片</el-button>
        </div>
      </div>
      <aside class="hero-side">
        <div class="side-title">影城信息</div>
        <dl>
          <div>
            <dt>影厅</dt>
            <dd>5 个（含 IMAX 巨幕厅）</dd>
          </div>
          <div>
            <dt>座位</dt>
            <dd>452 座</dd>
          </div>
          <div>
            <dt>会员</dt>
            <dd>购票最低 8.5 折</dd>
          </div>
          <div>
            <dt>退票</dt>
            <dd>开场前 30 分钟可退</dd>
          </div>
        </dl>
      </aside>
    </section>

    <!-- 公告 -->
    <div v-if="notices.length" class="notice-bar">
      <span class="notice-label">影城公告</span>
      <el-carousel
        height="22px"
        direction="vertical"
        :autoplay="true"
        :interval="5000"
        indicator-position="none"
        arrow="never"
        class="notice-carousel"
      >
        <el-carousel-item v-for="n in notices" :key="n.id">
          <span class="notice-text"
            >{{ n.title }}<em v-if="n.content"> · {{ n.content }}</em></span
          >
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 热映列表 -->
    <div class="section-head">
      <h3>正在热映</h3>
      <span class="more" @click="$router.push('/app/movies')"
        >全部 {{ movies.length }} 部 &rsaquo;</span
      >
    </div>
    <div v-if="loading" class="movie-grid">
      <div v-for="i in 6" :key="i" class="skeleton">
        <div class="skeleton-poster"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>
    <div v-else class="movie-grid">
      <article v-for="m in movies" :key="m.id" class="movie-card" @click="goDetail(m.id)">
        <MoviePoster :movie="m" />
        <h4 class="movie-name">{{ m.name }}</h4>
        <div class="movie-sub">{{ m.genre }} · {{ m.duration }} 分钟</div>
        <div class="movie-foot">
          <span class="rate" v-if="m.score > 0">{{ Number(m.score).toFixed(1) }}</span>
          <span class="rate none" v-else>暂无评分</span>
          <span class="buy">购票</span>
        </div>
      </article>
    </div>

    <!-- 待映 -->
    <template v-if="upcoming.length">
      <div class="section-head">
        <h3>即将上映</h3>
        <span class="more" @click="$router.push('/app/movies')">更多 &rsaquo;</span>
      </div>
      <div class="coming-list">
        <article v-for="m in upcoming" :key="m.id" class="coming-item" @click="goDetail(m.id)">
          <div class="coming-poster"><MoviePoster :movie="m" /></div>
          <div class="coming-info">
            <h4>{{ m.name }}</h4>
            <div class="movie-sub">{{ m.genre }} · {{ m.duration }} 分钟</div>
            <div class="coming-date">{{ m.releaseDate }} 上映</div>
          </div>
        </article>
      </div>
    </template>

    <!-- 观影服务 -->
    <div class="section-head"><h3>观影服务</h3></div>
    <div class="service-row">
      <div class="service">
        <h4>多规格影厅</h4>
        <p>IMAX 巨幕、VIP 沙发厅、儿童厅与普通厅，按影片类型选择合适场次。</p>
      </div>
      <div class="service">
        <h4>会员购票折扣</h4>
        <p>银卡 9 折、金卡 8.5 折，消费即累计积分，积分决定会员等级。</p>
      </div>
      <div class="service">
        <h4>退改规则</h4>
        <p>开场前 30 分钟可申请退票，退款原路返回；已取票订单请到前台办理。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotMovies, getNotices, getMovieList } from '@/api/movie'
import MoviePoster from '@/components/MoviePoster.vue'

const router = useRouter()
const movies = ref([])
const upcoming = ref([])
const notices = ref([])
const loading = ref(false)

const featured = computed(() => movies.value[0])
const goDetail = (id) => router.push('/app/movie/' + id)

onMounted(async () => {
  loading.value = true
  try {
    const [h, n, u] = await Promise.all([
      getHotMovies(),
      getNotices(),
      getMovieList({ current: 1, size: 4, movieStatus: 0 })
    ])
    movies.value = h.data || []
    notices.value = (n.data || []).slice(0, 3)
    upcoming.value = u.data?.records || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* 主推影片：影院常用的深色信息带 */
.hero {
  display: grid;
  grid-template-columns: 172px 1fr 220px;
  gap: 28px;
  padding: 24px;
  background: var(--cms-ink);
  border-radius: var(--cms-radius);
  color: #fff;
}
.hero-poster {
  width: 172px;
}
.hero-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.kicker {
  font-size: 11px;
  letter-spacing: 1.2px;
  color: #c8a86b;
}
.hero-info h1 {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #f5f2ec;
}
.hero-info h1 span {
  margin-left: 8px;
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.5px;
  color: rgba(255, 255, 255, 0.4);
}
.meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}
.meta i {
  width: 1px;
  height: 10px;
  background: rgba(255, 255, 255, 0.2);
}
.synopsis {
  margin-top: 12px;
  max-width: 520px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.62);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hero-actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.score {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-right: 10px;
}
.score b {
  font-size: 28px;
  font-weight: 600;
  color: #e0a33e;
  letter-spacing: -0.5px;
}
.score span {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
}
.ghost {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.28);
  color: rgba(255, 255, 255, 0.8);
}
.ghost:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.4);
  color: #fff;
}

.hero-side {
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  padding-left: 22px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.side-title {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 1px;
  margin-bottom: 10px;
}
.hero-side dl > div {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 5px 0;
  font-size: 12px;
}
.hero-side dt {
  color: rgba(255, 255, 255, 0.45);
  flex-shrink: 0;
}
.hero-side dd {
  color: rgba(255, 255, 255, 0.78);
  text-align: right;
}

/* 公告条 */
.notice-bar {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  height: 40px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.notice-label {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 500;
  color: var(--cms-brand);
  padding-right: 12px;
  border-right: 1px solid var(--cms-line);
}
.notice-carousel {
  flex: 1;
  min-width: 0;
}
.notice-text {
  font-size: 13px;
  color: var(--cms-sub);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  line-height: 22px;
}
.notice-text em {
  font-style: normal;
  color: var(--cms-muted);
}

/* 影片网格 */
.movie-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 18px;
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
  color: var(--cms-ink);
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
.rate.none::before {
  content: '';
}
.buy {
  font-size: 12px;
  color: var(--cms-brand);
}
.movie-card:hover .buy {
  text-decoration: underline;
}

/* 待映 */
.coming-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.coming-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
  cursor: pointer;
}
.coming-item:hover {
  border-color: var(--cms-line-strong);
}
.coming-poster {
  width: 64px;
  flex-shrink: 0;
}
.coming-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.coming-info h4 {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.coming-date {
  margin-top: 4px;
  font-size: 12px;
  color: var(--cms-brand);
}

/* 服务说明 */
.service-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.service {
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.service h4 {
  font-size: 14px;
  font-weight: 600;
}
.service p {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--cms-sub);
}

/* 骨架屏 */
.skeleton-poster {
  aspect-ratio: 2 / 3;
  background: linear-gradient(180deg, #ecedef 0%, #f3f4f5 100%);
  border-radius: 3px;
}
.skeleton-line {
  height: 12px;
  margin-top: 8px;
  background: #ecedef;
  border-radius: 2px;
}
.skeleton-line.short {
  width: 60%;
}

@media (max-width: 1100px) {
  .hero {
    grid-template-columns: 150px 1fr;
  }
  .hero-side {
    display: none;
  }
  .movie-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 760px) {
  .hero {
    grid-template-columns: 110px 1fr;
    gap: 16px;
    padding: 16px;
  }
  .hero-poster {
    width: 110px;
  }
  .hero-info h1 {
    font-size: 20px;
  }
  .synopsis {
    display: none;
  }
  .movie-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
  .coming-list,
  .service-row {
    grid-template-columns: 1fr;
  }
}
</style>
