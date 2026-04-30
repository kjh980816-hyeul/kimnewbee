<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createClip } from '@/api/clip';

const router = useRouter();

const title = ref('');
const videoUrl = ref('');
const description = ref('');
const submitting = ref(false);
const error = ref<string | null>(null);

const isUrlValid = computed(() => {
  if (!videoUrl.value) return false;
  try {
    const u = new URL(videoUrl.value);
    return u.protocol === 'https:' || u.protocol === 'http:';
  } catch {
    return false;
  }
});

async function onSubmit(): Promise<void> {
  if (!title.value.trim() || !videoUrl.value.trim()) {
    error.value = '제목과 영상 URL은 필수예요';
    return;
  }
  if (!isUrlValid.value) {
    error.value = '유효한 URL이 아니에요 (http(s)://)';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    const clip = await createClip({
      title: title.value,
      videoUrl: videoUrl.value,
      description: description.value,
    });
    await router.push({ name: 'clip-detail', params: { id: clip.id } });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '영상 등록에 실패했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 취소
    </button>

    <h1 class="text-2xl font-bold text-pepper mb-6">영상 등록</h1>

    <form class="space-y-4 max-w-2xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="clip-title">제목</label>
        <input
          id="clip-title"
          v-model="title"
          type="text"
          maxlength="120"
          placeholder="영상 제목"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="clip-url">영상 URL</label>
        <input
          id="clip-url"
          v-model="videoUrl"
          type="url"
          placeholder="https://www.youtube.com/watch?v=... 또는 https://chzzk.naver.com/clips/..."
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
        <p class="mt-1 text-xs text-ink-muted">유튜브 또는 치지직 클립 URL</p>
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="clip-desc">설명 (선택)</label>
        <textarea
          id="clip-desc"
          v-model="description"
          rows="6"
          placeholder="영상 설명, 타임스탬프 등"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper resize-y"
        />
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>

      <div class="flex gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
        >
          {{ submitting ? '등록 중...' : '등록하기' }}
        </button>
        <button
          type="button"
          class="rounded-md border border-border px-4 py-2 text-sm text-ink-muted hover:text-ink"
          @click="router.back()"
        >
          취소
        </button>
      </div>
    </form>
  </main>
</template>
