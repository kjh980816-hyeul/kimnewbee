<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchSongs, toggleSongVote } from '@/api/song';
import type { SongRecommendation } from '@/types/song';

const songs = ref<SongRecommendation[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const sortedSongs = computed(() => [...songs.value].sort((a, b) => b.voteCount - a.voteCount));
const topSong = computed(() => sortedSongs.value[0] ?? null);
const restSongs = computed(() => sortedSongs.value.slice(1));

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
}

function authorInitial(name: string): string {
  return name ? name.charAt(0) : '?';
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">노래추천</span>
    </nav>

    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">노래추천 🎵</h1>
        <p class="mt-2 text-sm text-ink-muted">늉비가 불러줬으면 하는 곡 추천해주세요~</p>
      </div>
      <RouterLink
        :to="{ name: 'song-add' }"
        class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors whitespace-nowrap"
      >
        + 노래 추천하기
      </RouterLink>
    </header>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <p v-else-if="songs.length === 0" class="text-sm text-ink-muted py-12 text-center">
      아직 추천된 곡이 없어요. 첫 곡을 추천해보세요!
    </p>
    <template v-else>
      <div
        v-if="topSong"
        class="rounded-2xl bg-gradient-to-br from-corn/20 via-cheek/10 to-violet/15 border border-corn/30 p-6 mb-4"
      >
        <div class="flex items-center gap-2 text-xs text-corn font-semibold mb-2">
          <span>🌟 이번 주 많이 추천한 곡</span>
          <span class="text-cheek">♥ {{ topSong.voteCount }}</span>
        </div>
        <h2 class="text-2xl font-extrabold text-ink leading-tight">
          {{ topSong.title }}
          <span class="text-base text-ink-muted font-bold ml-2">· {{ topSong.artist }}</span>
        </h2>
        <p class="mt-2 text-xs text-ink-muted">매주 월요일 가장 많이 추천된 곡이 방송에서 불러져요!</p>
      </div>

      <ol class="rounded-2xl bg-surface border border-border overflow-hidden divide-y divide-border">
        <li
          v-for="(song, idx) in restSongs"
          :key="song.id"
          class="flex items-center gap-4 px-5 py-4 hover:bg-elevated transition-colors"
        >
          <div class="text-2xl font-extrabold text-violet w-8 text-center shrink-0 tabular-nums">
            {{ idx + 2 }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-baseline gap-2 truncate">
              <span class="text-base font-bold text-ink truncate">{{ song.title }}</span>
              <span class="text-sm text-ink-muted shrink-0">· {{ song.artist }}</span>
            </div>
          </div>
          <div class="hidden sm:flex items-center gap-2 shrink-0">
            <span class="w-6 h-6 rounded-full bg-violet/30 flex items-center justify-center text-[10px] font-bold text-ink">
              {{ authorInitial(song.submittedBy) }}
            </span>
            <span class="text-xs text-ink-muted">@{{ song.submittedBy }}</span>
          </div>
          <button
            type="button"
            class="rounded-full px-3 py-1.5 text-xs font-semibold transition-colors shrink-0"
            :class="song.votedByMe ? 'bg-cheek/30 text-cheek' : 'bg-cheek/15 text-cheek hover:bg-cheek/25'"
            @click="onVote(song)"
          >
            ♥ {{ song.voteCount }} 투표
          </button>
          <a
            :href="song.link"
            target="_blank"
            rel="noopener noreferrer"
            class="rounded-full border border-border px-3 py-1.5 text-xs text-ink-muted hover:text-ink hover:border-violet/40 transition-colors shrink-0 flex items-center gap-1"
          >
            <span>▶</span><span>듣기</span>
          </a>
        </li>
      </ol>
    </template>
  </div>
</template>
