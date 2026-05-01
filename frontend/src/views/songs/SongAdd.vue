<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createSong } from '@/api/song';

const router = useRouter();

const title = ref('');
const artist = ref('');
const link = ref('');
const submitting = ref(false);
const error = ref<string | null>(null);

const isLinkValid = computed(() => {
  if (!link.value) return false;
  try {
    const u = new URL(link.value);
    return u.protocol === 'https:' || u.protocol === 'http:';
  } catch {
    return false;
  }
});

async function onSubmit(): Promise<void> {
  if (!title.value.trim() || !artist.value.trim() || !link.value.trim()) {
    error.value = '제목, 아티스트, 링크 모두 필수예요';
    return;
  }
  if (!isLinkValid.value) {
    error.value = '유효한 URL이 아니에요 (http(s)://)';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    await createSong({
      title: title.value,
      artist: artist.value,
      link: link.value,
    });
    await router.push({ name: 'songs' });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '곡 추천에 실패했어요';
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

    <h1 class="text-2xl font-bold text-pepper mb-6">곡 추천하기</h1>

    <form class="space-y-4 max-w-2xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="song-title">곡 제목</label>
        <input
          id="song-title"
          v-model="title"
          type="text"
          maxlength="120"
          placeholder="비 오는 날"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="song-artist">아티스트</label>
        <input
          id="song-artist"
          v-model="artist"
          type="text"
          maxlength="80"
          placeholder="적재"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="song-link">링크</label>
        <input
          id="song-link"
          v-model="link"
          type="url"
          placeholder="https://www.youtube.com/watch?v=..."
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
        <p class="mt-1 text-xs text-ink-muted">유튜브, 스포티파이, 멜론 등 공유 가능한 링크</p>
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>

      <div class="flex gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
        >
          {{ submitting ? '추천 중...' : '추천하기' }}
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
