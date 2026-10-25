<template>
  <div v-loading="loading">
    <!-- 未开通 -->
    <div v-if="!center.opened" class="join-card">
      <h2>开通星光会员</h2>
      <p>免费开卡，购票累计积分。积分达到门槛自动升级，银卡 9 折、金卡 8.5 折。</p>
      <div class="benefits">
        <div><b>500 积分</b><span>升级银卡 · 9 折</span></div>
        <div><b>2000 积分</b><span>升级金卡 · 8.5 折</span></div>
        <div><b>消费 1 元</b><span>累计 1 积分</span></div>
      </div>
      <el-button type="danger" @click="open">立即开通</el-button>
    </div>

    <!-- 会员卡 -->
    <template v-else>
      <div class="member-card">
        <div class="mc-left">
          <div class="mc-brand">STARLIGHT MEMBER</div>
          <div class="mc-level">{{ level.levelName }}</div>
          <div class="mc-name">{{ userStore.nickname || userStore.username }}</div>
        </div>
        <div class="mc-mid">
          <div class="mc-item">
            <span>积分</span>
            <b>{{ member.points || 0 }}</b>
          </div>
          <div class="mc-item">
            <span>累计消费</span>
            <b>￥{{ member.totalConsume || 0 }}</b>
          </div>
          <div class="mc-item">
            <span>购票折扣</span>
            <b>{{ level.discount ? Number(level.discount * 10).toFixed(1) + ' 折' : '—' }}</b>
          </div>
        </div>
        <div class="mc-right">
          <span class="balance-label">账户余额</span>
          <div class="balance">￥{{ member.balance || 0 }}</div>
          <el-button class="recharge" size="small" @click="rechargeDialog = true">充值</el-button>
        </div>
      </div>

      <div class="section-head"><h3>等级权益</h3></div>
      <div class="level-grid">
        <div
          v-for="lv in levels"
          :key="lv.id"
          class="level-item"
          :class="{ current: currentLv === lv.id }"
        >
          <div class="lv-head">
            <h4>{{ lv.levelName }}</h4>
            <span v-if="currentLv === lv.id" class="lv-now">当前等级</span>
          </div>
          <p>{{ lv.description }}</p>
          <ul>
            <li>满 {{ lv.minPoints }} 积分升级</li>
            <li>购票 {{ Number(lv.discount * 10).toFixed(1) }} 折</li>
          </ul>
        </div>
      </div>
    </template>

    <el-dialog v-model="rechargeDialog" title="会员充值" width="380px">
      <p class="recharge-tip">选择充值金额（演示环境，不产生真实扣款）</p>
      <el-radio-group v-model="amount" class="recharge-amounts">
        <el-radio-button :value="100">￥100</el-radio-button>
        <el-radio-button :value="300">￥300</el-radio-button>
        <el-radio-button :value="500">￥500</el-radio-button>
      </el-radio-group>
      <template #footer>
        <el-button @click="rechargeDialog = false">取消</el-button>
        <el-button type="danger" @click="doRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { memberCenter, openMember, rechargeMember, memberLevels } from '@/api/order'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const center = ref({})
const levels = ref([])
const rechargeDialog = ref(false)
const amount = ref(100)

const member = computed(() => center.value.member || {})
const level = computed(() => center.value.level || {})
const currentLv = computed(() => member.value.levelId)

async function load() {
  loading.value = true
  try {
    const [c, l] = await Promise.all([memberCenter(), memberLevels()])
    center.value = c.data
    levels.value = l.data
  } finally {
    loading.value = false
  }
}
async function open() {
  await openMember()
  ElMessage.success('会员开通成功')
  load()
}
async function doRecharge() {
  await rechargeMember(amount.value)
  ElMessage.success('充值成功')
  rechargeDialog.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
/* 开通引导 */
.join-card {
  padding: 44px 40px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.join-card h2 {
  font-size: 20px;
  font-weight: 600;
}
.join-card > p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--cms-sub);
}
.benefits {
  display: flex;
  gap: 40px;
  margin: 24px 0 28px;
}
.benefits > div {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-left: 14px;
  border-left: 2px solid var(--cms-line);
}
.benefits b {
  font-size: 14px;
  font-weight: 500;
  color: var(--cms-ink);
}
.benefits span {
  font-size: 12px;
  color: var(--cms-muted);
}

/* 会员卡 */
.member-card {
  display: grid;
  grid-template-columns: 1.2fr 1.4fr 0.8fr;
  gap: 24px;
  padding: 26px 28px;
  background: #1a1a1c;
  border: 1px solid #2b2b2f;
  border-radius: var(--cms-radius);
  color: #f0e8d8;
}
.mc-brand {
  font-size: 9px;
  letter-spacing: 2px;
  color: rgba(240, 232, 216, 0.35);
}
.mc-level {
  margin-top: 10px;
  font-size: 22px;
  font-weight: 600;
  color: #d3b078;
  letter-spacing: 1px;
}
.mc-name {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(240, 232, 216, 0.6);
}
.mc-mid {
  display: flex;
  align-items: center;
  gap: 36px;
  border-left: 1px solid rgba(255, 255, 255, 0.08);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0 32px;
}
.mc-item span {
  display: block;
  font-size: 11px;
  color: rgba(240, 232, 216, 0.45);
}
.mc-item b {
  font-size: 18px;
  font-weight: 500;
  color: #f0e8d8;
  font-variant-numeric: tabular-nums;
}
.mc-right {
  text-align: right;
}
.balance-label {
  font-size: 11px;
  color: rgba(240, 232, 216, 0.45);
}
.balance {
  margin: 2px 0 10px;
  font-size: 26px;
  font-weight: 600;
  color: #f0e8d8;
  font-variant-numeric: tabular-nums;
}
.recharge {
  background: transparent;
  border-color: rgba(211, 176, 120, 0.5);
  color: #d3b078;
}
.recharge:hover {
  background: rgba(211, 176, 120, 0.1);
  border-color: #d3b078;
  color: #d3b078;
}

/* 等级权益 */
.level-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.level-item {
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.level-item.current {
  border-color: var(--cms-brand);
  box-shadow: inset 2px 0 0 var(--cms-brand);
}
.lv-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.lv-head h4 {
  font-size: 14px;
  font-weight: 600;
}
.lv-now {
  font-size: 11px;
  color: var(--cms-brand);
}
.level-item p {
  margin-top: 6px;
  font-size: 12px;
  color: var(--cms-sub);
}
.level-item ul {
  margin-top: 10px;
  list-style: none;
}
.level-item li {
  padding: 3px 0;
  font-size: 12px;
  color: var(--cms-text);
}
.level-item li::before {
  content: '·';
  margin-right: 6px;
  color: var(--cms-muted);
}

.recharge-tip {
  font-size: 12px;
  color: var(--cms-muted);
  margin-bottom: 14px;
}
.recharge-amounts {
  display: flex;
  width: 100%;
}
.recharge-amounts :deep(.el-radio-button) {
  flex: 1;
}
.recharge-amounts :deep(.el-radio-button__inner) {
  width: 100%;
}

@media (max-width: 900px) {
  .member-card {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .mc-mid {
    border: none;
    padding: 0;
    gap: 24px;
  }
  .mc-right {
    text-align: left;
  }
  .level-grid {
    grid-template-columns: 1fr;
  }
  .benefits {
    flex-direction: column;
    gap: 14px;
  }
}
</style>
