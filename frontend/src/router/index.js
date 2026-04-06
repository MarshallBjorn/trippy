import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import VerifyEmail from '../views/VerifyEmail.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import UsersView from '../views/UsersView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/verify',
    name: 'VerifyEmail',
    component: VerifyEmail
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '', 
        redirect: '/admin/users'
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: UsersView
      },
      {
        path: 'users/create',
        name: 'AdminUserCreate',
        component: () => import('../views/UserCreateView.vue')
      },
      {
        path: 'users/:id/edit',
        name: 'AdminUserEdit',
        component: () => import('../views/UserEditView.vue')
      },
      // { path: 'content', component: ContentView },
      // { path: 'dictionaries', component: DictionariesView }
    ]
  },
  {
    path: '/',
    redirect: '/admin/users' 
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('trippy_token')

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  }  
  else if (to.path === '/login' && isAuthenticated) {
    next('/admin/users')
  } 
  else {
    next()
  }
})

export default router