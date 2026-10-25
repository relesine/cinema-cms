<template>
  <div class="profile">
    <div class="profile-head">
      <div class="avatar">{{ initial }}</div>
      <div class="head-info">
        <h2>{{ user.nickname || user.username }}</h2>
        <div class="roles">
          <el-tag v-for="r in roles" :key="r" size="small" effect="plain">{{ roleText(r) }}</el-tag>
        </div>
      </div>
      <div class="head-actions">
        <el-button size="small" @click="$router.push('/app/orders')">我的订单</el-button>
        <el-button size="small" type="danger" plain @click="$router.push('/app/member')">
          会员中心
        </el-button>
      </div>
    </div>

    <div class="info-panel">
      <div class="panel-title">账号资料</div>
      <div class="info-grid">
        <div class="info-row">
          <span>账号</span><b>{{ user.username || '—' }}</b>
        </div>
        <div class="info-row">
          <span>昵称</span><b>{{ user.nickname || '—' }}</b>
        </div>
        <div class="info-row">
          <span>真实姓名</span><b>{{ user.realName || '未填写' }}</b>
        </div>
        <div class="info-row">
          <span>手机号</span><b>{{ user.phone || '未填写' }}</b>
        </div>
        <div class="info-row">
          <span>邮箱</span><b>{{ user.email || '未填写' }}</b>
        </div>
        <div class="info-row">
          <span>性别</span><b>{{ genderText }}</b>
        </div>
      </div>
      <p class="panel-note">如需修改账号资料，请联系影城前台或管理员。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getInfo } from '@/api/auth'

const user = ref({})
const roles = ref([])
const initial = computed(() =>
  (user.value.nickname || user.value.username || 'U').slice(0, 1).toUpperCase()
)
const genderText = computed(() => ({ 0: '未知', 1: '男', 2: '女' })[user.value.gender] || '未填写')
const roleText = (r) =>
  ({ ROLE_USER: '普通用户', ROLE_STAFF: '影院员工', ROLE_ADMIN: '管理员' })[r] || r

onMounted(async () => {
  const res = await getInfo()
  user.value = res.data.user
  roles.value = res.data.roles
})
</script>

<style scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.avatar {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #f0f1f3;
  color: var(--cms-sub);
  font-size: 22px;
  font-weight: 500;
}
.head-info h2 {
  font-size: 18px;
  font-weight: 600;
}
.roles {
  margin-top: 4px;
  display: flex;
  gap: 6px;
}
.head-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.info-panel {
  margin-top: 16px;
  padding: 20px 24px 24px;
  background: #fff;
  border: 1px solid var(--cms-line);
  border-radius: var(--cms-radius);
}
.panel-title {
  padding-bottom: 12px;
  border-bottom: 1px solid var(--cms-line);
  font-size: 14px;
  font-weight: 600;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 48px;
}
.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 11px 0;
  border-bottom: 1px solid #f3f4f5;
  font-size: 13px;
}
.info-row span {
  width: 72px;
  flex-shrink: 0;
  color: var(--cms-muted);
}
.info-row b {
  font-weight: 400;
  color: var(--cms-text);
}
.panel-note {
  margin-top: 16px;
  font-size: 12px;
  color: var(--cms-muted);
}

@media (max-width: 720px) {
  .profile-head {
    flex-wrap: wrap;
  }
  .head-actions {
    width: 100%;
    margin-left: 0;
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
