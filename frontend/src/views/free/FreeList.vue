<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchFreePosts } from '@/api/free';
import type { FreePostListItem } from '@/types/free';

const posts = ref<FreePostListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

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
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">자유게시판</h1>
        <p class="mt-1 text-ink-muted text-sm">초록고추들끼리 편하게 떠드는 공간</p>
      </div>
      <RouterLink
        :to="{ name: 'free-write' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        글쓰기
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ul v-else class="divide-y divide-border rounded-md bg-surface">
      <li
        v-for="post in posts"
        :key="post.id"
        class="hover:bg-elevated transition-colors"
      >
        <RouterLink
          :to="{ name: 'free-detail', params: { id: post.id } }"
          class="flex items-center justify-between gap-4 p-4"
        >
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
              <span class="text-ink truncate">{{ post.title }}</span>
              <span v-if="post.commentCount > 0" class="text-xs text-corn">
                [{{ post.commentCount }}]
              </span>
            </div>
            <div class="mt-1 text-xs text-ink-muted">
              {{ post.author }} · {{ new Date(post.createdAt).toLocaleDateString('ko-KR') }}
            </div>
          </div>
          <div class="text-xs text-ink-muted whitespace-nowrap flex gap-2">
            <span>♥ {{ post.likeCount }}</span>
            <span>조회 {{ post.viewCount }}</span>
          </div>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>
