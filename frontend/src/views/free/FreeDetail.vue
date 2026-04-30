<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFreePost, toggleFreePostLike } from '@/api/free';
import type { FreePost } from '@/types/free';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();
const router = useRouter();

const post = ref<FreePost | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const postId = computed(() => Number(route.params['id']));

onMounted(async () => {
  if (Number.isNaN(postId.value)) {
    error.value = '잘못된 게시글 번호예요';
    loading.value = false;
    return;
  }
  try {
    post.value = await fetchFreePost(postId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시글을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!post.value) return;
  const res = await toggleFreePostLike(post.value.id);
  post.value.likedByMe = res.liked;
  post.value.likeCount = res.likeCount;
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 목록으로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="post">
      <article class="rounded-md bg-surface p-6">
        <header class="mb-4 pb-4 border-b border-border">
          <h1 class="text-xl font-bold text-ink">{{ post.title }}</h1>
          <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
            <span>{{ post.author }}</span>
            <span>·</span>
            <span>{{ new Date(post.createdAt).toLocaleDateString('ko-KR') }}</span>
            <span>·</span>
            <span>조회 {{ post.viewCount }}</span>
          </div>
        </header>
        <div class="whitespace-pre-wrap text-ink leading-relaxed">{{ post.content }}</div>

        <div class="mt-6 flex gap-2">
          <button
            type="button"
            class="rounded-md border border-border px-3 py-1.5 text-sm transition-colors"
            :class="post.likedByMe ? 'bg-cheek text-paper border-cheek' : 'text-ink-muted hover:text-cheek hover:border-cheek'"
            @click="onLike"
          >
            ♥ {{ post.likeCount }}
          </button>
        </div>
      </article>

      <CommentSection :post-id="post.id" class="mt-6" />
    </template>
  </main>
</template>
