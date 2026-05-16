<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';

const route = useRoute();

interface MenuItem {
  to: string;
  label: string;
  icon: string;
  matchPrefix?: string;
}

const menu: MenuItem[] = [
  { to: '/', label: '홈', icon: '⌂', matchPrefix: '' },
  { to: '/notices', label: '공지사항', icon: '☆' },
  { to: '/free', label: '자유게시판', icon: '💬' },
  { to: '/fanart', label: '팬아트 갤러리', icon: '🎨' },
  { to: '/pets', label: '반려동물 사진', icon: '🐾' },
  { to: '/songs', label: '노래추천', icon: '🎵' },
  { to: '/letters', label: '팬레터', icon: '✉' },
  { to: '/clips', label: '클립 공유', icon: '🎬' },
];

function isActive(item: MenuItem): boolean {
  if (item.to === '/') return route.path === '/';
  return route.path === item.to || route.path.startsWith(item.to + '/');
}

const activeIndex = computed(() => menu.findIndex(isActive));
</script>

<template>
  <aside class="w-60 shrink-0 bg-surface border-r border-border flex flex-col">
    <div class="px-6 pt-6 pb-4">
      <RouterLink to="/" class="block">
        <h1 class="text-2xl font-extrabold leading-tight">
          <span class="text-pepper">🌶</span>
          <span class="text-ink">김늉비</span>
          <span class="text-corn">고</span>
          <br />
          <span class="text-ink pl-7">추밭</span>
        </h1>
      </RouterLink>
    </div>

    <div class="mx-4 mb-5 p-3 rounded-xl bg-elevated border border-border">
      <div class="flex items-center gap-2">
        <div class="w-9 h-9 rounded-lg bg-violet/30 flex items-center justify-center text-base">
          🌽
        </div>
        <div class="min-w-0 flex-1">
          <div class="text-xs text-corn font-semibold truncate">우리아기 불고노</div>
          <div class="text-xs text-ink-muted truncate">혀모니카는</div>
        </div>
      </div>
      <div class="mt-2 text-[11px] text-ink-muted truncate">▶ 오늘의 애교송 재생</div>
    </div>

    <div class="px-6 pb-2 text-[11px] tracking-widest text-ink-muted">MENU</div>

    <nav class="flex-1 px-3 pb-6 overflow-y-auto">
      <ul class="space-y-1">
        <li v-for="(item, i) in menu" :key="item.to">
          <RouterLink
            :to="item.to"
            class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
            :class="
              activeIndex === i
                ? 'bg-violet-deep/40 text-ink font-semibold'
                : 'text-ink-muted hover:text-ink hover:bg-elevated'
            "
          >
            <span class="w-5 text-center text-base">{{ item.icon }}</span>
            <span class="flex-1">{{ item.label }}</span>
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>
