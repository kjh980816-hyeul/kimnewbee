<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchFanartList } from '@/api/fanart';
import type { FanartListItem } from '@/types/fanart';

const items = ref<FanartListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetchFanartList();
    items.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '팬아트를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">팬아트 갤러리</h1>
        <p class="mt-1 text-ink-muted text-sm">늉비를 그림으로 남겨주세요</p>
      </div>
      <RouterLink
        :to="{ name: 'fanart-write' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        그림 올리기
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4"
    >
      <li
        v-for="item in items"
        :key="item.id"
        class="rounded-md bg-surface overflow-hidden hover:bg-elevated transition-colors"
      >
        <RouterLink :to="{ name: 'fanart-detail', params: { id: item.id } }">
          <div class="aspect-square overflow-hidden bg-elevated">
            <img
              :src="item.thumbnailUrl"
              :alt="item.title"
              class="w-full h-full object-cover"
              loading="lazy"
            />
          </div>
          <div class="p-3">
            <div class="text-sm text-ink truncate">{{ item.title }}</div>
            <div class="mt-1 flex items-center justify-between text-xs text-ink-muted">
              <span class="truncate">{{ item.author }}</span>
              <span class="flex gap-2 whitespace-nowrap ml-2">
                <span>♥ {{ item.likeCount }}</span>
                <span v-if="item.commentCount > 0" class="text-corn">[{{ item.commentCount }}]</span>
              </span>
            </div>
          </div>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>
