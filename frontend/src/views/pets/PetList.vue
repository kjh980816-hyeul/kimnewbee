<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchPets } from '@/api/pet';
import type { PetListItem } from '@/types/pet';

const items = ref<PetListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetchPets();
    items.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '사진을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">반려동물 사진</h1>
        <p class="mt-1 text-ink-muted text-sm">댕댕이 냥냥이 자랑타임</p>
      </div>
      <RouterLink
        :to="{ name: 'pet-write' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        사진 올리기
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
        <RouterLink :to="{ name: 'pet-detail', params: { id: item.id } }">
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
