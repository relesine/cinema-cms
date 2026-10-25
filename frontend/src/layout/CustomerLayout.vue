<template>
  <div class="site">
    <header class="site-header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/app/home')">
          <span class="brand-mark">星</span>
          <span class="brand-text">
            <b>星光国际影城</b>
            <small>STARLIGHT CINEMA</small>
          </span>
        </div>

        <nav class="nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <div class="account">
          <el-dropdown trigger="click" @command="onCommand">
            <span class="account-trigger">
              <span class="avatar">{{ initial }}</span>
              <span class="name">{{ userStore.nickname || userStore.username }}</span>
              <el-icon class="caret"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/app/profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="/app/orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="/app/member">会员中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="brand-mark sm">星</span>
          <span>星光国际影城</span>
        </div>
        <p>Cinema Management System · 本系统为学习演示项目，购票与支付均为模拟流程</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { label: '首页', path: '/app/home' },
  { label: '正在热映', path: '/app/movies' },
  { label: '我的订单', path: '/app/orders' },
  { label: '会员中心', path: '/app/member' },
  { label: '个人中心', path: '/app/profile' }
]

const initial = computed(() =>
  (userStore.nickname || userStore.username || 'U').slice(0, 1).toUpperCase()
)

function isActive(path) {
  if (path === '/app/movies') {
    return route.path.startsWith('/app/movie') || route.path.startsWith('/app/seat')
  }
  return route.path === path
}

async function onCommand(command) {
  if (command !== 'logout') {
    router.push(command)
    return
  }
  try {
    await ElMessageBox.confirm('退出当前账号？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await logout()
  } catch {
    /* 退出接口异常不阻断本地清理 */
  }
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.site {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--cms-bg);
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid var(--cms-line);
}
.header-inner {
  max-width: 1180px;
  margin: 0 auto;
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 36px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  user-select: none;
}
.brand-mark {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  background: var(--cms-brand);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border-radius: 2px;
}
.brand-mark.sm {
  width: 20px;
  height: 20px;
  font-size: 12px;
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.brand-text b {
  font-size: 16px;
  font-weight: 600;
  color: var(--cms-ink);
  letter-spacing: 0.5px;
}
.brand-text small {
  font-size: 9px;
  color: var(--cms-muted);
  letter-spacing: 1.6px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
  height: 100%;
}
.nav-item {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--cms-sub);
  transition: color 0.2s;
}
.nav-item:hover {
  color: var(--cms-ink);
}
.nav-item.active {
  color: var(--cms-ink);
  font-weight: 500;
}
.nav-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--cms-brand);
}

.account {
  flex-shrink: 0;
}
.account-trigger {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--cms-sub);
  cursor: pointer;
  outline: none;
}
.account-trigger:hover {
  color: var(--cms-ink);
}
.avatar {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #eef0f2;
  color: var(--cms-sub);
  font-size: 12px;
}
.caret {
  font-size: 11px;
  color: var(--cms-muted);
}

.site-main {
  flex: 1;
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.site-footer {
  background: var(--cms-ink);
  color: rgba(255, 255, 255, 0.45);
}
.footer-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  font-size: 12px;
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}
.footer-inner p {
  color: rgba(255, 255, 255, 0.35);
}

@media (max-width: 900px) {
  .header-inner {
    gap: 16px;
  }
  .brand-text small {
    display: none;
  }
  .nav {
    gap: 16px;
    overflow-x: auto;
  }
  .footer-inner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
