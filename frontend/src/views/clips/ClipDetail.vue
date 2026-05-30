<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { fetchClip, toggleClipLike } from '@/api/clip';
import { useIsOwner } from '@/composables/useIsOwner';
import type { Clip } from '@/types/clip';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();

const clip = ref<Clip | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const clipId = computed(() => Number(route.params['id']));
const isOwner = useIsOwner(computed(() => clip.value?.author ?? null));

const embedUrl = computed<string | null>(() => {
  if (!clip.value) return null;
  const url = clip.value.videoUrl;
  const youtubeMatch = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([\w-]+)/);
  if (youtubeMatch) return `https://www.youtube.com/embed/${youtubeMatch[1]}`;
  const chzzkMatch = url.match(/chzzk\.naver\.com\/clips\/([\w-]+)/);
  if (chzzkMatch) return `https://chzzk.naver.com/embed/clip/${chzzkMatch[1]}`;
  return null;
});

onMounted(async () => {
  if (Number.isNaN(clipId.value)) {
    error.value = '잘못된 영상 번호예요';
    loading.value = false;
    return;
  }
  try {
    clip.value = await fetchClip(clipId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '영상을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!clip.value) return;
  const res = await toggleClipLike(clip.value.id);
  clip.value.likedByMe = res.liked;
  clip.value.likeCount = res.likeCount;
}
</script>

<template>
  <div class="p-8 max-w-3xl">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶️ 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <RouterLink :to="{ name: 'clips' }" class="hover:text-ink">영상/클립</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">클립</span>
    </nav>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="clip">
      <div v-if="isOwner" class="mb-3 flex justify-end">
        <RouterLink
          :to="{ name: 'clip-edit', params: { id: clip.id } }"
          class="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:text-pepper hover:border-pepper transition-colors"
        >
          수정
        </RouterLink>
      </div>
      <PostArticle
        :title="clip.title"
        :author="clip.author"
        :author-avatar="clip.authorProfileImage"
        :created-at="clip.createdAt"
        :view-count="clip.viewCount"
        :content="clip.description"
        :liked-by-me="clip.likedByMe"
        :like-count="clip.likeCount"
        category="클립"
        @like="onLike"
      >
        <template #media>
          <iframe
            v-if="embedUrl"
            :src="embedUrl"
            class="w-full aspect-video bg-elevated rounded-xl mb-6"
            allowfullscreen
            referrerpolicy="strict-origin-when-cross-origin"
          />
          <a
            v-else
            :href="clip.videoUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="block aspect-video bg-elevated rounded-xl mb-6 grid place-items-center text-pepper hover:underline"
          >
            원본 영상 보러가기 ↗
          </a>
        </template>
      </PostArticle>
      <CommentSection :post-id="clip.id" class="mt-10" />
    </template>
  </div>
</template>
