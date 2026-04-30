import { createRouter, createWebHistory } from 'vue-router';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/notices',
      name: 'notices',
      component: () => import('@/views/notice/NoticeList.vue'),
    },
    {
      path: '/notices/:id(\\d+)',
      name: 'notice-detail',
      component: () => import('@/views/notice/NoticeDetail.vue'),
    },
    {
      path: '/free',
      name: 'free',
      component: () => import('@/views/free/FreeList.vue'),
    },
    {
      path: '/free/write',
      name: 'free-write',
      component: () => import('@/views/free/FreeWrite.vue'),
    },
    {
      path: '/free/:id(\\d+)',
      name: 'free-detail',
      component: () => import('@/views/free/FreeDetail.vue'),
    },
  ],
});
