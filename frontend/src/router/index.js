import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/documents'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/documents',
    name: 'Documents',
    component: () => import('@/views/Documents.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/editor/:id?',
    name: 'Editor',
    component: () => import('@/views/Editor.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/document/:id',
    name: 'DocumentView',
    component: () => import('@/views/DocumentView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/public',
    name: 'PublicDocuments',
    component: () => import('@/views/PublicDocuments.vue')
  },
  {
    path: '/public/:id',
    name: 'PublicDocument',
    component: () => import('@/views/PublicDocument.vue')
  },
  {
    path: '/author/:userId/category/:category',
    name: 'AuthorCategory',
    component: () => import('@/views/AuthorCategory.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue')
  },
  {
    path: '/messages',
    name: 'MessageCenter',
    component: () => import('@/views/MessageCenter.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/content-management',
    name: 'ContentManagement',
    component: () => import('@/views/ContentManagement.vue'),
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next('/documents')
  } else {
    next()
  }
})

export default router
