import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import VerifyEmail from '../views/VerifyEmail.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import UsersView from '../views/UsersView.vue'
import UserCreateView from '../views/UserCreateView.vue'
import UserEditView from '../views/UserEditView.vue'
import DictionariesView from '../views/DictionariesView.vue'
import CurrencyCreateView from '../views/CurrencyCreateView.vue'
import RoleCreateView from '../views/RoleCreateView.vue'
import TripsView from '../views/TripsView.vue'
import ModerationView from '../views/ModerationView.vue'

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
        component: UserCreateView
      },
      {
        path: 'users/:id/edit',
        name: 'AdminUserEdit',
        component: UserEditView,
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: UsersView
      },
      { 
        path: 'dictionaries', 
        name: 'AdminDictionaries',
        component: DictionariesView
      },
      { 
        path: 'dictionaries/currencies/create', 
        name: 'AdminCurrencyCreate',
        component: CurrencyCreateView
      },
      { 
        path: 'dictionaries/roles/create', 
        name: 'AdminRoleCreate',
        component: RoleCreateView
      },
      {
        path: 'trips',
        name: 'AdminTrips',
        component: TripsView
      },
      {
        path: 'moderation',
        name: 'AdminModeration',
        component: ModerationView
      }
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