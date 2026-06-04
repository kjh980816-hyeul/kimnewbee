<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { fetchNotice } from '@/api/notice';
import type { Notice } from '@/types/notice';

const route = useRoute();

const notice = ref<Notice | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  const id = Number(route.params['id']);
  if (Number.isNaN(id)) {
    error.value = '잘못된 공지 번호예요';
    loading.value = false;
    return;
  }

  try {
    notice.value = await fetchNotice(id);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '공지를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

function formatDate(iso: string): string {
  const d = new Date(iso);
  const yy = String(d.getFullYear()).slice(2);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mi = String(d.getMinutes()).padStart(2, '0');
  return `20${yy}.${mm}.${dd} · ${hh}:${mi}`;
}
</script>

<template>
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'notices' }" class="hover:text-ink">공지사항</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">공지</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <article v-else-if="notice" class="glass card-pad">
      <div class="mb-4">
        <span class="tag corn" style="font-size: 12px; padding: 5px 12px">🌶 공지</span>
      </div>
      <h1 class="text-3xl font-extrabold text-ink leading-tight mb-5" style="font-family: var(--font-serif)">{{ notice.title }}</h1>
      <div class="flex items-center gap-3 pb-5 mb-6" style="border-bottom: 1px solid var(--line)">
        <div class="w-11 h-11 rounded-full bg-gradient-to-br from-corn to-cheek flex items-center justify-center text-lg shrink-0">
          👑
        </div>
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <span class="text-sm font-semibold text-ink">{{ notice.author }}</span>
            <span class="text-[10px] px-1.5 py-0.5 rounded bg-cheek/20 text-cheek font-semibold">발주인</span>
          </div>
          <div class="mt-0.5 text-xs text-ink-muted">
            {{ formatDate(notice.createdAt) }} · 조회 {{ notice.viewCount }}
          </div>
        </div>
      </div>
      <div class="whitespace-pre-wrap text-ink leading-relaxed">{{ notice.content }}</div>
    </article>
  </div>
</template>
