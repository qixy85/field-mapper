import { createRouter, createWebHistory } from 'vue-router'
import Login from './views/Login.vue'
import AdminDashboard from './views/AdminDashboard.vue'
import UserDashboard from './views/UserDashboard.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/admin', component: AdminDashboard, meta: { requiresAuth: true, role: 'ADMIN' } },
  { path: '/', component: UserDashboard, meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== role) {
    next('/')
  } else {
    next()
  }
})

export default router
