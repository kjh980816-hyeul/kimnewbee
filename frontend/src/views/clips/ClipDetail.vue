<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchClip, toggleClipLike } from '@/api/clip';
import type { Clip } from '@/types/clip';
import PostArticle from '@/components/post/PostArticle.vue';
import CommentSection from '@/components/post/CommentSection.vue';

const route = useRoute();
const router = useRouter();

const clip = ref<Clip | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const clipId = computed(() => Number(route.params['id']));

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
    <template v-else-if="clip">
      <PostArticle
        :title="clip.title"
        :author="clip.author"
        :created-at="clip.createdAt"
        :view-count="clip.viewCount"
        :content="clip.description"
        :liked-by-me="clip.likedByMe"
        :like-count="clip.likeCount"
        @like="onLike"
      >
        <template #media>
          <iframe
            v-if="embedUrl"
            :src="embedUrl"
            class="w-full aspect-video bg-elevated"
            allowfullscreen
            referrerpolicy="strict-origin-when-cross-origin"
          />
          <a
            v-else
            :href="clip.videoUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="block aspect-video bg-elevated grid place-items-center text-pepper hover:underline"
          >
            원본 영상 보러가기 ↗
          </a>
        </template>
      </PostArticle>
      <CommentSection :post-id="clip.id" class="mt-6" />
    </template>
  </main>
</template>
