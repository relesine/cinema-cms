<template>
  <div>
    <div class="page-head">
      <div>
        <h2>会员办理</h2>
        <p>前台开卡、充值，以及查看会员消费记录</p>
      </div>
    </div>

    <section class="panel mb16">
      <div class="panel-title">开卡 / 充值</div>
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="输入账号 / 昵称 / 手机号搜索用户"
          clearable
          style="max-width: 380px"
          @keyup.enter="search"
        />
        <el-button type="primary" @click="search">搜索</el-button>
      </div>
      <el-table :data="users" size="small" v-if="users.length">
        <el-table-column label="账号" prop="username" width="140" />
        <el-table-column label="昵称" prop="nickname" width="140" />
        <el-table-column label="手机号" prop="phone" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="open(row)">开卡</el-button>
            <el-button size="small" @click="recharge(row)">充值</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="panel">
      <div class="panel-title">
        会员列表
        <span>共 {{ total }} 位</span>
      </div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column label="账号" prop="username" width="130" />
        <el-table-column label="昵称" prop="nickname" width="130" />
        <el-table-column label="手机号" prop="phone" width="140" />
        <el-table-column label="等级" prop="levelName" width="110" />
        <el-table-column label="积分" width="100">
          <template #default="{ row }">{{ row.member.points }}</template>
        </el-table-column>
        <el-table-column label="余额" width="110">
          <template #default="{ row }"
            ><span class="money">￥{{ row.member.balance }}</span></template
          >
        </el-table-column>
        <el-table-column label="累计消费" width="120">
          <template #default="{ row }">
            <span class="money">￥{{ row.member.totalConsume }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开卡时间">
          <template #default="{ row }">{{ row.member.openTime }}</template>
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
    </section>

    <el-dialog v-model="rechargeVisible" title="会员充值" width="360px">
      <p class="dialog-line">
        为 <b>{{ target.nickname || target.username }}</b> 充值（模拟）
      </p>
      <el-input-number v-model="amount" :min="1" :step="50" />
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="doRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { searchUser, openMemberFor, rechargeFor, staffMemberPage } from '@/api/staff'
const users = ref([])
const keyword = ref('')
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const rechargeVisible = ref(false)
const target = ref({})
const amount = ref(100)

async function search() {
  if (keyword.value) users.value = (await searchUser(keyword.value)).data
}
async function open(row) {
  await openMemberFor(row.id)
  ElMessage.success('开卡成功')
  load()
}
function recharge(row) {
  target.value = row
  amount.value = 100
  rechargeVisible.value = true
}
async function doRecharge() {
  await rechargeFor(target.value.id, amount.value)
  ElMessage.success('充值成功')
  rechargeVisible.value = false
  load()
}
async function load() {
  loading.value = true
  try {
    const res = await staffMemberPage({ current: current.value, size: size.value })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
function onPage(p) {
  current.value = p
  load()
}
onMounted(load)
</script>

<style scoped>
.panel {
  padding: 16px 18px 18px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.panel-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--cms-ink);
}
.panel-title span {
  font-size: 11px;
  font-weight: 400;
  color: var(--cms-muted);
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.dialog-line {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--cms-sub);
}
.dialog-line b {
  color: var(--cms-ink);
}
</style>
