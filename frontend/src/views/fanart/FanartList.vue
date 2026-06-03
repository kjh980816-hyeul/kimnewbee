<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchFanartList } from '@/api/fanart';
import { deletePost } from '@/api/post';
import { useCurrentUser } from '@/composables/useCurrentUser';
import type { FanartListItem } from '@/types/fanart';

const items = ref<FanartListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const categories = ['전체', '일러스트', '낙서·스케치', '만화', '도트', '영상'];
const activeCategory = ref('전체');

const NEW_DAYS = 3;

const failedImageIds = ref<Set<number>>(new Set());
function markFailed(id: number): void {
  failedImageIds.value.add(id);
}

const { currentUser, isOwner: isAdmin } = useCurrentUser();
function canDeletePost(author: string): boolean {
  return isAdmin.value || currentUser.value?.nickname === author;
}
async function removePost(id: number): Promise<void> {
  if (!confirm('이 게시글을 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deletePost(id);
    items.value = items.value.filter((x) => x.id !== id);
  } catch {
    alert('삭제에 실패했어요');
  }
}

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

function isNew(iso: string): boolean {
  return Date.now() - new Date(iso).getTime() < NEW_DAYS * 86400000;
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">팬아트 갤러리</span>
    </nav>

    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">팬아트 갤러리 🎨</h1>
        <p class="mt-2 text-sm text-ink-muted">늉비의 귀여운 순간들을 그림으로 남겨주세요</p>
      </div>
      <RouterLink
        :to="{ name: 'fanart-write' }"
        class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors whitespace-nowrap"
      >
        ✏ 그림 올리기
      </RouterLink>
    </header>

    <div class="flex items-center gap-2 mb-6 flex-wrap">
      <button
        v-for="cat in categories"
        :key="cat"
        class="px-4 py-1.5 rounded-full text-sm transition-colors"
        :class="
          activeCategory === cat
            ? 'bg-violet-deep/40 text-ink font-semibold'
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
      class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4"
    >
      <li v-for="item in items" :key="item.id" class="relative">
        <button
          v-if="canDeletePost(item.author)"
          type="button"
          class="absolute right-2 top-2 z-10 rounded-md bg-paper/80 px-2 py-1 text-xs font-semibold text-cheek border border-cheek/40 hover:bg-cheek hover:text-paper transition-colors"
          @click.prevent.stop="removePost(item.id)"
        >
          🗑
        </button>
        <RouterLink
          :to="{ name: 'fanart-detail', params: { id: item.id } }"
          class="block rounded-2xl bg-surface border border-border overflow-hidden hover:border-violet/40 transition-colors group"
        >
          <div class="relative aspect-square overflow-hidden bg-gradient-to-br from-violet/40 to-corn/30">
            <img
              v-if="!failedImageIds.has(item.id)"
              :src="item.thumbnailUrl"
              :alt="item.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              loading="lazy"
              @error="markFailed(item.id)"
            />
            <div
              v-else
              class="w-full h-full flex flex-col items-center justify-center text-ink-muted gap-1"
            >
              <span class="text-4xl">🖼️</span>
              <span class="text-[10px]">이미지를 불러올 수 없어요</span>
            </div>
            <span
              v-if="isNew(item.createdAt)"
              class="absolute left-3 top-3 px-2 py-0.5 rounded bg-cheek text-paper text-[10px] font-bold tracking-wider"
            >
              NEW
            </span>
          </div>
          <div class="px-4 py-3">
            <div class="text-sm font-semibold text-ink truncate mb-1">{{ item.title }}</div>
            <div class="flex items-center justify-between text-xs text-ink-muted">
              <span class="truncate">{{ item.author }}</span>
              <span class="flex gap-2 whitespace-nowrap ml-2">
                <span class="text-cheek">♥ {{ item.likeCount }}</span>
                <span v-if="item.commentCount > 0" class="text-corn">💬 {{ item.commentCount }}</span>
              </span>
            </div>
          </div>
        </RouterLink>
      </li>
    </ul>
    <p v-else class="py-10 text-center text-sm text-ink-muted">아직 팬아트가 없어요. 첫 그림을 남겨주세요!</p>
  </div>
</template>
