<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchClips } from '@/api/clip';
import type { ClipListItem } from '@/types/clip';

const items = ref<ClipListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetchClips();
    items.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '영상을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">영상/클립</h1>
        <p class="mt-1 text-ink-muted text-sm">치지직/유튜브 영상 모음</p>
      </div>
      <RouterLink
        :to="{ name: 'clip-write' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        영상 등록
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <li
        v-for="item in items"
        :key="item.id"
        class="rounded-md bg-surface overflow-hidden hover:bg-elevated transition-colors"
      >
        <RouterLink
          :to="{ name: 'clip-detail', params: { id: item.id } }"
          class="block p-4"
        >
          <div class="flex items-center justify-between gap-2 mb-2">
            <span
              class="rounded-full px-2 py-0.5 text-xs"
              :class="{
                'bg-chzzk text-paper': item.source === 'chzzk',
                'bg-corn text-paper': item.source === 'youtube',
                'bg-elevated text-ink-muted': item.source === 'other',
              }"
            >
              {{ item.source.toUpperCase() }}
            </span>
            <span class="text-xs text-ink-muted">
              {{ new Date(item.createdAt).toLocaleDateString('ko-KR') }}
            </span>
          </div>
          <div class="text-sm text-ink truncate">{{ item.title }}</div>
          <div class="mt-1 flex items-center justify-between text-xs text-ink-muted">
            <span class="truncate">{{ item.author }}</span>
            <span class="flex gap-2 whitespace-nowrap ml-2">
              <span>♥ {{ item.likeCount }}</span>
              <span v-if="item.commentCount > 0" class="text-corn">[{{ item.commentCount }}]</span>
            </span>
          </div>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>
