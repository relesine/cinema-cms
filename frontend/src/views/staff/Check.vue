<template>
  <div>
    <div class="page-head">
      <div>
        <h2>检票核销</h2>
        <p>支持扫码枪输入与手工录入订单号</p>
      </div>
    </div>

    <div class="check-panel">
      <div class="input-row">
        <el-input
          v-model="orderNo"
          size="large"
          placeholder="扫描或输入订单号，回车核销"
          clearable
          @keyup.enter="scan"
        >
          <template #prefix
            ><el-icon><Search /></el-icon
          ></template>
        </el-input>
        <el-button type="danger" size="large" @click="scan">核销</el-button>
      </div>
      <p class="hint">每张订单仅可核销一次；开场前 10 分钟开放入场。</p>
    </div>

    <div v-if="lastOrder" class="result" :class="lastSuccess ? 'ok' : 'fail'">
      <div class="result-title">
        <span class="dot"></span>
        <b>{{ lastMessage }}</b>
        <span class="result-tag">最近一次核销</span>
      </div>
      <div class="result-grid">
        <div>
          <span>影片</span><b>{{ lastOrder.movieName }}</b>
        </div>
        <div>
          <span>场次</span><b>{{ fmtTime(lastOrder.startTime) }}</b>
        </div>
        <div>
          <span>影厅</span><b>{{ lastOrder.hallName }}</b>
        </div>
        <div class="wide">
          <span>座位</span><b>{{ (lastOrder.seatNames || []).join('、') }}</b>
        </div>
      </div>
    </div>

    <section class="records">
      <div class="panel-head">
        <h3>检票记录</h3>
        <span>最近 50 条</span>
        <el-button size="small" @click="loadRecords">刷新</el-button>
      </div>
      <el-table :data="records" size="small">
        <el-table-column label="核销时间" width="180">
          <template #default="{ row }">{{ fmtTime(row.record.checkTime) }}</template>
        </el-table-column>
        <el-table-column label="订单号" prop="orderNo" width="260" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <span :class="row.record.result === 1 ? 'txt-ok' : 'txt-fail'">
              {{ row.record.result === 1 ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="record.note" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { staffScan, checkRecords } from '@/api/staff'

const orderNo = ref('')
const records = ref([])
const lastOrder = ref(null)
const lastSuccess = ref(false)
const lastMessage = ref('')

function fmtTime(t) {
  if (!t) return '—'
  const s = String(t).replace('T', ' ')
  return s.length >= 16 ? `${s.slice(0, 10)} ${s.slice(11, 16)}` : s
}

async function scan() {
  if (!orderNo.value) return
  const res = await staffScan(orderNo.value.trim())
  lastSuccess.value = res.data.success
  lastMessage.value = res.data.message
  lastOrder.value = res.data.order
  orderNo.value = ''
  loadRecords()
}
async function loadRecords() {
  records.value = (await checkRecords()).data
}
onMounted(loadRecords)
</script>

<style scoped>
.check-panel {
  padding: 20px 22px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.input-row {
  display: flex;
  gap: 10px;
  max-width: 620px;
}
.hint {
  margin-top: 10px;
  font-size: 12px;
  color: var(--cms-muted);
}

.result {
  margin-top: 12px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-left: 3px solid var(--cms-line-strong);
  border-radius: var(--cms-radius);
}
.result.ok {
  border-left-color: #2f7d4f;
}
.result.fail {
  border-left-color: var(--cms-brand);
}
.result-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.result-title .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2f7d4f;
}
.result.fail .result-title .dot {
  background: var(--cms-brand);
}
.result-title b {
  font-size: 14px;
  font-weight: 600;
  color: var(--cms-ink);
}
.result-tag {
  margin-left: auto;
  font-size: 11px;
  color: var(--cms-muted);
}
.result-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 24px;
}
.result-grid > div {
  display: flex;
  gap: 10px;
  font-size: 12px;
}
.result-grid .wide {
  grid-column: 1 / -1;
}
.result-grid span {
  flex-shrink: 0;
  color: var(--cms-muted);
}
.result-grid b {
  font-weight: 400;
  color: var(--cms-text);
}

.records {
  margin-top: 12px;
  padding: 14px 18px 18px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.panel-head h3 {
  font-size: 14px;
  font-weight: 600;
}
.panel-head span {
  flex: 1;
  font-size: 11px;
  color: var(--cms-muted);
}
.txt-ok {
  font-size: 12px;
  color: #2f7d4f;
}
.txt-fail {
  font-size: 12px;
  color: var(--cms-brand);
}

@media (max-width: 720px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
  .input-row {
    flex-direction: column;
  }
}
</style>
