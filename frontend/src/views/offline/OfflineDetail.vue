<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchOfflineReview, toggleOfflineReviewLike } from '@/api/offline';
import { isHttpStatus } from '@/api/error';
import type { OfflineReview } from '@/types/offline';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();
const router = useRouter();

const review = ref<OfflineReview | null>(null);
const loading = ref(true);
const accessDenied = ref(false);
const error = ref<string | null>(null);

const reviewId = computed(() => Number(route.params['id']));

onMounted(async () => {
  if (Number.isNaN(reviewId.value)) {
    error.value = '잘못된 후기 번호예요';
    loading.value = false;
    return;
  }
  try {
    review.value = await fetchOfflineReview(reviewId.value);
  } catch (e) {
    if (isHttpStatus(e, 403)) {
      accessDenied.value = true;
    } else if (isHttpStatus(e, 404)) {
      error.value = '후기를 찾을 수 없어요';
    } else {
      error.value = e instanceof Error ? e.message : '후기를 불러올 수 없어요';
    }
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!review.value) return;
  const res = await toggleOfflineReviewLike(review.value.id);
  review.value.likedByMe = res.liked;
  review.value.likeCount = res.likeCount;
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 후기 목록으로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="accessDenied"
      class="rounded-md bg-surface p-8 text-center"
    >
      <p class="text-2xl">🌽</p>
      <p class="mt-2 text-ink">옥수수 등급 이상만 볼 수 있어요</p>
      <p class="mt-1 text-sm text-ink-muted">
        고정/후원 팬에게만 공개되는 게시판입니다
      </p>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="review">
      <div class="mb-2 text-xs text-ink-muted flex gap-3">
        <span>📍 {{ review.location }}</span>
        <span>📅 {{ review.meetupDate }}</span>
      </div>
      <PostArticle
        :title="review.title"
        :author="review.author"
        :created-at="review.createdAt"
        :view-count="review.viewCount"
        :content="review.content"
        :liked-by-me="review.likedByMe"
        :like-count="review.likeCount"
        @like="onLike"
      >
        <template #media>
          <img
            :src="review.imageUrl"
            :alt="review.title"
            class="w-full max-h-[60vh] object-cover bg-elevated"
          />
        </template>
      </PostArticle>
      <CommentSection :post-id="review.id" class="mt-6" />
    </template>
  </main>
</template>
