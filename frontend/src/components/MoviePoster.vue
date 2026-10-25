<template>
  <div class="poster" :class="'tone-' + tone">
    <el-image v-if="movie.cover" :src="fullImg(movie.cover)" fit="cover" class="img" />
    <template v-else>
      <div class="poster-brand">星光国际影城</div>

      <div class="poster-main">
        <div class="poster-genre">{{ movie.genre }}</div>
        <h3 class="poster-name">{{ movie.name }}</h3>
        <div v-if="movie.nameEn" class="poster-en">{{ movie.nameEn }}</div>
      </div>

      <div class="poster-foot">
        <span>{{ movie.duration }} 分钟</span>
        <span class="dot">/</span>
        <span>{{ movie.region }}</span>
      </div>

      <div v-if="movie.score > 0" class="poster-score">
        <b>{{ Number(movie.score).toFixed(1) }}</b>
      </div>
      <div v-else-if="movie.movieStatus === 0" class="poster-badge">即将上映</div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { fullImg } from '@/api/upload'

const props = defineProps({
  movie: { type: Object, required: true }
})

// 影院物料常用的低饱和色调，按影片 id 稳定分配
const TONES = ['ink', 'olive', 'wine', 'steel', 'sand', 'slate']
const tone = computed(() => TONES[(props.movie.id || 0) % TONES.length])
</script>

<style scoped>
.poster {
  position: relative;
  width: 100%;
  aspect-ratio: 2 / 3;
  overflow: hidden;
  border-radius: 3px;
  background: #1a1d22;
  color: #f2f0eb;
  container-type: inline-size;
}
.img {
  width: 100%;
  height: 100%;
}

/* 低饱和深色底，模拟实拍剧照的影调 */
.tone-ink {
  background: linear-gradient(165deg, #262c35 0%, #14171c 62%, #101216 100%);
}
.tone-olive {
  background: linear-gradient(165deg, #2b2c24 0%, #191a15 62%, #101109 100%);
}
.tone-wine {
  background: linear-gradient(165deg, #33222a 0%, #1b1317 62%, #120d10 100%);
}
.tone-steel {
  background: linear-gradient(165deg, #24303a 0%, #151b20 62%, #0f1316 100%);
}
.tone-sand {
  background: linear-gradient(165deg, #32291f 0%, #1c1712 62%, #12100c 100%);
}
.tone-slate {
  background: linear-gradient(165deg, #272a33 0%, #16181e 62%, #101115 100%);
}
/* 极轻微颗粒感，避免大色块显得像占位图 */
.poster::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.35;
  background-image: repeating-linear-gradient(
    0deg,
    rgba(255, 255, 255, 0.018) 0 1px,
    transparent 1px 3px
  );
}

.poster-brand {
  position: absolute;
  top: 10px;
  left: 12px;
  font-size: 9px;
  letter-spacing: 1px;
  color: rgba(242, 240, 235, 0.5);
}
.poster-main {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 30px;
}
.poster-genre {
  font-size: 9px;
  letter-spacing: 1.5px;
  color: #c8a86b;
  text-transform: uppercase;
}
.poster-name {
  margin-top: 4px;
  font-size: 22px;
  line-height: 1.25;
  font-weight: 600;
  letter-spacing: 1px;
  color: #f4f1ea;
  text-wrap: balance;
}
.poster-en {
  margin-top: 3px;
  font-size: 9px;
  letter-spacing: 0.4px;
  color: rgba(242, 240, 235, 0.45);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.poster-foot {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 12px;
  font-size: 10px;
  color: rgba(242, 240, 235, 0.55);
}
.poster-foot .dot {
  margin: 0 4px;
  opacity: 0.5;
}
.poster-score {
  position: absolute;
  top: 8px;
  right: 10px;
  color: #e0a33e;
  font-size: 12px;
}
.poster-score b {
  font-size: 15px;
  font-weight: 600;
}
.poster-badge {
  position: absolute;
  top: 8px;
  right: 10px;
  padding: 1px 5px;
  font-size: 10px;
  color: #d9b26a;
  border: 1px solid rgba(217, 178, 106, 0.45);
  border-radius: 2px;
}

/* 小尺寸下标题跟随容器缩放 */
@container (max-width: 170px) {
  .poster-name {
    font-size: 18px;
  }
  .poster-main {
    bottom: 26px;
  }
}
@container (max-width: 140px) {
  .poster-name {
    font-size: 15px;
  }
  .poster-brand,
  .poster-en {
    display: none;
  }
}
</style>
