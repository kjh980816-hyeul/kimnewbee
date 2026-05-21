<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchAdminDashboard } from '@/api/admin';
import { fetchAdminBoards } from '@/api/board';
import type { AdminDashboard } from '@/types/admin';
import type { Board } from '@/types/board';

const dashboard = ref<AdminDashboard | null>(null);
const boards = ref<Board[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const activeBoardCount = computed(() => boards.value.filter(b => b.active).length);
const inactiveBoardCount = computed(() => boards.value.length - activeBoardCount.value);

onMounted(async () => {
  try {
    const [d, b] = await Promise.all([
      fetchAdminDashboard().catch(() => null),
      fetchAdminBoards().catch(() => ({ data: [] as Board[], total: 0 })),
    ]);
    dashboard.value = d;
    boards.value = b.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '통계를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="p-8">
    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight">대시보드 📊</h1>
      <p class="mt-2 text-sm text-ink-muted">고추밭의 오늘 한눈에 보기</p>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="rounded-2xl bg-surface border border-border p-5">
          <div class="text-xs text-ink-muted">전체 회원</div>
          <div class="mt-2 text-3xl font-extrabold text-ink tabular-nums">
            {{ dashboard?.totalUsers.toLocaleString('ko-KR') ?? '—' }}
          </div>
          <div class="mt-1 text-[11px] text-pepper">활동 중</div>
        </div>
        <div class="rounded-2xl bg-surface border border-border p-5">
          <div class="text-xs text-ink-muted">전체 게시글</div>
          <div class="mt-2 text-3xl font-extrabold text-violet tabular-nums">
            {{ dashboard?.totalPosts.toLocaleString('ko-KR') ?? '—' }}
          </div>
          <div class="mt-1 text-[11px] text-ink-muted">댓글 {{ dashboard?.totalComments.toLocaleString('ko-KR') ?? '—' }}건</div>
        </div>
        <div class="rounded-2xl bg-surface border border-border p-5">
          <div class="text-xs text-ink-muted">받은 ♥</div>
          <div class="mt-2 text-3xl font-extrabold text-cheek tabular-nums">
            {{ dashboard?.totalLikes.toLocaleString('ko-KR') ?? '—' }}
          </div>
          <div class="mt-1 text-[11px] text-cheek">누적</div>
        </div>
        <div class="rounded-2xl bg-surface border border-border p-5">
          <div class="text-xs text-ink-muted">활성 게시판</div>
          <div class="mt-2 text-3xl font-extrabold text-corn tabular-nums">
            {{ activeBoardCount }}
          </div>
          <div class="mt-1 text-[11px] text-ink-muted">{{ inactiveBoardCount }}개 비공개</div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div class="rounded-2xl bg-surface border border-border p-5">
          <div class="flex items-center gap-2 mb-4">
            <span class="text-base">📋</span>
            <h2 class="text-sm font-bold text-ink">게시판별 글 수</h2>
            <span class="text-xs text-ink-muted">활성 게시판</span>
          </div>
          <ul v-if="boards.length > 0" class="space-y-2.5">
            <li
              v-for="b in boards.filter(x => x.active)"
              :key="b.id"
              class="flex items-center gap-3"
            >
              <span class="text-xs text-ink w-24 truncate shrink-0">{{ b.name }}</span>
              <div class="flex-1 h-2 rounded-full bg-elevated overflow-hidden">
                <div
                  class="h-full bg-violet/60"
                  :style="{ width: Math.min(100, (b.orderIndex + 1) * 12) + '%' }"
                ></div>
              </div>
              <span class="text-xs text-ink-muted tabular-nums w-12 text-right shrink-0">—</span>
            </li>
          </ul>
          <p v-else class="text-sm text-ink-muted py-6 text-center">게시판 데이터가 없어요</p>
          <p class="mt-3 text-[10px] text-ink-muted/60 text-center">
            * 일자별 카운트 API는 추후 추가 예정
          </p>
        </div>

        <div class="rounded-2xl bg-surface border border-border p-5">
          <div class="flex items-center gap-2 mb-4">
            <span class="text-base">🚨</span>
            <h2 class="text-sm font-bold text-ink">최근 신고 / 승인 대기</h2>
          </div>
          <p class="text-sm text-ink-muted py-12 text-center">
            신고/승인 시스템은 준비 중이에요
          </p>
        </div>
      </div>
    </template>
  </div>
</template>
