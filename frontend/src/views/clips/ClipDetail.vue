<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchClip, toggleClipLike } from '@/api/clip';
import type { Clip } from '@/types/clip';
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
      <article class="rounded-md bg-surface overflow-hidden">
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

        <div class="p-6">
          <h1 class="text-xl font-bold text-ink">{{ clip.title }}</h1>
          <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
            <span>{{ clip.author }}</span>
            <span>·</span>
            <span>{{ new Date(clip.createdAt).toLocaleDateString('ko-KR') }}</span>
            <span>·</span>
            <span>조회 {{ clip.viewCount }}</span>
          </div>
          <p v-if="clip.description" class="mt-4 whitespace-pre-wrap text-ink leading-relaxed">
            {{ clip.description }}
          </p>
          <div class="mt-4 flex gap-2">
            <button
              type="button"
              class="rounded-md border border-border px-3 py-1.5 text-sm transition-colors"
              :class="clip.likedByMe ? 'bg-cheek text-paper border-cheek' : 'text-ink-muted hover:text-cheek hover:border-cheek'"
              @click="onLike"
            >
              ♥ {{ clip.likeCount }}
            </button>
          </div>
        </div>
      </article>

      <CommentSection :post-id="clip.id" class="mt-6" />
    </template>
  </main>
</template>
