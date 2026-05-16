<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
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

function formatDate(iso: string): string {
  const d = new Date(iso);
  const yy = String(d.getFullYear()).slice(2);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yy}.${mm}.${dd}`;
}
</script>

<template>
  <div class="p-8 max-w-5xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">공지사항</span>
    </nav>

    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight">공지사항</h1>
      <p class="mt-2 text-sm text-ink-muted">밭주인의 공식 공지를 확인하세요 📢</p>
    </header>

    <div class="flex items-center gap-2 mb-5">
      <span class="px-4 py-1.5 rounded-full bg-violet text-ink text-sm font-semibold">전체</span>
    </div>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else>
      <div class="rounded-2xl bg-elevated border border-border overflow-hidden">
        <div class="grid grid-cols-[80px_1fr_100px] gap-4 px-5 py-3 text-xs text-ink-muted border-b border-border">
          <span>분류</span>
          <span>제목</span>
          <span class="text-right">날짜</span>
        </div>
        <ul v-if="notices.length > 0" class="divide-y divide-border">
          <li v-for="n in notices" :key="n.id" class="hover:bg-surface transition-colors">
            <RouterLink
              :to="{ name: 'notice-detail', params: { id: n.id } }"
              class="grid grid-cols-[80px_1fr_100px] gap-4 px-5 py-3 items-center"
            >
              <span class="px-2 py-1 rounded text-[11px] font-semibold bg-corn/20 text-corn text-center">
                🌶 공지
              </span>
              <span class="text-ink truncate">{{ n.title }}</span>
              <span class="text-xs text-ink-muted text-right whitespace-nowrap">{{ formatDate(n.createdAt) }}</span>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="px-5 py-10 text-sm text-ink-muted text-center">아직 공지가 없어요</p>
      </div>
    </template>
  </div>
</template>
