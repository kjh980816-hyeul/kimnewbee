<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
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

const tierLabel: Record<Tier, string> = {
  seed: '🌱 새싹',
  pepper: '🌶️ 고추',
  corn: '🌽 옥수수',
  owner: '👑 밭주인',
};

const tierBadgeClass = computed(() => {
  if (!user.value) return '';
  return {
    seed: 'bg-elevated text-ink-muted',
    pepper: 'bg-pepper text-paper',
    corn: 'bg-corn text-paper',
    owner: 'bg-cheek text-paper',
  }[user.value.tier];
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
  <main class="min-h-screen bg-paper text-ink p-8">
    <h1 class="text-2xl font-bold text-pepper mb-6">마이페이지</h1>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="notLoggedIn"
      class="rounded-md bg-surface p-8 text-center"
    >
      <p class="text-ink mb-4">로그인이 필요해요</p>
      <RouterLink
        :to="{ name: 'login' }"
        class="inline-block rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        로그인하러 가기
      </RouterLink>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="user">
      <section class="rounded-md bg-surface p-6 mb-4">
        <div class="flex items-center gap-4">
          <div class="w-16 h-16 rounded-full bg-elevated grid place-items-center text-2xl shrink-0">
            {{ user.nickname[0] }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2 flex-wrap">
              <h2 class="text-lg font-bold text-ink">{{ user.nickname }}</h2>
              <span :class="['rounded-full px-2 py-0.5 text-xs', tierBadgeClass]">
                {{ tierLabel[user.tier] }}
              </span>
            </div>
            <p class="mt-1 text-xs text-ink-muted">
              가입일 {{ new Date(user.createdAt).toLocaleDateString('ko-KR') }}
            </p>
          </div>
          <button
            type="button"
            class="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:text-cheek hover:border-cheek transition-colors shrink-0"
            @click="onLogout"
          >
            로그아웃
          </button>
        </div>
      </section>

      <section class="rounded-md bg-surface p-6 mb-4">
        <h3 class="text-sm font-medium text-ink-muted mb-3">포인트</h3>
        <p class="text-3xl font-bold text-corn">{{ user.points.toLocaleString('ko-KR') }} P</p>
      </section>

      <section v-if="stats" class="rounded-md bg-surface p-6">
        <h3 class="text-sm font-medium text-ink-muted mb-3">활동</h3>
        <dl class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="rounded-md bg-elevated p-4 text-center">
            <dt class="text-xs text-ink-muted">작성한 글</dt>
            <dd class="mt-1 text-xl font-bold text-ink">{{ stats.postCount }}</dd>
          </div>
          <div class="rounded-md bg-elevated p-4 text-center">
            <dt class="text-xs text-ink-muted">남긴 댓글</dt>
            <dd class="mt-1 text-xl font-bold text-ink">{{ stats.commentCount }}</dd>
          </div>
          <div class="rounded-md bg-elevated p-4 text-center">
            <dt class="text-xs text-ink-muted">좋아요한 글</dt>
            <dd class="mt-1 text-xl font-bold text-ink">{{ stats.likeGivenCount }}</dd>
          </div>
          <div class="rounded-md bg-elevated p-4 text-center">
            <dt class="text-xs text-ink-muted">출석 연속</dt>
            <dd class="mt-1 text-xl font-bold text-pepper">{{ stats.attendanceStreak }}일</dd>
          </div>
        </dl>
      </section>
    </template>
  </main>
</template>
