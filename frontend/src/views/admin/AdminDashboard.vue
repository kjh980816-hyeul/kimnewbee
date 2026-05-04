<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchAdminDashboard } from '@/api/admin';
import type { AdminDashboard } from '@/types/admin';

const dashboard = ref<AdminDashboard | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    dashboard.value = await fetchAdminDashboard();
  } catch (e) {
    error.value = e instanceof Error ? e.message : '통계를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-ink mb-4">대시보드</h2>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>

    <dl
      v-else-if="dashboard"
      class="grid grid-cols-2 md:grid-cols-4 gap-4"
    >
      <div class="rounded-md bg-surface p-5">
        <dt class="text-xs text-ink-muted">총 회원</dt>
        <dd class="mt-1 text-3xl font-bold text-pepper">
          {{ dashboard.totalUsers.toLocaleString('ko-KR') }}
        </dd>
      </div>
      <div class="rounded-md bg-surface p-5">
        <dt class="text-xs text-ink-muted">총 게시글</dt>
        <dd class="mt-1 text-3xl font-bold text-corn">
          {{ dashboard.totalPosts.toLocaleString('ko-KR') }}
        </dd>
      </div>
      <div class="rounded-md bg-surface p-5">
        <dt class="text-xs text-ink-muted">총 댓글</dt>
        <dd class="mt-1 text-3xl font-bold text-ink">
          {{ dashboard.totalComments.toLocaleString('ko-KR') }}
        </dd>
      </div>
      <div class="rounded-md bg-surface p-5">
        <dt class="text-xs text-ink-muted">총 좋아요</dt>
        <dd class="mt-1 text-3xl font-bold text-cheek">
          {{ dashboard.totalLikes.toLocaleString('ko-KR') }}
        </dd>
      </div>
    </dl>
  </div>
</template>
