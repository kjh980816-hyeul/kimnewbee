<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchAdminDashboard } from '@/api/admin';
import { fetchAdminBoards } from '@/api/board';
import type { AdminDashboard } from '@/types/admin';
import type { Board } from '@/types/board';

const dashboard = ref<AdminDashboard | null>(null);
const boards = ref<Board[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const [d, b] = await Promise.all([
      fetchAdminDashboard(),
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
  <div class="p-8 max-w-6xl">
    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight">대시보드 📊</h1>
      <p class="mt-2 text-sm text-ink-muted">고추밭의 오늘 한눈에 보기</p>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="dashboard">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="rounded-2xl bg-elevated border border-border p-5">
          <div class="text-xs text-ink-muted">전체 회원</div>
          <div class="mt-2 text-4xl font-extrabold text-violet">
            {{ dashboard.totalUsers.toLocaleString('ko-KR') }}
          </div>
          <div class="mt-1 text-xs text-pepper">활동 중인 초록고추들</div>
        </div>
        <div class="rounded-2xl bg-elevated border border-border p-5">
          <div class="text-xs text-ink-muted">전체 게시글</div>
          <div class="mt-2 text-4xl font-extrabold text-pepper">
            {{ dashboard.totalPosts.toLocaleString('ko-KR') }}
          </div>
          <div class="mt-1 text-xs text-ink-muted">댓글 {{ dashboard.totalComments.toLocaleString('ko-KR') }}건</div>
        </div>
        <div class="rounded-2xl bg-elevated border border-border p-5">
          <div class="text-xs text-ink-muted">받은 좋아요</div>
          <div class="mt-2 text-4xl font-extrabold text-cheek">
            {{ dashboard.totalLikes.toLocaleString('ko-KR') }}
          </div>
          <div class="mt-1 text-xs text-cheek">♥ 누적</div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div class="lg:col-span-2 rounded-2xl bg-elevated border border-border p-5">
          <div class="flex items-center gap-2 mb-4">
            <span class="text-base">📋</span>
            <h2 class="text-sm font-bold text-ink">활성 게시판</h2>
            <span class="text-xs text-ink-muted">{{ boards.filter(b => b.active).length }} / {{ boards.length }}</span>
          </div>
          <ul v-if="boards.length > 0" class="space-y-2">
            <li
              v-for="b in boards"
              :key="b.id"
              class="flex items-center justify-between px-3 py-2 rounded-lg bg-paper"
            >
              <div class="flex items-center gap-3 min-w-0">
                <span class="text-xs text-ink-muted w-5 text-center">{{ b.orderIndex + 1 }}</span>
                <span class="text-sm text-ink truncate">{{ b.name }}</span>
                <span class="text-[10px] px-1.5 py-0.5 rounded bg-violet-deep/30 text-violet">
                  {{ b.layout }}
                </span>
              </div>
              <span
                class="text-[10px] px-2 py-0.5 rounded font-semibold"
                :class="b.active ? 'bg-pepper/20 text-pepper' : 'bg-elevated text-ink-muted'"
              >
                {{ b.active ? 'ACTIVE' : 'OFF' }}
              </span>
            </li>
          </ul>
          <p v-else class="text-sm text-ink-muted py-6 text-center">게시판 데이터가 없어요</p>
        </div>

        <div class="rounded-2xl bg-elevated border border-border p-5">
          <div class="flex items-center gap-2 mb-4">
            <span class="text-base">🔔</span>
            <h2 class="text-sm font-bold text-ink">최근 활동</h2>
          </div>
          <p class="text-sm text-ink-muted py-6 text-center">
            신고/승인 시스템은 준비 중이에요
          </p>
        </div>
      </div>
    </template>
  </div>
</template>
