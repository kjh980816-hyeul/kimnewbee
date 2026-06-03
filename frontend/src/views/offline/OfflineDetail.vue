<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { fetchOfflineReview, toggleOfflineReviewLike } from '@/api/offline';
import { deletePost } from '@/api/post';
import { isHttpStatus, errorMessage } from '@/api/error';
import { useIsOwner } from '@/composables/useIsOwner';
import { useCurrentUser } from '@/composables/useCurrentUser';
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
const isOwner = useIsOwner(computed(() => review.value?.author ?? null));
const { currentUser, isOwner: isAdmin } = useCurrentUser();
const canDelete = computed(
  () => isAdmin.value || (!!currentUser.value && currentUser.value.nickname === review.value?.author),
);

async function handleDelete(): Promise<void> {
  if (!review.value) return;
  if (!confirm('이 게시글을 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deletePost(review.value.id);
    await router.push({ name: 'offline' });
  } catch (e) {
    error.value = errorMessage(e, '삭제에 실패했어요');
  }
}

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
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'offline' }" class="hover:text-ink">오프후기</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">후기</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <div
      v-else-if="accessDenied"
      class="rounded-2xl bg-elevated border border-border p-8 text-center"
    >
      <p class="text-3xl">🌽</p>
      <p class="mt-3 text-ink">옥수수 등급 이상만 볼 수 있어요</p>
      <p class="mt-1 text-sm text-ink-muted">
        고정/후원 팬에게만 공개되는 게시판입니다
      </p>
    </div>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="review">
      <div v-if="isOwner" class="mb-3 flex justify-end">
        <RouterLink
          :to="{ name: 'offline-edit', params: { id: review.id } }"
          class="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:text-pepper hover:border-pepper transition-colors"
        >
          수정
        </RouterLink>
      </div>
      <div class="mb-4 text-xs text-ink-muted flex gap-3">
        <span>📍 {{ review.location }}</span>
        <span>📅 {{ review.meetupDate }}</span>
      </div>
      <PostArticle
        :title="review.title"
        :author="review.author"
        :author-avatar="review.authorProfileImage"
        :created-at="review.createdAt"
        :view-count="review.viewCount"
        :content="review.content"
        :liked-by-me="review.likedByMe"
        :like-count="review.likeCount"
        :can-delete="canDelete"
        category="후기"
        @like="onLike"
        @delete="handleDelete"
      >
        <template #media>
          <img
            :src="review.imageUrl"
            :alt="review.title"
            class="w-full max-h-[60vh] object-cover bg-elevated rounded-xl mb-6"
          />
        </template>
      </PostArticle>
      <CommentSection :post-id="review.id" class="mt-10" />
    </template>
  </div>
</template>
