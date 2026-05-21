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
import type { CurrentUser } from '@/types/user';
import type { NotificationItem, NotificationType } from '@/types/notification';

const route = useRoute();
const router = useRouter();
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
  <header class="h-16 px-6 flex items-center border-b border-border bg-surface">
    <nav class="flex items-center gap-1 shrink-0">
      <RouterLink
        v-for="item in nav"
        :key="item.to"
        :to="item.to"
        class="px-3 py-1.5 rounded-full text-sm transition-colors"
        :class="
          isActive(item)
            ? 'bg-violet-deep/40 text-ink font-semibold'
            : 'text-ink-muted hover:text-ink hover:bg-elevated'
        "
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <form
      class="flex-1 ml-2.5"
      @submit="onSearch"
    >
      <div class="relative">
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted text-sm">🔍</span>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="고추밭에서 검색..."
          class="w-full pl-9 pr-3 py-2 rounded-full bg-paper border border-border text-sm text-ink placeholder:text-ink-muted/60 focus:outline-none focus:border-violet/40"
        />
      </div>
    </form>

    <div class="relative shrink-0 ml-3">
      <button
        type="button"
        class="w-9 h-9 flex items-center justify-center rounded-full text-ink-muted hover:text-ink hover:bg-elevated transition-colors relative"
        aria-label="알림"
        @click="toggleNotif"
      >
        🔔
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

    <div class="relative shrink-0 ml-3">
      <RouterLink
        v-if="!me"
        to="/login"
        class="px-4 py-1.5 rounded-lg bg-naver text-white text-sm font-semibold hover:opacity-90 transition-opacity"
      >
        네이버 로그인
      </RouterLink>

      <template v-else>
        <button
          class="flex items-center gap-2 pl-1 pr-3 py-1 rounded-full hover:bg-elevated transition-colors"
          @click="menuOpen = !menuOpen; notifOpen = false"
        >
          <span
            v-if="me.profileImage"
            class="w-8 h-8 rounded-full overflow-hidden bg-violet/30 shrink-0"
          >
            <img :src="me.profileImage" :alt="me.nickname" class="w-full h-full object-cover" />
          </span>
          <span
            v-else
            class="w-8 h-8 rounded-full bg-violet/30 flex items-center justify-center text-sm font-bold text-ink shrink-0"
          >
            {{ meInitial }}
          </span>
          <span class="flex flex-col items-start leading-tight">
            <span class="text-sm font-semibold text-ink">{{ me.nickname }}</span>
            <span v-if="meTier" class="text-[10px] text-ink-muted">
              {{ meTier.emoji }}{{ meTier.label }} · Lv{{ meTier.level }}
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
