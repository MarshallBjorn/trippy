import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'

const Dashboard = { template: '<div class="p-8 text-2xl">Witaj w Panelu! <button @click="logout" class="text-red-500 text-sm ml-4">Wyloguj</button></div>', methods: { logout() { localStorage.removeItem('trippy_token'); this.$router.push('/login'); } } }

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('trippy_token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } 
  else if (to.path === '/login' && token) {
    next('/dashboard')
  } 
  else {
    next()
  }
})

export default router