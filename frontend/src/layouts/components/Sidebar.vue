<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { fetchAttendanceStatus } from '@/api/attendance';
import { useBoards, boardLink, boardIcon, tierRank } from '@/composables/useBoards';
import { useCurrentUser } from '@/composables/useCurrentUser';

const route = useRoute();
const streak = ref<number | null>(null);

interface MenuItem {
  to: string;
  label: string;
  icon: string;
  count?: number;
}

// API가 비거나 로딩 전이면 보여줄 정적 폴백(기본 게시판).
const FALLBACK_MENU: MenuItem[] = [
  { to: '/notices', label: '공지사항', icon: '★' },
  { to: '/free', label: '자유게시판', icon: '💬' },
  { to: '/fanart', label: '팬아트 갤러리', icon: '🎨' },
  { to: '/pets', label: '반려동물 사진', icon: '🐾' },
  { to: '/songs', label: '노래추천', icon: '🎵' },
  { to: '/letters', label: '팬레터', icon: '✉' },
  { to: '/clips', label: '클립 공유', icon: '🎬' },
];

const { boards } = useBoards();
const { currentUser } = useCurrentUser();

// 읽기 권한(readTier)이 내 등급 이하인 게시판만 노출. 비로그인은 새싹 취급.
const boardMenu = computed<MenuItem[]>(() => {
  if (boards.value.length === 0) return FALLBACK_MENU;
  const myRank = tierRank(currentUser.value?.tier);
  return boards.value
    .filter((b) => b.active && tierRank(b.readTier) <= myRank)
    .map((b) => ({ to: boardLink(b), label: b.name, icon: boardIcon(b) }));
});

const myMenu: { to: string; label: string; icon: string; badgeKey?: 'streak' }[] = [
  { to: '/me', label: '내 활동', icon: '👤' },
  { to: '/favorites', label: '즐겨찾기', icon: '☆' },
  { to: '/checkin', label: '출석체크', icon: '✓', badgeKey: 'streak' },
];

function isActive(to: string): boolean {
  if (to === '/') return route.path === '/';
  return route.path === to || route.path.startsWith(to + '/');
}

onMounted(async () => {
  try {
    const status = await fetchAttendanceStatus();
    streak.value = status.currentStreak;
  } catch {
    streak.value = null;
  }
});
</script>

<template>
  <aside class="w-60 shrink-0 bg-surface border-r border-border flex flex-col">
    <div class="px-6 pt-6 pb-4">
      <RouterLink to="/" class="flex items-center gap-2">
        <span class="text-2xl">🌶</span>
        <h1 class="text-xl font-extrabold tracking-tight text-ink leading-none">
          김늉비<span class="text-pepper">고추밭</span>
        </h1>
      </RouterLink>
    </div>

    <div class="mx-4 mb-5 p-3 rounded-2xl bg-corn/10 border border-corn/30">
      <div class="flex items-start gap-2">
        <div class="text-base leading-tight shrink-0">🟡</div>
        <div class="min-w-0 flex-1">
          <div class="text-xs text-corn font-semibold leading-snug">우리아기 불고노는</div>
          <div class="text-xs text-ink-muted leading-snug"> 하모니카는 🍃</div>
        </div>
      </div>
      <div class="mt-2 text-[11px] text-ink-muted truncate flex items-center gap-1">
        <span class="text-violet">▶</span>
        <a href="https://youtube.com/shorts/5X9lrMI6FFw?si=fzhHuJDtZQM79--d" target="_blank" rel="noopener noreferrer"> 오늘의 애교송 재생</a>
      </div>
    </div>

    <div class="px-6 pb-2 text-[10px] tracking-[0.15em] text-ink-muted/70 font-semibold">MENU</div>
    <nav class="px-3">
      <ul class="space-y-0.5">
        <li>
          <RouterLink
            to="/"
            class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
            :class="
              isActive('/')
                ? 'bg-violet-deep/40 text-ink font-semibold'
                : 'text-ink-muted hover:text-ink hover:bg-elevated'
            "
          >
            <span class="w-5 text-center shrink-0">⌂</span>
            <span class="flex-1 truncate">홈</span>
          </RouterLink>
        </li>
        <li v-for="item in boardMenu" :key="item.to">
          <RouterLink
            :to="item.to"
            class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
            :class="
              isActive(item.to)
                ? 'bg-violet-deep/40 text-ink font-semibold'
                : 'text-ink-muted hover:text-ink hover:bg-elevated'
            "
          >
            <span class="w-5 text-center shrink-0">{{ item.icon }}</span>
            <span class="flex-1 truncate">{{ item.label }}</span>
            <span v-if="item.count" class="text-[11px] text-ink-muted/60 tabular-nums">
              {{ item.count.toLocaleString() }}
            </span>
          </RouterLink>
        </li>
      </ul>
    </nav>

    <div class="px-6 pt-5 pb-2 text-[10px] tracking-[0.15em] text-ink-muted/70 font-semibold">MY</div>
    <nav class="px-3 pb-6">
      <ul class="space-y-0.5">
        <li v-for="item in myMenu" :key="item.to">
          <RouterLink
            :to="item.to"
            class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
            :class="
              isActive(item.to)
                ? 'bg-violet-deep/40 text-ink font-semibold'
                : 'text-ink-muted hover:text-ink hover:bg-elevated'
            "
          >
            <span class="w-5 text-center shrink-0">{{ item.icon }}</span>
            <span class="flex-1 truncate">{{ item.label }}</span>
            <span
              v-if="item.badgeKey === 'streak' && streak !== null && streak > 0"
              class="px-1.5 py-0.5 rounded text-[10px] bg-violet/25 text-violet font-bold tabular-nums"
            >
              {{ streak }}일
            </span>
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>
