<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { fetchMe } from '@/api/me';
import { logout } from '@/api/auth';
import { isHttpStatus } from '@/api/error';
import type { CurrentUser } from '@/types/user';

const route = useRoute();
const router = useRouter();
const me = ref<CurrentUser | null>(null);
const menuOpen = ref(false);

interface NavItem {
  to: string;
  label: string;
  matchPrefix?: string;
}

const nav: NavItem[] = [
  { to: '/', label: '홈' },
  { to: '/free', label: '자유게시판' },
  { to: '/fanart', label: '팬아트' },
  { to: '/pets', label: '반려동물' },
  { to: '/songs', label: '노래추천' },
  { to: '/letters', label: '팬레터' },
];

function isActive(item: NavItem): boolean {
  if (item.to === '/') return route.path === '/';
  return route.path === item.to || route.path.startsWith(item.to + '/');
}

const tierLabel: Record<string, string> = {
  seed: '새싹',
  pepper: '고추',
  corn: '옥수수',
  owner: '발주인',
};

onMounted(async () => {
  try {
    me.value = await fetchMe();
  } catch (e) {
    if (isHttpStatus(e, 401)) {
      me.value = null;
    } else {
      me.value = null;
    }
  }
});

async function onLogout() {
  try {
    await logout();
  } catch { /* graceful */ }
  me.value = null;
  menuOpen.value = false;
  if (route.path !== '/') router.push('/');
}
</script>

<template>
  <header class="h-16 px-6 flex items-center justify-between border-b border-border bg-paper">
    <nav class="flex items-center gap-2">
      <RouterLink
        v-for="item in nav"
        :key="item.to"
        :to="item.to"
        class="px-3 py-1.5 rounded-full text-sm transition-colors"
        :class="
          isActive(item)
            ? 'bg-violet text-ink font-semibold'
            : 'text-ink-muted hover:text-ink hover:bg-elevated'
        "
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <div class="relative">
      <RouterLink
        v-if="!me"
        to="/login"
        class="px-4 py-1.5 rounded-lg bg-naver text-white text-sm font-semibold hover:opacity-90 transition-opacity"
      >
        네이버 로그인
      </RouterLink>

      <template v-else>
        <button
          class="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-elevated transition-colors"
          @click="menuOpen = !menuOpen"
        >
          <span class="text-xs px-1.5 py-0.5 rounded bg-violet-deep/40 text-ink-muted">
            {{ tierLabel[me.tier] ?? me.tier }}
          </span>
          <span class="text-sm font-semibold">{{ me.nickname }}</span>
          <span class="text-ink-muted text-xs">▼</span>
        </button>

        <div
          v-if="menuOpen"
          class="absolute right-0 top-full mt-1 w-40 rounded-lg bg-elevated border border-border py-1 z-10"
        >
          <RouterLink
            to="/me"
            class="block px-4 py-2 text-sm text-ink hover:bg-surface"
            @click="menuOpen = false"
          >
            마이페이지
          </RouterLink>
          <button
            class="block w-full text-left px-4 py-2 text-sm text-ink-muted hover:bg-surface"
            @click="onLogout"
          >
            로그아웃
          </button>
        </div>
      </template>
    </div>
  </header>
</template>
