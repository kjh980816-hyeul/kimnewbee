<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchNotice } from '@/api/notice';
import type { Notice } from '@/types/notice';

const route = useRoute();
const router = useRouter();

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
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 목록으로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <article v-else-if="notice" class="rounded-md bg-surface p-6">
      <header class="mb-4 pb-4 border-b border-border">
        <h1 class="text-xl font-bold text-ink">{{ notice.title }}</h1>
        <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
          <span>{{ notice.author }}</span>
          <span>·</span>
          <span>{{ new Date(notice.createdAt).toLocaleDateString('ko-KR') }}</span>
          <span>·</span>
          <span>조회 {{ notice.viewCount }}</span>
        </div>
      </header>
      <div class="whitespace-pre-wrap text-ink leading-relaxed">{{ notice.content }}</div>
    </article>
  </main>
</template>
