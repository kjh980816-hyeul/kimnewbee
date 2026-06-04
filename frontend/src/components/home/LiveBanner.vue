<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { fetchLiveStatus } from '@/api/chzzk';
import type { LiveStatus } from '@/types/chzzk';

const status = ref<LiveStatus | null>(null);
const thumbFailed = ref(false);

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
    class="live-banner"
  >
    <div class="glow"></div>
    <div class="live-thumb">
      <img
        v-if="status.thumbnailUrl && !thumbFailed"
        :src="status.thumbnailUrl"
        :alt="status.title"
        @error="thumbFailed = true"
      />
      <svg v-else width="42" height="42" viewBox="0 0 40 40" fill="none">
        <path d="M14 12 Q10 20 16 30 Q24 34 28 24 Q30 14 22 12 Q18 11 14 12 Z" fill="#A3EC8E" stroke="#3C8C3B" stroke-width="1.3"></path>
      </svg>
      <span class="ld">LIVE</span>
    </div>
    <div class="min-w-0 flex-1">
      <div class="flex items-center gap-2.5 mb-1.5 flex-wrap">
        <span class="chzzk-chip">CHZZK · 방송중</span>
        <span class="text-[11px]" style="color: #b8b8be">👥 {{ viewerStr }}명 시청 · {{ elapsed }}째</span>
      </div>
      <div class="text-[18px] font-bold text-ink truncate">{{ status.title }}</div>
      <div class="text-xs mt-0.5" style="color: var(--text-mute)">#버라이어티 · 김늉비</div>
    </div>
    <span class="btn-chzzk">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 12a9 9 0 0 1 18 0M6 12a6 6 0 0 1 12 0M9 12a3 3 0 0 1 6 0"></path></svg>
      지금 보러가기
    </span>
  </a>

  <div v-else class="glass card-pad flex items-center gap-3">
    <div class="w-10 h-10 rounded-xl flex items-center justify-center text-lg" style="background: rgba(95, 199, 107, 0.12)">
      📺
    </div>
    <div class="text-sm" style="color: var(--text-mute)">늉비가 지금은 자리 비웠어요. 곧 봐요! 🌶️</div>
  </div>
</template>
