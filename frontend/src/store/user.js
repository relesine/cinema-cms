import { defineStore } from 'pinia'

// 用户登录状态：token / 用户信息 / 角色，手动持久化到 localStorage
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('cms_token') || '',
    userId: localStorage.getItem('cms_userId') || '',
    username: localStorage.getItem('cms_username') || '',
    nickname: localStorage.getItem('cms_nickname') || '',
    roles: JSON.parse(localStorage.getItem('cms_roles') || '[]'),
    primaryRole: localStorage.getItem('cms_primaryRole') || ''
  }),
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.roles.includes('ROLE_ADMIN'),
    isStaff: (state) => state.roles.includes('ROLE_STAFF') || state.roles.includes('ROLE_ADMIN'),
    isUser: (state) => state.roles.includes('ROLE_USER')
  },
  actions: {
    setLogin(loginVO) {
      this.token = loginVO.token
      this.userId = loginVO.userId
      this.username = loginVO.username
      this.nickname = loginVO.nickname
      this.roles = loginVO.roles || []
      this.primaryRole = loginVO.primaryRole
      localStorage.setItem('cms_token', loginVO.token)
      localStorage.setItem('cms_userId', loginVO.userId)
      localStorage.setItem('cms_username', loginVO.username)
      localStorage.setItem('cms_nickname', loginVO.nickname || '')
      localStorage.setItem('cms_roles', JSON.stringify(this.roles))
      localStorage.setItem('cms_primaryRole', loginVO.primaryRole)
    },
    // 按主角色返回登录后应进入的首页路径
    homePath() {
      if (this.roles.includes('ROLE_ADMIN')) return '/admin/dashboard'
      if (this.roles.includes('ROLE_STAFF')) return '/staff/dashboard'
      return '/app/home'
    },
    clear() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.nickname = ''
      this.roles = []
      this.primaryRole = ''
      localStorage.clear()
    }
  }
})
