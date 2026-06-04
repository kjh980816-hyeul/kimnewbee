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

function relativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return '방금';
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  const d = Math.floor(h / 24);
  if (d < 7) return `${d}일 전`;
  const w = Math.floor(d / 7);
  if (w < 5) return `${w}주 전`;
  const dt = new Date(iso);
  return `${String(dt.getFullYear()).slice(2)}.${String(dt.getMonth() + 1).padStart(2, '0')}.${String(dt.getDate()).padStart(2, '0')}`;
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">공지사항</span>
    </nav>

    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight" style="font-family: var(--font-serif)">공지사항</h1>
      <p class="mt-2 text-sm text-ink-muted">밭주인의 공식 공지를 확인하세요 📢</p>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else>
      <div class="glass overflow-hidden">
        <div class="grid grid-cols-[70px_1fr_120px] gap-3 px-5 py-3 text-[11px] text-ink-muted border-b border-border tracking-wide">
          <span>분류</span>
          <span>제목</span>
          <span class="text-right">작성시간</span>
        </div>
        <ul v-if="notices.length > 0" class="divide-y divide-border">
          <li v-for="n in notices" :key="n.id" class="hover:bg-elevated transition-colors">
            <RouterLink
              :to="{ name: 'notice-detail', params: { id: n.id } }"
              class="grid grid-cols-[70px_1fr_120px] gap-3 px-5 py-3 items-center text-sm"
            >
              <span class="tag corn justify-center">📌 공지</span>
              <span class="text-ink truncate">{{ n.title }}</span>
              <span class="text-xs text-ink-muted text-right whitespace-nowrap">{{ relativeTime(n.createdAt) }}</span>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="px-5 py-10 text-sm text-ink-muted text-center">아직 공지가 없어요</p>
      </div>
    </template>
  </div>
</template>
