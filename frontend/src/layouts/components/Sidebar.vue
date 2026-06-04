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
  <aside class="sidebar">
    <div class="side-card">
      <svg class="harmonica" width="70" height="35" viewBox="0 0 40 20" fill="none">
        <rect x="2" y="4" width="36" height="12" rx="2" fill="#5FC76B" opacity=".5" stroke="#3C8C3B"></rect>
        <rect x="5" y="7" width="30" height="6" rx="1" fill="#0F0F10"></rect>
      </svg>
      <div style="font-size: 12px; color: var(--corn); font-weight: 700">우리아기 불고노는</div>
      <div style="font-size: 17px; font-weight: 700; font-family: var(--font-serif)">하모니카는 🌽</div>
      <a
        href="https://youtube.com/shorts/5X9lrMI6FFw?si=fzhHuJDtZQM79--d"
        target="_blank"
        rel="noopener noreferrer"
        style="display: block; font-size: 11px; color: var(--text-mute); margin-top: 8px"
      >
        오늘의 애교송 ▸ 재생
      </a>
    </div>

    <div class="side-label">게시판</div>
    <RouterLink to="/" class="side-link" :class="{ on: isActive('/') }">
      <span class="ic">🏡</span><span class="lbl">홈</span>
    </RouterLink>
    <RouterLink
      v-for="item in boardMenu"
      :key="item.to"
      :to="item.to"
      class="side-link"
      :class="{ on: isActive(item.to) }"
    >
      <span class="ic">{{ item.icon }}</span>
      <span class="lbl">{{ item.label }}</span>
      <span v-if="item.count" class="ct">{{ item.count.toLocaleString() }}</span>
    </RouterLink>

    <div class="side-label">MY</div>
    <RouterLink
      v-for="item in myMenu"
      :key="item.to"
      :to="item.to"
      class="side-link"
      :class="{ on: isActive(item.to) }"
    >
      <span class="ic">{{ item.icon }}</span>
      <span class="lbl">{{ item.label }}</span>
      <span
        v-if="item.badgeKey === 'streak' && streak !== null && streak > 0"
        class="ct streak"
      >
        {{ streak }}일
      </span>
    </RouterLink>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 248px;
  flex-shrink: 0;
  padding: 26px 16px 40px;
  align-self: flex-start;
  position: sticky;
  top: 70px;
  max-height: calc(100vh - 70px);
  overflow-y: auto;
}
.side-card {
  background: linear-gradient(150deg, rgba(95, 199, 107, 0.12), rgba(28, 28, 31, 0.6));
  border: 1px solid rgba(95, 199, 107, 0.18);
  border-radius: var(--r-lg);
  padding: 16px;
  margin-bottom: 18px;
  position: relative;
  overflow: hidden;
}
.side-card .harmonica {
  position: absolute;
  right: -6px;
  bottom: -6px;
  opacity: 0.5;
}
.side-label {
  font-size: 11px;
  color: var(--text-mute);
  font-weight: 700;
  letter-spacing: 0.1em;
  padding: 6px 10px;
  margin: 14px 0 4px;
}
.side-link {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 10px 12px;
  border-radius: var(--r-md);
  color: var(--text-soft);
  font-size: 14px;
  font-weight: 500;
  transition: 0.18s var(--ease);
  margin: 1px 0;
  position: relative;
}
.side-link:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--text);
}
.side-link.on {
  background: linear-gradient(90deg, rgba(95, 199, 107, 0.16), transparent);
  color: var(--green-bright);
  font-weight: 700;
}
.side-link.on::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  border-radius: 2px;
  background: var(--green);
}
.side-link .ic {
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}
.side-link .lbl {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.side-link .ct {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-faint);
  font-variant-numeric: tabular-nums;
}
.side-link .ct.streak {
  background: rgba(95, 199, 107, 0.16);
  color: var(--green-bright);
  padding: 2px 7px;
  border-radius: 5px;
  font-weight: 700;
}

@media (max-width: 980px) {
  .sidebar {
    display: none;
  }
}
</style>
