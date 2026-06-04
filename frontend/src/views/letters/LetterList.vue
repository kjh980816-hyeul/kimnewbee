<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchLetters, fetchAdminViewerInfo } from '@/api/letter';
import type { LetterListItem } from '@/types/letter';

const letters = ref<LetterListItem[]>([]);
const isAdmin = ref(false);
const loading = ref(true);
const error = ref<string | null>(null);

const CARD_PALETTES = [
  { bg: 'bg-corn/15', border: 'border-corn/30', text: 'text-corn' },
  { bg: 'bg-pepper/15', border: 'border-pepper/30', text: 'text-pepper' },
  { bg: 'bg-cheek/15', border: 'border-cheek/30', text: 'text-cheek' },
  { bg: 'bg-violet/15', border: 'border-violet/30', text: 'text-violet' },
];

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

function relativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return '방금 전';
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  const d = Math.floor(h / 24);
  return `${d}일 전`;
}

function palette(id: number) {
  return CARD_PALETTES[id % CARD_PALETTES.length]!;
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">팬레터</span>
    </nav>

    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight font-serif">팬레터 💌</h1>
        <p class="mt-2 text-sm text-ink-muted">늉비에게 따뜻한 마음을 전해보세요</p>
      </div>
      <RouterLink :to="{ name: 'letter-write' }" class="btn-primary whitespace-nowrap">✏ 편지 쓰기</RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <p v-else-if="letters.length === 0" class="text-sm text-ink-muted py-12 text-center">
      아직 편지가 없어요. 첫 편지를 남겨주세요!
    </p>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <RouterLink
        v-for="letter in letters"
        :key="letter.id"
        :to="{ name: 'letter-detail', params: { id: letter.id } }"
        class="block rounded-2xl border p-5 hover:scale-[1.02] hover:shadow-xl transition-all min-h-[200px] flex flex-col"
        :class="[palette(letter.id).bg, palette(letter.id).border]"
      >
        <div class="flex items-center justify-between mb-2">
          <span class="text-[11px] font-semibold text-ink">TO. 늉비에게</span>
          <span v-if="!isAdmin" class="text-[10px] text-ink-muted">🔒</span>
        </div>
        <p class="text-sm text-ink/90 line-clamp-4 leading-relaxed flex-1">{{ letter.preview }}</p>
        <div class="mt-3 pt-3 border-t border-dashed border-ink/15 flex items-center justify-between text-[11px]">
          <span class="text-ink-muted">FROM. <span :class="palette(letter.id).text">{{ letter.author }}</span></span>
          <span class="text-ink-muted">{{ relativeTime(letter.createdAt) }}</span>
        </div>
      </RouterLink>
    </div>
  </div>
</template>
