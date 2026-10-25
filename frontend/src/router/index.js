import { createRouter, createWebHashHistory } from 'vue-router'

// 路由表：三端分别使用各自布局
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },

  // ===== 用户购票端 =====
  {
    path: '/app',
    component: () => import('@/layout/CustomerLayout.vue'),
    meta: { roles: ['ROLE_USER'] },
    redirect: '/app/home',
    children: [
      { path: 'home', component: () => import('@/views/app/Home.vue'), meta: { title: '首页' } },
      {
        path: 'movies',
        component: () => import('@/views/app/MovieList.vue'),
        meta: { title: '电影' }
      },
      {
        path: 'movie/:id',
        component: () => import('@/views/app/MovieDetail.vue'),
        meta: { title: '电影详情' }
      },
      {
        path: 'seat/:scheduleId',
        component: () => import('@/views/app/SeatSelect.vue'),
        meta: { title: '选座购票' }
      },
      {
        path: 'orders',
        component: () => import('@/views/app/OrderCenter.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'member',
        component: () => import('@/views/app/MemberCenter.vue'),
        meta: { title: '会员中心' }
      },
      {
        path: 'profile',
        component: () => import('@/views/app/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },

  // ===== 影院员工端 =====
  {
    path: '/staff',
    component: () => import('@/layout/StaffLayout.vue'),
    meta: { roles: ['ROLE_STAFF', 'ROLE_ADMIN'] },
    redirect: '/staff/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/staff/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'check',
        component: () => import('@/views/staff/Check.vue'),
        meta: { title: '检票核销' }
      },
      {
        path: 'schedule',
        component: () => import('@/views/staff/ScheduleQuery.vue'),
        meta: { title: '场次查询' }
      },
      {
        path: 'order',
        component: () => import('@/views/staff/OrderQuery.vue'),
        meta: { title: '订单查询' }
      },
      {
        path: 'member',
        component: () => import('@/views/staff/MemberHandle.vue'),
        meta: { title: '会员办理' }
      }
    ]
  },

  // ===== 管理后台 =====
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { roles: ['ROLE_ADMIN'] },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '票房大盘' }
      },
      {
        path: 'movie',
        component: () => import('@/views/admin/MovieManage.vue'),
        meta: { title: '电影管理' }
      },
      {
        path: 'hall',
        component: () => import('@/views/admin/HallManage.vue'),
        meta: { title: '影厅管理' }
      },
      {
        path: 'schedule',
        component: () => import('@/views/admin/ScheduleManage.vue'),
        meta: { title: '排片管理' }
      },
      {
        path: 'order',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'user',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'member',
        component: () => import('@/views/admin/MemberManage.vue'),
        meta: { title: '会员管理' }
      },
      {
        path: 'notice',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理' }
      }
    ]
  },

  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 全局前置守卫：登录校验 + 角色校验
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('cms_token')
  if (to.meta.public) {
    // 已登录访问登录页则直接进对应首页
    if (to.path === '/login' && token) {
      const roles = JSON.parse(localStorage.getItem('cms_roles') || '[]')
      if (roles.includes('ROLE_ADMIN')) return next('/admin/dashboard')
      if (roles.includes('ROLE_STAFF')) return next('/staff/dashboard')
      return next('/app/home')
    }
    return next()
  }
  if (!token) return next('/login')
  const roles = JSON.parse(localStorage.getItem('cms_roles') || '[]')
  if (to.matched.some((r) => r.meta.roles)) {
    const need = to.matched.flatMap((r) => r.meta.roles || [])
    const ok = need.some((r) => roles.includes(r))
    if (!ok) {
      if (roles.includes('ROLE_ADMIN')) return next('/admin/dashboard')
      if (roles.includes('ROLE_STAFF')) return next('/staff/dashboard')
      return next('/app/home')
    }
  }
  next()
})

export default router
