import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ChangePassword from '@/views/ChangePassword.vue'
import UserProfileView from '@/views/UserProfileView.vue'
import MomentDetail from '@/views/MomentDetail.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfileView,
    meta: { requiresAuth: true }
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: ChangePassword,
    meta: { requiresAuth: true }
  },
  {
    path: '/moment/:id',
    name: 'MomentDetail',
    component: MomentDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/user/:id/profile',
    name: 'UserProfile',
    component: UserProfileView,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ?¡¤??????
// router.beforeEach((to, from, next) => {
//   const userStore = useUserStore()
//   if (to.meta.requiresAuth && !userStore.currentUser) {
//     next('/login')
//   } else {
//     next()
//   }
// })

export default router