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

const elapsedMinutes = computed(() => {
  if (!status.value?.startedAt) return 0;
  return Math.floor((Date.now() - new Date(status.value.startedAt).getTime()) / 60000);
});
</script>

<template>
  <a
    v-if="status?.isLive"
    :href="status.channelUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="block rounded-md bg-surface border border-chzzk hover:bg-elevated transition-colors p-4"
  >
    <div class="flex items-center gap-3">
      <span class="rounded-full bg-chzzk text-paper px-2 py-0.5 text-xs font-bold animate-pulse">
        LIVE
      </span>
      <span class="text-ink truncate flex-1">{{ status.title }}</span>
      <span class="text-xs text-ink-muted whitespace-nowrap">
        👁 {{ status.viewerCount.toLocaleString('ko-KR') }} · {{ elapsedMinutes }}분째
      </span>
    </div>
  </a>
  <div
    v-else-if="status"
    class="rounded-md bg-surface p-4 flex items-center gap-3"
  >
    <span class="rounded-full bg-elevated text-ink-muted px-2 py-0.5 text-xs">OFFLINE</span>
    <span class="text-sm text-ink-muted">늉비가 지금은 자리 비웠어요. 곧 봐요!</span>
  </div>
</template>
