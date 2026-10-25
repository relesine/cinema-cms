<template>
  <div class="login-page">
    <!-- 左侧品牌信息 -->
    <aside class="login-side">
      <div class="side-brand">
        <span class="mark">星</span>
        <div>
          <b>星光国际影城</b>
          <small>STARLIGHT CINEMA</small>
        </div>
      </div>

      <div class="side-copy">
        <h1>在线选座<br />影院票务系统</h1>
        <p>覆盖购票、会员、检票与排片管理的完整业务流程。</p>
      </div>

      <div class="side-facts">
        <div><b>5</b><span>个影厅</span></div>
        <div><b>452</b><span>个座位</span></div>
        <div><b>8.5</b><span>折会员价</span></div>
      </div>

      <div class="side-foot">Cinema Management System · v1.1.0</div>
    </aside>

    <!-- 右侧表单 -->
    <main class="login-main">
      <div class="form-wrap">
        <div class="switch">
          <button :class="{ active: tab === 'login' }" @click="tab = 'login'">登录</button>
          <button :class="{ active: tab === 'register' }" @click="tab = 'register'">注册</button>
        </div>

        <template v-if="tab === 'login'">
          <h2>欢迎回来</h2>
          <p class="sub">输入账号与密码登录，系统将按角色进入对应工作台</p>
          <el-form :model="loginForm" @keyup.enter="doLogin">
            <el-form-item>
              <el-input
                v-model="loginForm.username"
                placeholder="账号"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
            <el-button
              type="danger"
              size="large"
              class="submit"
              :loading="loading"
              @click="doLogin"
            >
              登录
            </el-button>
          </el-form>
        </template>

        <template v-else>
          <h2>注册购票账号</h2>
          <p class="sub">注册后即可在线选座，开卡后享受会员折扣</p>
          <el-form :model="regForm" @keyup.enter="doRegister">
            <el-form-item>
              <el-input
                v-model="regForm.username"
                placeholder="账号（4-20 位）"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="regForm.password"
                type="password"
                placeholder="密码（6-20 位）"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="regForm.nickname"
                placeholder="昵称（选填）"
                :prefix-icon="Avatar"
              />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.phone" placeholder="手机号（选填）" :prefix-icon="Phone" />
            </el-form-item>
            <el-button
              type="danger"
              size="large"
              class="submit"
              :loading="loading"
              @click="doRegister"
            >
              注册
            </el-button>
          </el-form>
        </template>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar, Phone } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const tab = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '', phone: '' })

async function doLogin() {
  if (!loginForm.username || !loginForm.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const res = await login(loginForm)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push(userStore.homePath())
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!regForm.username || !regForm.password) return ElMessage.warning('请填写账号和密码')
  loading.value = true
  try {
    await register(regForm)
    ElMessage.success('注册成功，请登录')
    loginForm.username = regForm.username
    loginForm.password = regForm.password
    tab.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: #fff;
}

/* 左侧 */
.login-side {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 36px 48px;
  background: var(--cms-ink);
  color: #fff;
}
.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.mark {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  background: var(--cms-brand);
  font-size: 16px;
  font-weight: 600;
  border-radius: 2px;
}
.side-brand b {
  display: block;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.5px;
}
.side-brand small {
  display: block;
  font-size: 8px;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.3);
}

.side-copy {
  margin: auto 0;
}
.side-copy h1 {
  font-size: 38px;
  line-height: 1.35;
  font-weight: 600;
  letter-spacing: 1px;
  color: #f3efe7;
}
.side-copy p {
  margin-top: 14px;
  max-width: 380px;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.45);
}
.side-facts {
  display: flex;
  gap: 40px;
  padding-top: 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
.side-facts b {
  display: block;
  font-size: 24px;
  font-weight: 500;
  color: #d3b078;
  letter-spacing: -0.5px;
}
.side-facts span {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
}
.side-foot {
  margin-top: 26px;
  font-size: 10px;
  letter-spacing: 0.5px;
  color: rgba(255, 255, 255, 0.22);
}

/* 右侧 */
.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.form-wrap {
  width: 100%;
  max-width: 360px;
}
.switch {
  display: flex;
  gap: 24px;
  margin-bottom: 26px;
  border-bottom: 1px solid var(--cms-line);
}
.switch button {
  position: relative;
  padding: 0 0 10px;
  border: 0;
  background: none;
  font-family: inherit;
  font-size: 15px;
  color: var(--cms-sub);
  cursor: pointer;
}
.switch button.active {
  color: var(--cms-ink);
  font-weight: 600;
}
.switch button.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--cms-brand);
}
.form-wrap h2 {
  font-size: 22px;
  font-weight: 600;
}
.sub {
  margin: 6px 0 22px;
  font-size: 12px;
  color: var(--cms-muted);
}
.submit {
  width: 100%;
  margin-top: 4px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .login-side {
    display: none;
  }
  .login-main {
    min-height: 100vh;
  }
}
</style>
