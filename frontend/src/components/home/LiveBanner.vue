<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { fetchLiveStatus } from '@/api/chzzk';
import type { LiveStatus } from '@/types/chzzk';

const status = ref<LiveStatus | null>(null);

onMounted(async () => {
  try {
    status.value = await fetchLiveStatus();
  } catch {
    status.value = null;
  }
});

const elapsed = computed(() => {
  if (!status.value?.isLive || !status.value.startedAt) return '';
  const min = Math.floor((Date.now() - new Date(status.value.startedAt).getTime()) / 60000);
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}시간 ${m}분` : `${m}분`;
});

const viewerStr = computed(() =>
  status.value?.viewerCount?.toLocaleString('ko-KR') ?? '0'
);
</script>

<template>
  <a
    v-if="status?.isLive"
    :href="status.channelUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="block h-full rounded-2xl bg-elevated border border-border overflow-hidden hover:border-violet transition-colors"
  >
    <div class="flex items-stretch h-full">
      <div class="relative w-44 shrink-0 bg-gradient-to-br from-cheek to-violet flex items-center justify-center">
        <span class="text-6xl select-none">🌶️</span>
        <span class="absolute left-2 top-2 px-2 py-0.5 rounded bg-red-500 text-white text-[10px] font-bold tracking-wider animate-pulse">
          LIVE
        </span>
      </div>
      <div class="flex-1 p-5 min-w-0 flex flex-col justify-center">
        <div class="flex items-center gap-2 text-xs mb-2 flex-wrap">
          <span class="px-1.5 py-0.5 rounded bg-chzzk/20 text-chzzk font-semibold">CHZZK · 방송중</span>
          <span class="text-ink-muted">👁 {{ viewerStr }}명 시청</span>
          <span class="text-ink-muted">· {{ elapsed }}</span>
        </div>
        <h2 class="text-lg font-bold text-ink line-clamp-2 mb-1">
          {{ status.title }}
        </h2>
        <div class="text-xs text-ink-muted">#버라이어티 · 김늉비</div>
      </div>
    </div>
  </a>

  <div
    v-else
    class="h-full rounded-2xl bg-elevated border border-border p-4 flex items-center gap-3"
  >
    <div class="w-10 h-10 rounded-lg bg-violet-deep/30 flex items-center justify-center text-lg">
      📺
    </div>
    <div class="text-sm text-ink-muted">늉비가 지금은 자리 비웠어요. 곧 봐요!</div>
  </div>
</template>
