<template>
  <el-container class="console">
    <el-aside width="208px" class="aside">
      <div class="brand">
        <span class="mark">星</span>
        <div class="brand-text">
          <b>星光影城</b>
          <small>管理后台</small>
        </div>
      </div>

      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon><span>票房大盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/movie">
          <el-icon><Film /></el-icon><span>电影管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/schedule">
          <el-icon><Calendar /></el-icon><span>排片管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/hall">
          <el-icon><Grid /></el-icon><span>影厅管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><Tickets /></el-icon><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/member">
          <el-icon><GoldMedal /></el-icon><span>会员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/notice">
          <el-icon><Bell /></el-icon><span>公告管理</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">v1.1.0 · 演示环境</div>
    </el-aside>

    <el-container class="body">
      <el-header class="topbar">
        <div class="crumb">
          <span class="crumb-root">管理后台</span>
          <el-icon><ArrowRight /></el-icon>
          <span class="crumb-now">{{ $route.meta.title }}</span>
        </div>
        <div class="top-right">
          <span class="user">{{ userStore.nickname || userStore.username }}</span>
          <el-divider direction="vertical" />
          <el-button link @click="doLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

async function doLogout() {
  try {
    await logout()
  } catch {
    // 退出接口异常不阻断本地清理
  }
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.console {
  height: 100vh;
}
.aside {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid var(--cms-line);
  overflow-x: hidden;
}
.brand {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 9px;
  border-bottom: 1px solid var(--cms-line);
}
.mark {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  background: var(--cms-brand);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border-radius: 2px;
}
.brand-text b {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--cms-ink);
  line-height: 1.2;
}
.brand-text small {
  display: block;
  font-size: 10px;
  color: var(--cms-muted);
}

.menu {
  flex: 1;
  padding: 8px;
  border-right: none;
  --el-menu-item-height: 38px;
  --el-menu-item-font-size: 13px;
  --el-menu-hover-bg-color: #f6f7f8;
  --el-menu-active-color: var(--cms-brand);
  --el-menu-text-color: #4b5563;
  --el-menu-bg-color: transparent;
}
.menu :deep(.el-menu-item) {
  margin-bottom: 2px;
  border-radius: 3px;
  color: var(--cms-sub);
}
.menu :deep(.el-menu-item .el-icon) {
  font-size: 15px;
  color: #8b9198;
}
.menu :deep(.el-menu-item:hover) {
  background: #f6f7f8;
  color: var(--cms-ink);
}
.menu :deep(.el-menu-item.is-active) {
  background: #f3f4f5;
  color: var(--cms-ink);
  font-weight: 500;
}
.menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--cms-brand);
}
.menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: -8px;
  top: 9px;
  bottom: 9px;
  width: 2px;
  background: var(--cms-brand);
}

.aside-foot {
  padding: 12px 16px;
  border-top: 1px solid var(--cms-line);
  font-size: 10px;
  color: var(--cms-muted);
}

.body {
  background: var(--cms-bg);
}
.topbar {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid var(--cms-line);
}
.crumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--cms-muted);
}
.crumb-now {
  color: var(--cms-text);
}
.crumb .el-icon {
  font-size: 10px;
}
.top-right {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}
.user {
  color: var(--cms-sub);
}
.main {
  padding: 16px 20px 24px;
}
</style>
