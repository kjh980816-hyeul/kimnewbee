<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { fetchLetter } from '@/api/letter';
import type { Letter } from '@/types/letter';

const route = useRoute();
const router = useRouter();

const letter = ref<Letter | null>(null);
const loading = ref(true);
const accessDenied = ref(false);
const error = ref<string | null>(null);

const letterId = computed(() => Number(route.params['id']));

onMounted(async () => {
  if (Number.isNaN(letterId.value)) {
    error.value = '잘못된 편지 번호예요';
    loading.value = false;
    return;
  }
  try {
    letter.value = await fetchLetter(letterId.value);
  } catch (e) {
    if (axios.isAxiosError(e) && e.response?.status === 403) {
      accessDenied.value = true;
    } else if (axios.isAxiosError(e) && e.response?.status === 404) {
      error.value = '편지를 찾을 수 없어요';
    } else {
      error.value = e instanceof Error ? e.message : '편지를 불러올 수 없어요';
    }
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
      ← 편지함으로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="accessDenied"
      class="rounded-md bg-surface p-8 text-center"
    >
      <p class="text-2xl">🔒</p>
      <p class="mt-2 text-ink">관리자 전용 편지입니다</p>
      <p class="mt-1 text-sm text-ink-muted">
        팬레터 본문은 밭주인(관리자)만 열람할 수 있어요
      </p>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <article v-else-if="letter" class="rounded-md bg-surface p-6">
      <header class="mb-4 pb-4 border-b border-border">
        <div class="flex items-center gap-2">
          <h1 class="text-xl font-bold text-ink">{{ letter.author }}</h1>
          <span
            v-if="letter.isReadByAdmin"
            class="rounded-full bg-elevated px-2 py-0.5 text-xs text-ink-muted"
          >
            읽음
          </span>
          <span
            v-else
            class="rounded-full bg-pepper px-2 py-0.5 text-xs text-paper"
          >
            새 편지
          </span>
        </div>
        <div class="mt-2 text-xs text-ink-muted">
          {{ new Date(letter.createdAt).toLocaleDateString('ko-KR') }}
        </div>
      </header>
      <div class="whitespace-pre-wrap text-ink leading-relaxed">{{ letter.content }}</div>
    </article>
  </main>
</template>
