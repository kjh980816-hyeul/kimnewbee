import { createRouter, createWebHistory } from 'vue-router';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },

    { path: '/notices', name: 'notices', component: () => import('@/views/notice/NoticeList.vue') },
    { path: '/notices/:id(\\d+)', name: 'notice-detail', component: () => import('@/views/notice/NoticeDetail.vue') },

    { path: '/free', name: 'free', component: () => import('@/views/free/FreeList.vue') },
    { path: '/free/write', name: 'free-write', component: () => import('@/views/free/FreeWrite.vue') },
    { path: '/free/:id(\\d+)', name: 'free-detail', component: () => import('@/views/free/FreeDetail.vue') },

    { path: '/fanart', name: 'fanart', component: () => import('@/views/fanart/FanartList.vue') },
    { path: '/fanart/write', name: 'fanart-write', component: () => import('@/views/fanart/FanartWrite.vue') },
    { path: '/fanart/:id(\\d+)', name: 'fanart-detail', component: () => import('@/views/fanart/FanartDetail.vue') },

    { path: '/clips', name: 'clips', component: () => import('@/views/clips/ClipList.vue') },
    { path: '/clips/write', name: 'clip-write', component: () => import('@/views/clips/ClipWrite.vue') },
    { path: '/clips/:id(\\d+)', name: 'clip-detail', component: () => import('@/views/clips/ClipDetail.vue') },

    { path: '/letters', name: 'letters', component: () => import('@/views/letters/LetterList.vue') },
    { path: '/letters/write', name: 'letter-write', component: () => import('@/views/letters/LetterWrite.vue') },
    { path: '/letters/:id(\\d+)', name: 'letter-detail', component: () => import('@/views/letters/LetterDetail.vue') },

    { path: '/pets', name: 'pets', component: () => import('@/views/pets/PetList.vue') },
    { path: '/pets/write', name: 'pet-write', component: () => import('@/views/pets/PetWrite.vue') },
    { path: '/pets/:id(\\d+)', name: 'pet-detail', component: () => import('@/views/pets/PetDetail.vue') },
  ],
});
