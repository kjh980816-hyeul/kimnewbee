<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFreePost, toggleFreePostLike } from '@/api/free';
import type { FreePost } from '@/types/free';
import PostArticle from '@/components/post/PostArticle.vue';
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
      <PostArticle
        :title="post.title"
        :author="post.author"
        :created-at="post.createdAt"
        :view-count="post.viewCount"
        :content="post.content"
        :liked-by-me="post.likedByMe"
        :like-count="post.likeCount"
        @like="onLike"
      />
      <CommentSection :post-id="post.id" class="mt-6" />
    </template>
  </main>
</template>
