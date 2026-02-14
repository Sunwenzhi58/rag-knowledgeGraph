import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(err => err)
}

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/ai-chat',
    children: [
      // 核心功能
      { path: 'ai-chat', name: 'AiChat', meta: { name: 'AI 对话' }, component: () => import('../views/manager/AiChat') },
      { path: 'knowledge-base', name: 'KnowledgeBase', meta: { name: '知识库' }, component: () => import('../views/manager/KnowledgeBase') },
      { path: 'org-tags', name: 'OrgTags', meta: { name: '组织标签' }, component: () => import('../views/manager/OrgTags') },

      // 系统管理
      { path: 'user-management', name: 'UserManagement', meta: { name: '用户管理' }, component: () => import('../views/manager/UserManagement') },
      { path: 'person-center', name: 'PersonCenter', meta: { name: '个人中心' }, component: () => import('../views/manager/PersonCenter') },

      // 保留原有功能页面
      { path: '403', name: 'NoAuth', meta: { name: '无权限' }, component: () => import('../views/manager/403') },
      { path: 'home', name: 'Home', meta: { name: '系统首页' }, component: () => import('../views/manager/Home') },
      { path: 'admin', name: 'Admin', meta: { name: '管理员信息' }, component: () => import('../views/manager/Admin') },
      { path: 'person', name: 'Person', meta: { name: '个人信息' }, component: () => import('../views/manager/Person') },
      { path: 'password', name: 'Password', meta: { name: '修改密码' }, component: () => import('../views/manager/Password') },
      { path: 'notice', name: 'Notice', meta: { name: '公告信息' }, component: () => import('../views/manager/Notice') },
    ]
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    children: [
      { path: 'home', name: 'FrontHome', meta: { name: '系统首页' }, component: () => import('../views/front/Home') },
    ]
  },
  { path: '/user/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/user/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'NotFound', meta: { name: '无法访问' }, component: () => import('../views/404.vue') },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
