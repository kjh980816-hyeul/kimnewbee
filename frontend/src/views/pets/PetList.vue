<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchPets } from '@/api/pet';
import type { PetListItem } from '@/types/pet';

const items = ref<PetListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const categories = ['전체', '강아지', '고양이', '햄찌', '기타'];
const activeCategory = ref('전체');

const NEW_DAYS = 3;

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

function isNew(iso: string): boolean {
  return Date.now() - new Date(iso).getTime() < NEW_DAYS * 86400000;
}
</script>

<template>
  <div class="p-8 max-w-6xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">반려동물 사진</span>
    </nav>

    <header class="mb-6 flex items-end justify-between gap-4">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">반려동물 사진</h1>
        <p class="mt-2 text-sm text-ink-muted">우리집 댕댕이 냥냥이 자랑하는 시간 🐾</p>
      </div>
      <RouterLink
        :to="{ name: 'pet-write' }"
        class="rounded-lg bg-pepper px-4 py-2 text-sm font-semibold text-paper hover:bg-pepper-deep transition-colors whitespace-nowrap"
      >
        ✏ 사진 올리기
      </RouterLink>
    </header>

    <div class="flex items-center gap-2 mb-6 flex-wrap">
      <button
        v-for="cat in categories"
        :key="cat"
        class="px-4 py-1.5 rounded-full text-sm transition-colors"
        :class="
          activeCategory === cat
            ? 'bg-violet text-ink font-semibold'
            : 'bg-elevated text-ink-muted hover:text-ink'
        "
        @click="activeCategory = cat"
      >
        {{ cat }}
      </button>
    </div>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul
      v-else-if="items.length > 0"
      class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5"
    >
      <li v-for="item in items" :key="item.id">
        <RouterLink
          :to="{ name: 'pet-detail', params: { id: item.id } }"
          class="block rounded-2xl bg-elevated border border-border overflow-hidden hover:border-violet transition-colors group"
        >
          <div class="relative aspect-square overflow-hidden bg-gradient-to-br from-cheek/30 to-violet/30">
            <img
              :src="item.thumbnailUrl"
              :alt="item.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              loading="lazy"
            />
            <span
              v-if="isNew(item.createdAt)"
              class="absolute left-3 top-3 px-2 py-0.5 rounded bg-cheek text-white text-[10px] font-bold tracking-wider"
            >
              NEW
            </span>
            <div class="absolute left-3 right-3 bottom-3 px-3 py-1.5 rounded-lg bg-paper/70 backdrop-blur-sm">
              <div class="text-sm font-semibold text-ink truncate">{{ item.title }}</div>
            </div>
          </div>
          <div class="px-4 py-3 flex items-center justify-between text-xs text-ink-muted">
            <span class="truncate">{{ item.author }}</span>
            <span class="flex gap-2 whitespace-nowrap ml-2">
              <span class="text-cheek">♥ {{ item.likeCount }}</span>
              <span v-if="item.commentCount > 0" class="text-corn">[{{ item.commentCount }}]</span>
            </span>
          </div>
        </RouterLink>
      </li>
    </ul>
    <p v-else class="py-10 text-center text-sm text-ink-muted">아직 사진이 없어요. 댕댕이/냥냥이 사진 한 장 올려주세요!</p>
  </div>
</template>
