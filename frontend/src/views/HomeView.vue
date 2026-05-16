<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchNotices } from '@/api/notice';
import { fetchFreePosts } from '@/api/free';
import { fetchFanartList } from '@/api/fanart';
import { fetchClips } from '@/api/clip';
import { fetchPets } from '@/api/pet';
import { fetchCafeConfig } from '@/api/cafe';
import type { NoticeListItem } from '@/types/notice';
import type { FreePostListItem } from '@/types/free';
import type { FanartListItem } from '@/types/fanart';
import type { ClipListItem } from '@/types/clip';
import type { PetListItem } from '@/types/pet';
import type { CafeConfig } from '@/types/cafe';
import LiveBanner from '@/components/home/LiveBanner.vue';

const notices = ref<NoticeListItem[]>([]);
const freePosts = ref<FreePostListItem[]>([]);
const fanarts = ref<FanartListItem[]>([]);
const clips = ref<ClipListItem[]>([]);
const pets = ref<PetListItem[]>([]);
const cafe = ref<CafeConfig | null>(null);
const loading = ref(true);

interface BestItem {
  boardSlug: string;
  boardLabel: string;
  id: number;
  title: string;
  author: string;
  likeCount: number;
}

const topNotice = computed(() => notices.value[0] ?? null);

const bestPosts = computed<BestItem[]>(() => {
  const all: BestItem[] = [];
  freePosts.value.forEach(p =>
    all.push({ boardSlug: 'free', boardLabel: '자유게시판', id: p.id, title: p.title, author: p.author, likeCount: p.likeCount })
  );
  fanarts.value.forEach(p =>
    all.push({ boardSlug: 'fanart', boardLabel: '팬아트', id: p.id, title: p.title, author: p.author, likeCount: p.likeCount })
  );
  clips.value.forEach(p =>
    all.push({ boardSlug: 'clips', boardLabel: '클립', id: p.id, title: p.title, author: p.author, likeCount: p.likeCount })
  );
  pets.value.forEach(p =>
    all.push({ boardSlug: 'pets', boardLabel: '반려동물', id: p.id, title: p.title, author: p.author, likeCount: p.likeCount })
  );
  return all.sort((a, b) => b.likeCount - a.likeCount).slice(0, 5);
});

onMounted(async () => {
  try {
    const settled = await Promise.allSettled([
      fetchNotices(),
      fetchFreePosts(),
      fetchFanartList(),
      fetchClips(),
      fetchPets(),
      fetchCafeConfig(),
    ]);
    const [noticeR, freeR, fanartR, clipR, petR, cafeR] = settled;
    if (noticeR.status === 'fulfilled') notices.value = noticeR.value.data;
    if (freeR.status === 'fulfilled') freePosts.value = freeR.value.data;
    if (fanartR.status === 'fulfilled') fanarts.value = fanartR.value.data;
    if (clipR.status === 'fulfilled') clips.value = clipR.value.data;
    if (petR.status === 'fulfilled') pets.value = petR.value.data;
    if (cafeR.status === 'fulfilled') cafe.value = cafeR.value;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="p-8 space-y-6 max-w-5xl">
    <LiveBanner />

    <section
      class="relative rounded-2xl border border-border overflow-hidden bg-elevated"
      :style="cafe?.heroBannerUrl ? `background-image: url('${cafe.heroBannerUrl}'); background-size: cover; background-position: center;` : ''"
    >
      <div
        class="p-6"
        :class="cafe?.heroBannerUrl ? 'bg-gradient-to-r from-elevated/95 via-elevated/80 to-elevated/40' : ''"
      >
        <div class="flex items-center gap-2 text-xs text-corn font-semibold mb-2">
          <span class="text-base">🌽</span>
          <span>초록고추 여러분 안녕~</span>
        </div>
        <h1 class="text-2xl font-extrabold text-ink leading-tight">
          {{ cafe?.heroHeadline || '오늘도 늉비랑 고추밭에서 놀자!' }}
        </h1>
        <p v-if="cafe?.heroSubtext" class="mt-2 text-sm text-ink-muted">
          {{ cafe.heroSubtext }}
        </p>
      </div>
    </section>

    <RouterLink
      v-if="topNotice"
      :to="`/notices/${topNotice.id}`"
      class="block rounded-xl bg-corn/10 border border-corn/30 px-4 py-3 hover:bg-corn/20 transition-colors"
    >
      <div class="flex items-center gap-3">
        <span class="px-2 py-0.5 rounded bg-corn text-paper text-xs font-bold shrink-0">공지</span>
        <span class="text-sm text-ink truncate">{{ topNotice.title }}</span>
      </div>
    </RouterLink>

    <section class="rounded-2xl border border-border bg-elevated p-5">
      <div class="flex items-center gap-2 mb-4">
        <span class="text-xl">🔥</span>
        <h2 class="text-lg font-bold text-ink">이번 주 베스트</h2>
        <span class="text-xs text-ink-muted">좋아요 많은 순</span>
      </div>
      <p v-if="loading" class="text-sm text-ink-muted">불러오는 중...</p>
      <ol v-else-if="bestPosts.length > 0" class="space-y-1">
        <li v-for="(p, i) in bestPosts" :key="`${p.boardSlug}-${p.id}`">
          <RouterLink
            :to="`/${p.boardSlug}/${p.id}`"
            class="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-surface transition-colors"
          >
            <span class="text-sm font-bold text-violet w-5 text-center shrink-0">{{ i + 1 }}</span>
            <span class="text-sm text-ink truncate flex-1">{{ p.title }}</span>
            <span class="hidden sm:inline text-xs text-ink-muted whitespace-nowrap">{{ p.boardLabel }}</span>
            <span class="hidden sm:inline text-xs text-ink-muted whitespace-nowrap">· {{ p.author }}</span>
            <span class="text-xs text-cheek whitespace-nowrap shrink-0">♥ {{ p.likeCount }}</span>
          </RouterLink>
        </li>
      </ol>
      <p v-else class="text-sm text-ink-muted py-4 text-center">아직 글이 없어요. 첫 글을 남겨보세요!</p>
    </section>
  </div>
</template>
