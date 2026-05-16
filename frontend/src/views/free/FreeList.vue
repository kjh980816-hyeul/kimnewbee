<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchFreePosts } from '@/api/free';
import type { FreePostListItem } from '@/types/free';

const posts = ref<FreePostListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const categories = ['전체', '공지', '잡담', '질문', '후기', '정보'];
const activeCategory = ref('전체');

const HOT_THRESHOLD = 10;

onMounted(async () => {
  try {
    const res = await fetchFreePosts();
    posts.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

function formatDate(iso: string): string {
  const d = new Date(iso);
  const yy = String(d.getFullYear()).slice(2);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yy}.${mm}.${dd}`;
}
</script>

<template>
  <div class="p-8 max-w-5xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">자유게시판</span>
    </nav>

    <header class="mb-6 flex items-end justify-between gap-4">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">자유게시판</h1>
        <p class="mt-2 text-sm text-ink-muted">초록고추들끼리 편하게 떠드는 공간이에요 💬</p>
      </div>
      <RouterLink
        :to="{ name: 'free-write' }"
        class="rounded-lg bg-pepper px-4 py-2 text-sm font-semibold text-paper hover:bg-pepper-deep transition-colors whitespace-nowrap"
      >
        ✏ 글쓰기
      </RouterLink>
    </header>

    <div class="flex items-center gap-2 mb-5 flex-wrap">
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
    <template v-else>
      <div class="rounded-2xl bg-elevated border border-border overflow-hidden">
        <div class="grid grid-cols-[80px_1fr_100px] gap-4 px-5 py-3 text-xs text-ink-muted border-b border-border">
          <span>분류</span>
          <span>제목</span>
          <span class="text-right">날짜</span>
        </div>
        <ul v-if="posts.length > 0" class="divide-y divide-border">
          <li v-for="p in posts" :key="p.id" class="hover:bg-surface transition-colors">
            <RouterLink
              :to="{ name: 'free-detail', params: { id: p.id } }"
              class="grid grid-cols-[80px_1fr_100px] gap-4 px-5 py-3 items-center"
            >
              <span class="px-2 py-1 rounded text-[11px] font-semibold bg-violet-deep/30 text-ink-muted text-center">
                💬 잡담
              </span>
              <div class="min-w-0 flex items-center gap-2">
                <span class="text-ink truncate">{{ p.title }}</span>
                <span v-if="p.commentCount > 0" class="text-xs text-corn shrink-0">
                  [{{ p.commentCount }}]
                </span>
                <span
                  v-if="p.likeCount >= HOT_THRESHOLD"
                  class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-500 text-white shrink-0"
                >
                  HOT
                </span>
              </div>
              <span class="text-xs text-ink-muted text-right whitespace-nowrap">{{ formatDate(p.createdAt) }}</span>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="px-5 py-10 text-sm text-ink-muted text-center">아직 글이 없어요. 첫 글을 남겨보세요!</p>
      </div>
    </template>
  </div>
</template>
