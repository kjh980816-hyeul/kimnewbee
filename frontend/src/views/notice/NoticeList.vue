<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchNotices } from '@/api/notice';
import type { NoticeListItem } from '@/types/notice';

const notices = ref<NoticeListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetchNotices();
    notices.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '공지사항을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6">
      <h1 class="text-2xl font-bold text-pepper">공지사항</h1>
      <p class="mt-1 text-ink-muted text-sm">밭주인의 공식 공지</p>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul v-else class="divide-y divide-border rounded-md bg-surface">
      <li
        v-for="notice in notices"
        :key="notice.id"
        class="hover:bg-elevated transition-colors"
      >
        <RouterLink
          :to="{ name: 'notice-detail', params: { id: notice.id } }"
          class="flex items-center justify-between gap-4 p-4"
        >
          <div class="min-w-0 flex-1">
            <div class="text-ink truncate">{{ notice.title }}</div>
            <div class="mt-1 text-xs text-ink-muted">
              {{ notice.author }} · {{ new Date(notice.createdAt).toLocaleDateString('ko-KR') }}
            </div>
          </div>
          <div class="text-xs text-ink-muted whitespace-nowrap">조회 {{ notice.viewCount }}</div>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>
