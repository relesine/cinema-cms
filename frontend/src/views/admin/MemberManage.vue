<template>
  <div>
    <div class="page-head">
      <div>
        <h2>会员管理</h2>
        <p>会员等级、积分与余额一览</p>
      </div>
    </div>

    <div class="panel">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="账号" prop="username" width="130" />
        <el-table-column label="昵称" prop="nickname" width="130" />
        <el-table-column label="手机号" prop="phone" width="140" />
        <el-table-column label="会员等级" prop="levelName" width="120" />
        <el-table-column label="折扣" width="90">
          <template #default="{ row }">{{ Number(row.discount * 10).toFixed(1) }} 折</template>
        </el-table-column>
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
        <el-table-column label="开卡时间" min-width="160">
          <template #default="{ row }">{{ row.member.openTime }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.member.status === 1 ? 'success' : 'info'"
              disable-transitions
            >
              {{ row.member.status === 1 ? '有效' : '冻结' }}
            </el-tag>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminMemberPage } from '@/api/admin'
const list = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)
async function load() {
  loading.value = true
  try {
    const r = await adminMemberPage({ current: current.value, size: size.value })
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
