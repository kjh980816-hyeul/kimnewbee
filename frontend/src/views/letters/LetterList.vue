<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchLetters, fetchAdminViewerInfo } from '@/api/letter';
import type { LetterListItem } from '@/types/letter';

const letters = ref<LetterListItem[]>([]);
const isAdmin = ref(false);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const [listRes, adminInfo] = await Promise.all([
      fetchLetters(),
      fetchAdminViewerInfo(),
    ]);
    letters.value = listRes.data;
    isAdmin.value = adminInfo.isAdmin;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '편지함을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">팬레터함</h1>
        <p class="mt-1 text-ink-muted text-sm">
          밭주인(늉비)에게 보내는 편지 — 본문은 관리자만 열람할 수 있어요
        </p>
      </div>
      <RouterLink
        :to="{ name: 'letter-write' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        편지 쓰기
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul v-else class="divide-y divide-border rounded-md bg-surface">
      <li
        v-for="letter in letters"
        :key="letter.id"
        class="hover:bg-elevated transition-colors"
      >
        <RouterLink
          :to="{ name: 'letter-detail', params: { id: letter.id } }"
          class="flex items-center justify-between gap-4 p-4"
        >
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
              <span class="text-sm text-ink">{{ letter.author }}</span>
              <span v-if="!isAdmin" class="text-xs text-corn">🔒 관리자 전용</span>
            </div>
            <p class="mt-1 text-xs text-ink-muted truncate">{{ letter.preview }}</p>
          </div>
          <div class="text-xs text-ink-muted whitespace-nowrap">
            {{ new Date(letter.createdAt).toLocaleDateString('ko-KR') }}
          </div>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>
