import { createRouter, createWebHistory } from 'vue-router';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
    { path: '/me', name: 'me', component: () => import('@/views/me/MyPageView.vue') },

    { path: '/notices', name: 'notices', component: () => import('@/views/notice/NoticeList.vue') },
    { path: '/notices/:id(\\d+)', name: 'notice-detail', component: () => import('@/views/notice/NoticeDetail.vue') },

    { path: '/free', name: 'free', component: () => import('@/views/free/FreeList.vue') },
    { path: '/free/write', name: 'free-write', component: () => import('@/views/free/FreeWrite.vue') },
    { path: '/free/:id(\\d+)', name: 'free-detail', component: () => import('@/views/free/FreeDetail.vue') },
    { path: '/free/:id(\\d+)/edit', name: 'free-edit', component: () => import('@/views/free/FreeEdit.vue') },

    { path: '/fanart', name: 'fanart', component: () => import('@/views/fanart/FanartList.vue') },
    { path: '/fanart/write', name: 'fanart-write', component: () => import('@/views/fanart/FanartWrite.vue') },
    { path: '/fanart/:id(\\d+)', name: 'fanart-detail', component: () => import('@/views/fanart/FanartDetail.vue') },
    { path: '/fanart/:id(\\d+)/edit', name: 'fanart-edit', component: () => import('@/views/fanart/FanartEdit.vue') },

    { path: '/clips', name: 'clips', component: () => import('@/views/clips/ClipList.vue') },
    { path: '/clips/write', name: 'clip-write', component: () => import('@/views/clips/ClipWrite.vue') },
    { path: '/clips/:id(\\d+)', name: 'clip-detail', component: () => import('@/views/clips/ClipDetail.vue') },
    { path: '/clips/:id(\\d+)/edit', name: 'clip-edit', component: () => import('@/views/clips/ClipEdit.vue') },

    { path: '/letters', name: 'letters', component: () => import('@/views/letters/LetterList.vue') },
    { path: '/letters/write', name: 'letter-write', component: () => import('@/views/letters/LetterWrite.vue') },
    { path: '/letters/:id(\\d+)', name: 'letter-detail', component: () => import('@/views/letters/LetterDetail.vue') },

    { path: '/pets', name: 'pets', component: () => import('@/views/pets/PetList.vue') },
    { path: '/pets/write', name: 'pet-write', component: () => import('@/views/pets/PetWrite.vue') },
    { path: '/pets/:id(\\d+)', name: 'pet-detail', component: () => import('@/views/pets/PetDetail.vue') },
    { path: '/pets/:id(\\d+)/edit', name: 'pet-edit', component: () => import('@/views/pets/PetEdit.vue') },

    { path: '/songs', name: 'songs', component: () => import('@/views/songs/SongList.vue') },
    { path: '/songs/add', name: 'song-add', component: () => import('@/views/songs/SongAdd.vue') },

    { path: '/offline', name: 'offline', component: () => import('@/views/offline/OfflineList.vue') },
    { path: '/offline/write', name: 'offline-write', component: () => import('@/views/offline/OfflineWrite.vue') },
    { path: '/offline/:id(\\d+)', name: 'offline-detail', component: () => import('@/views/offline/OfflineDetail.vue') },
    { path: '/offline/:id(\\d+)/edit', name: 'offline-edit', component: () => import('@/views/offline/OfflineEdit.vue') },
  ],
});
