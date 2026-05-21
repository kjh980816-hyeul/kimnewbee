<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { fetchMe, fetchMyStats } from '@/api/me';
import { logout } from '@/api/auth';
import { isHttpStatus } from '@/api/error';
import type { CurrentUser, UserStats } from '@/types/user';
import type { Tier } from '@/types/offline';

const router = useRouter();

const user = ref<CurrentUser | null>(null);
const stats = ref<UserStats | null>(null);
const loading = ref(true);
const notLoggedIn = ref(false);
const error = ref<string | null>(null);

const activeTab = ref<'posts' | 'comments' | 'liked' | 'scraps'>('posts');

const tierMeta: Record<Tier, { label: string; emoji: string }> = {
  seed: { label: '새싹', emoji: '🌱' },
  pepper: { label: '고추', emoji: '🌶' },
  corn: { label: '옥수수', emoji: '🌽' },
  owner: { label: '밭주인', emoji: '👑' },
};

const TIER_THRESHOLDS: Record<Tier, { floor: number; next: number | null }> = {
  seed: { floor: 0, next: 100 },
  pepper: { floor: 100, next: 500 },
  corn: { floor: 500, next: null },
  owner: { floor: 0, next: null },
};

const tierOrder: Tier[] = ['seed', 'pepper', 'corn', 'owner'];

const progress = computed(() => {
  if (!user.value) return { pct: 0, current: 0, needed: 0, nextTier: null as Tier | null };
  const t = TIER_THRESHOLDS[user.value.tier];
  if (t.next === null) {
    return { pct: 100, current: user.value.points - t.floor, needed: 0, nextTier: null };
  }
  const span = t.next - t.floor;
  const current = Math.max(0, user.value.points - t.floor);
  const pct = Math.min(100, Math.round((current / span) * 100));
  const idx = tierOrder.indexOf(user.value.tier);
  return { pct, current, needed: span, nextTier: tierOrder[idx + 1] ?? null };
});

const joinedLabel = computed(() => {
  if (!user.value) return '';
  const d = new Date(user.value.createdAt);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
});

onMounted(async () => {
  try {
    const [me, myStats] = await Promise.all([fetchMe(), fetchMyStats()]);
    user.value = me;
    stats.value = myStats;
  } catch (e) {
    if (isHttpStatus(e, 401)) {
      notLoggedIn.value = true;
    } else {
      error.value = e instanceof Error ? e.message : '내 정보를 불러올 수 없어요';
    }
  } finally {
    loading.value = false;
  }
});

async function onLogout(): Promise<void> {
  await logout();
  await router.push({ name: 'home' });
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">내 프로필</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="notLoggedIn"
      class="rounded-2xl bg-surface border border-border p-12 text-center"
    >
      <p class="text-ink mb-4">로그인이 필요해요</p>
      <RouterLink
        :to="{ name: 'login' }"
        class="inline-block rounded-full bg-violet-deep px-6 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors"
      >
        로그인하러 가기
      </RouterLink>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="user">
      <section class="relative rounded-2xl bg-gradient-to-br from-violet-deep to-violet/30 p-6 overflow-hidden">
        <div class="flex items-center gap-5">
          <div class="w-24 h-24 rounded-full bg-violet/40 border-4 border-violet/30 grid place-items-center text-4xl shrink-0 overflow-hidden">
            <img v-if="user.profileImage" :src="user.profileImage" :alt="user.nickname" class="w-full h-full object-cover" />
            <span v-else>{{ user.nickname[0] }}</span>
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2 flex-wrap">
              <h2 class="text-2xl font-extrabold text-ink">{{ user.nickname }}</h2>
              <span class="px-2 py-0.5 rounded-full text-xs bg-paper/30 text-ink font-semibold">
                {{ tierMeta[user.tier].emoji }} {{ tierMeta[user.tier].label }}
              </span>
            </div>
            <p class="mt-1 text-xs text-ink-muted">
              고추밭 가입일 · {{ joinedLabel }}
              <span v-if="stats && stats.attendanceStreak > 0" class="ml-2">· {{ stats.attendanceStreak }}일 연속 출석중 ⭐</span>
            </p>
            <div v-if="stats" class="mt-3 flex items-center gap-5 text-sm">
              <div><span class="font-bold text-ink">{{ stats.postCount }}</span> <span class="text-ink-muted text-xs">글</span></div>
              <div><span class="font-bold text-ink">{{ stats.commentCount }}</span> <span class="text-ink-muted text-xs">댓글</span></div>
              <div><span class="font-bold text-ink">{{ stats.likeGivenCount }}</span> <span class="text-ink-muted text-xs">받은 ♥</span></div>
            </div>
          </div>
          <button
            type="button"
            class="rounded-full border border-ink/30 px-4 py-1.5 text-sm text-ink hover:bg-paper/20 transition-colors shrink-0"
          >
            프로필 수정
          </button>
        </div>
      </section>

      <section class="mt-4 rounded-2xl bg-surface border border-border p-5">
        <div class="flex items-center justify-between mb-2 text-xs">
          <div class="flex items-center gap-2">
            <span class="font-semibold text-ink">
              {{ tierMeta[user.tier].emoji }}{{ tierMeta[user.tier].label }}
            </span>
            <template v-if="progress.nextTier">
              <span class="text-ink-muted">→</span>
              <span class="font-semibold text-ink">
                {{ tierMeta[progress.nextTier].emoji }}{{ tierMeta[progress.nextTier].label }}
              </span>
              <span class="text-ink-muted">활동점수 {{ progress.current }} / {{ progress.needed }} 필요</span>
            </template>
            <span v-else class="text-ink-muted">최고 등급 달성!</span>
          </div>
          <span class="font-bold text-violet">{{ progress.pct }}%</span>
        </div>
        <div class="relative h-2.5 rounded-full bg-elevated overflow-hidden">
          <div
            class="h-full bg-gradient-to-r from-pepper via-violet to-corn transition-all"
            :style="{ width: progress.pct + '%' }"
          ></div>
        </div>
        <div class="mt-2 flex justify-between text-[10px] text-ink-muted">
          <span>🌱 새싹</span>
          <span>🌶 고추</span>
          <span>🌽 옥수수</span>
          <span>👑 발주인</span>
        </div>
      </section>

      <section class="mt-4">
        <div class="flex items-center gap-1 border-b border-border">
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'posts' ? 'border-violet text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="activeTab = 'posts'"
          >
            내가 쓴 글 <span class="text-ink-muted ml-0.5 text-xs">{{ stats?.postCount ?? 0 }}</span>
          </button>
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'comments' ? 'border-violet text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="activeTab = 'comments'"
          >
            댓글 단 글 <span class="text-ink-muted ml-0.5 text-xs">{{ stats?.commentCount ?? 0 }}</span>
          </button>
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'liked' ? 'border-violet text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="activeTab = 'liked'"
          >
            좋아요한 글 <span class="text-ink-muted ml-0.5 text-xs">{{ stats?.likeGivenCount ?? 0 }}</span>
          </button>
          <button
            type="button"
            class="px-4 py-2.5 text-sm border-b-2 -mb-px transition-colors"
            :class="activeTab === 'scraps' ? 'border-violet text-ink font-semibold' : 'border-transparent text-ink-muted hover:text-ink'"
            @click="activeTab = 'scraps'"
          >
            스크랩 <span class="text-ink-muted ml-0.5 text-xs">0</span>
          </button>
        </div>
        <div class="rounded-b-2xl bg-surface border-x border-b border-border px-5 py-10 text-center text-sm text-ink-muted">
          탭별 글 리스트는 백엔드 endpoint 추가 후 표시됩니다.
        </div>
      </section>

      <div class="mt-6 text-right">
        <button
          type="button"
          class="text-xs text-ink-muted hover:text-cheek transition-colors"
          @click="onLogout"
        >
          로그아웃
        </button>
      </div>
    </template>
  </div>
</template>
