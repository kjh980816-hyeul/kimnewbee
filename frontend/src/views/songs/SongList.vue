<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchSongs, toggleSongVote } from '@/api/song';
import type { SongRecommendation } from '@/types/song';

const songs = ref<SongRecommendation[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await fetchSongs();
    songs.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '곡 목록을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onVote(song: SongRecommendation): Promise<void> {
  const res = await toggleSongVote(song.id);
  song.votedByMe = res.voted;
  song.voteCount = res.voteCount;
  songs.value = [...songs.value].sort((a, b) => b.voteCount - a.voteCount);
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header class="mb-6 flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-pepper">노래추천</h1>
        <p class="mt-1 text-ink-muted text-sm">
          늉비에게 추천하고 싶은 곡 — 투표 많은 순으로 정렬
        </p>
      </div>
      <RouterLink
        :to="{ name: 'song-add' }"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep transition-colors"
      >
        곡 추천하기
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <ol v-else class="space-y-2">
      <li
        v-for="(song, index) in songs"
        :key="song.id"
        class="rounded-md bg-surface hover:bg-elevated transition-colors p-4 flex items-center gap-4"
      >
        <div class="text-2xl font-bold text-corn w-10 text-center shrink-0">
          {{ index + 1 }}
        </div>
        <div class="min-w-0 flex-1">
          <a
            :href="song.link"
            target="_blank"
            rel="noopener noreferrer"
            class="text-ink hover:text-pepper transition-colors block truncate"
          >
            {{ song.title }}
            <span class="text-sm text-ink-muted ml-1">— {{ song.artist }}</span>
          </a>
          <div class="mt-1 text-xs text-ink-muted">
            {{ song.submittedBy }} 추천 · {{ new Date(song.createdAt).toLocaleDateString('ko-KR') }}
          </div>
        </div>
        <button
          type="button"
          class="rounded-md border px-3 py-1.5 text-sm transition-colors shrink-0"
          :class="song.votedByMe ? 'bg-pepper text-paper border-pepper' : 'border-border text-ink-muted hover:text-pepper hover:border-pepper'"
          @click="onVote(song)"
        >
          ▲ {{ song.voteCount }}
        </button>
      </li>
    </ol>
  </main>
</template>
