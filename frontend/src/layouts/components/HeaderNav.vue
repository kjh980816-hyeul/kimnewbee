<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { fetchMe } from '@/api/me';
import { logout } from '@/api/auth';
import { isHttpStatus } from '@/api/error';
import {
  fetchNotifications,
  fetchUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
} from '@/api/notification';
import { useSidebarDrawer } from '@/composables/useSidebarDrawer';
import type { CurrentUser } from '@/types/user';
import type { NotificationItem, NotificationType } from '@/types/notification';

const route = useRoute();
const router = useRouter();
const { toggle: toggleDrawer } = useSidebarDrawer();
const me = ref<CurrentUser | null>(null);
const menuOpen = ref(false);
const searchKeyword = ref('');

const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);
const notifOpen = ref(false);
const notifLoading = ref(false);
let unreadPoller: ReturnType<typeof setInterval> | null = null;

interface NavItem {
  to: string;
  label: string;
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

const tierMeta: Record<string, { label: string; emoji: string; level: number }> = {
  seed: { label: '새싹', emoji: '🌱', level: 1 },
  pepper: { label: '고추', emoji: '🌶', level: 2 },
  corn: { label: '옥수수', emoji: '🌽', level: 3 },
  owner: { label: '발주인', emoji: '👑', level: 99 },
};

const NOTIF_ICON: Record<NotificationType, string> = {
  COMMENT: '💬',
  REPLY: '↪',
  LIKE: '♥',
  NOTICE: '📢',
  SYSTEM: '⚙',
};

const meTier = computed(() => (me.value ? tierMeta[me.value.tier] : null));
const meInitial = computed(() => (me.value?.nickname ? me.value.nickname.charAt(0) : '?'));
const hasUnread = computed(() => unreadCount.value > 0);

onMounted(async () => {
  try {
    me.value = await fetchMe();
  } catch (e) {
    if (!isHttpStatus(e, 401)) console.warn('me fetch failed', e);
    me.value = null;
  }
  if (me.value) {
    refreshUnreadCount();
    unreadPoller = setInterval(refreshUnreadCount, 60_000);
  }
});

onUnmounted(() => {
  if (unreadPoller) clearInterval(unreadPoller);
});

async function refreshUnreadCount(): Promise<void> {
  try {
    const r = await fetchUnreadCount();
    unreadCount.value = r.unreadCount;
  } catch { /* graceful */ }
}

async function toggleNotif(): Promise<void> {
  if (notifOpen.value) {
    notifOpen.value = false;
    return;
  }
  notifOpen.value = true;
  menuOpen.value = false;
  if (notifications.value.length === 0) {
    notifLoading.value = true;
    try {
      const r = await fetchNotifications();
      notifications.value = r.data;
    } finally {
      notifLoading.value = false;
    }
  }
}

async function onNotifClick(n: NotificationItem): Promise<void> {
  if (!n.read) {
    try {
      await markNotificationRead(n.id);
      n.read = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    } catch { /* graceful */ }
  }
  notifOpen.value = false;
  if (n.link) router.push(n.link);
}

async function onMarkAllRead(): Promise<void> {
  try {
    await markAllNotificationsRead();
    notifications.value.forEach(n => (n.read = true));
    unreadCount.value = 0;
  } catch { /* graceful */ }
}

function relativeNotifTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return '방금';
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  return `${Math.floor(h / 24)}일 전`;
}

async function onLogout() {
  try {
    await logout();
  } catch { /* graceful */ }
  me.value = null;
  notifications.value = [];
  unreadCount.value = 0;
  menuOpen.value = false;
  if (route.path !== '/') router.push('/');
}

function onSearch(e: Event) {
  e.preventDefault();
  const q = searchKeyword.value.trim();
  if (!q) return;
  router.push({ path: '/search', query: { q } });
}
</script>

<template>
  <header class="header">
    <button type="button" class="hamburger" aria-label="메뉴" @click="toggleDrawer">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M4 6h16M4 12h16M4 18h16" stroke-linecap="round"></path>
      </svg>
    </button>
    <RouterLink to="/" class="brand">
      <svg class="logo-pep" viewBox="0 0 40 40" fill="none">
        <path d="M14 8 Q22 6 24 10 Q22 11 22 13" stroke="#A3EC8E" stroke-width="2" stroke-linecap="round"></path>
        <path d="M14 12 Q10 20 16 30 Q24 34 28 24 Q30 14 22 12 Q18 11 14 12 Z" fill="#5FC76B" stroke="#3C8C3B" stroke-width="1.3"></path>
        <path d="M17 16 Q15 22 19 28" stroke="#CFF3BE" stroke-width="1.5" stroke-linecap="round"></path>
      </svg>
      <b>고추<span class="accent">밭</span></b>
    </RouterLink>

    <nav class="nav">
      <RouterLink
        v-for="item in nav"
        :key="item.to"
        :to="item.to"
        :class="{ on: isActive(item) }"
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <div class="spacer"></div>

    <form class="search" @submit="onSearch">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
        <path d="M11 4a7 7 0 1 1 0 14 7 7 0 0 1 0-14zm6 13 4 4"></path>
      </svg>
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="고추밭에서 검색…"
      />
    </form>

    <div class="relative shrink-0">
      <button
        type="button"
        class="icon-btn relative"
        aria-label="알림"
        @click="toggleNotif"
      >
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M6 8a6 6 0 1 1 12 0v5l2 3H4l2-3zM10 19a2 2 0 0 0 4 0"></path>
        </svg>
        <span
          v-if="me && hasUnread"
          class="absolute top-1 right-1 min-w-[16px] h-4 px-1 rounded-full bg-cheek text-paper text-[9px] font-bold flex items-center justify-center"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </button>

      <div
        v-if="notifOpen"
        class="absolute right-0 top-full mt-1 w-80 rounded-2xl bg-surface border border-border shadow-2xl shadow-black/30 z-20 overflow-hidden"
      >
        <header class="px-4 py-3 border-b border-border flex items-center justify-between">
          <h3 class="text-sm font-bold text-ink">알림</h3>
          <button
            v-if="!me"
            type="button"
            class="text-[11px] text-ink-muted"
            disabled
          >로그인 필요</button>
          <button
            v-else-if="hasUnread"
            type="button"
            class="text-[11px] text-violet hover:underline"
            @click="onMarkAllRead"
          >
            모두 읽음
          </button>
        </header>
        <div class="max-h-96 overflow-y-auto">
          <p v-if="!me" class="px-4 py-8 text-center text-xs text-ink-muted">
            로그인하면 알림을 받을 수 있어요
          </p>
          <p v-else-if="notifLoading" class="px-4 py-8 text-center text-xs text-ink-muted">불러오는 중...</p>
          <p v-else-if="notifications.length === 0" class="px-4 py-8 text-center text-xs text-ink-muted">
            새 알림이 없어요
          </p>
          <ul v-else class="divide-y divide-border">
            <li v-for="n in notifications" :key="n.id">
              <button
                type="button"
                class="block w-full text-left px-4 py-3 hover:bg-elevated transition-colors"
                :class="{ 'bg-violet/5': !n.read }"
                @click="onNotifClick(n)"
              >
                <div class="flex items-start gap-2">
                  <span class="text-base shrink-0 mt-0.5">{{ NOTIF_ICON[n.type] }}</span>
                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-1">
                      <span class="text-xs font-semibold text-ink truncate">{{ n.title }}</span>
                      <span v-if="!n.read" class="w-1.5 h-1.5 rounded-full bg-cheek shrink-0"></span>
                    </div>
                    <p class="text-[11px] text-ink-muted truncate mt-0.5">{{ n.message }}</p>
                    <span class="text-[10px] text-ink-muted/70 mt-1 block">{{ relativeNotifTime(n.createdAt) }}</span>
                  </div>
                </div>
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div class="relative shrink-0">
      <RouterLink v-if="!me" to="/login" class="btn-naver">
        <span class="n">N</span>
        <span>네이버 로그인</span>
      </RouterLink>

      <template v-else>
        <button
          type="button"
          class="me"
          @click="menuOpen = !menuOpen; notifOpen = false"
        >
          <span v-if="me.profileImage" class="avatar overflow-hidden" style="width: 34px; height: 34px">
            <img :src="me.profileImage" :alt="me.nickname" class="w-full h-full object-cover" />
          </span>
          <span v-else class="avatar" style="width: 34px; height: 34px; font-size: 15px">
            {{ meInitial }}
          </span>
          <span class="flex flex-col items-start leading-tight">
            <span class="text-[13px] font-bold text-ink">{{ me.nickname }}</span>
            <span v-if="meTier" class="text-[10px] font-semibold" style="color: var(--green-bright)">
              {{ meTier.emoji }}{{ meTier.label }} · Lv.{{ meTier.level }}
            </span>
          </span>
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

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 50;
  height: 70px;
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 0 34px;
  background: rgba(15, 15, 16, 0.74);
  backdrop-filter: blur(18px) saturate(1.4);
  border-bottom: 1px solid var(--line-soft);
}
.brand {
  display: flex;
  align-items: center;
  gap: 11px;
}
.brand .logo-pep {
  width: 30px;
  height: 30px;
  filter: drop-shadow(0 2px 6px var(--green-glow));
}
.brand b {
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.01em;
  white-space: nowrap;
}
.brand b .accent {
  color: var(--green);
}
.nav {
  display: flex;
  gap: 2px;
  margin-left: 4px;
}
.nav a {
  padding: 9px 15px;
  border-radius: var(--r-pill);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-soft);
  transition: 0.2s var(--ease);
}
.nav a:hover {
  color: var(--text);
  background: rgba(255, 255, 255, 0.04);
}
.nav a.on {
  color: var(--green-bright);
  background: rgba(95, 199, 107, 0.1);
  font-weight: 700;
}
.spacer {
  flex: 1;
}
.search {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 250px;
  padding: 9px 15px;
  border-radius: var(--r-pill);
  background: var(--plum-750);
  border: 1px solid var(--line-soft);
  color: var(--text-mute);
  font-size: 13px;
  transition: 0.2s;
}
.search:hover,
.search:focus-within {
  border-color: var(--green-mid);
  box-shadow: 0 0 0 3px rgba(95, 199, 107, 0.07);
}
.search input {
  flex: 1;
  min-width: 0;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text);
  font-size: 13px;
}
.search input::placeholder {
  color: var(--text-mute);
}
.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}
.icon-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text);
}
.me {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 12px 5px 5px;
  border-radius: var(--r-pill);
  border: none;
  background: transparent;
  transition: 0.2s;
}
.me:hover {
  background: rgba(255, 255, 255, 0.04);
}
.avatar {
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-hand);
  font-weight: 700;
  color: #16240f;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--green-bright), var(--green-mid));
}
.btn-naver {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: none;
  border-radius: var(--r-pill);
  background: var(--naver);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  transition: 0.2s;
}
.btn-naver:hover {
  filter: brightness(1.06);
  transform: translateY(-1px);
}
.btn-naver .n {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  background: #fff;
  color: var(--naver);
  font-family: var(--font-hand);
  font-size: 16px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hamburger {
  display: none;
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  color: var(--text-soft);
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}
.hamburger:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text);
}

@media (max-width: 980px) {
  .nav {
    display: none;
  }
  .search {
    width: 150px;
  }
  .header {
    gap: 12px;
    padding: 0 14px;
  }
  .hamburger {
    display: flex;
  }
}
</style>
