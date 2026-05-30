<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchNotices } from '@/api/notice';
import { fetchFreePosts } from '@/api/free';
import { fetchFanartList } from '@/api/fanart';
import { fetchClips } from '@/api/clip';
import { fetchPets } from '@/api/pet';
import { fetchLetters } from '@/api/letter';
import { fetchCafeConfig } from '@/api/cafe';
import { fetchStatsSummary, type StatsSummary } from '@/api/stats';
import type { NoticeListItem } from '@/types/notice';
import type { FreePostListItem } from '@/types/free';
import type { FanartListItem } from '@/types/fanart';
import type { ClipListItem } from '@/types/clip';
import type { PetListItem } from '@/types/pet';
import type { LetterListItem } from '@/types/letter';
import type { CafeConfig } from '@/types/cafe';
import LiveBanner from '@/components/home/LiveBanner.vue';

const notices = ref<NoticeListItem[]>([]);
const freePosts = ref<FreePostListItem[]>([]);
const fanarts = ref<FanartListItem[]>([]);
const clips = ref<ClipListItem[]>([]);
const pets = ref<PetListItem[]>([]);
const letters = ref<LetterListItem[]>([]);
const cafe = ref<CafeConfig | null>(null);
const stats = ref<StatsSummary | null>(null);
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
    all.push({ boardSlug: 'free', boardLabel: '자유', id: p.id, title: p.title, author: p.author, likeCount: p.likeCount })
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

const freeLatest = computed(() => freePosts.value.slice(0, 5));
const letterLatest = computed(() => letters.value.slice(0, 2));

interface BoardWidget {
  slug: string;
  label: string;
  emoji: string;
  gradient: string;
  latestTitle: string | null;
}

const friendBoards = computed<BoardWidget[]>(() => [
  {
    slug: 'free',
    label: '자유게시판',
    emoji: '💬',
    gradient: 'from-violet/25 to-violet/5',
    latestTitle: freePosts.value[0]?.title ?? null,
  },
  {
    slug: 'fanart',
    label: '팬아트',
    emoji: '🎨',
    gradient: 'from-cheek/25 to-cheek/5',
    latestTitle: fanarts.value[0]?.title ?? null,
  },
  {
    slug: 'pets',
    label: '반려동물',
    emoji: '🐾',
    gradient: 'from-pepper/25 to-pepper/5',
    latestTitle: pets.value[0]?.title ?? null,
  },
  {
    slug: 'clips',
    label: '클립 공유',
    emoji: '🎬',
    gradient: 'from-corn/25 to-corn/5',
    latestTitle: clips.value[0]?.title ?? null,
  },
]);

onMounted(async () => {
  try {
    const settled = await Promise.allSettled([
      fetchNotices(),
      fetchFreePosts(),
      fetchFanartList(),
      fetchClips(),
      fetchPets(),
      fetchLetters(),
      fetchCafeConfig(),
      fetchStatsSummary(),
    ]);
    const [noticeR, freeR, fanartR, clipR, petR, letterR, cafeR, statsR] = settled;
    if (noticeR.status === 'fulfilled') notices.value = noticeR.value.data;
    if (freeR.status === 'fulfilled') freePosts.value = freeR.value.data;
    if (fanartR.status === 'fulfilled') fanarts.value = fanartR.value.data;
    if (clipR.status === 'fulfilled') clips.value = clipR.value.data;
    if (petR.status === 'fulfilled') pets.value = petR.value.data;
    if (letterR.status === 'fulfilled') letters.value = letterR.value.data;
    if (cafeR.status === 'fulfilled') cafe.value = cafeR.value;
    if (statsR.status === 'fulfilled') stats.value = statsR.value;
  } finally {
    loading.value = false;
  }
});

const heroHeadline = computed(() => cafe.value?.heroHeadline || '오늘도 늉비랑 고추밭에 놀자!');
const heroSubtext = computed(() => cafe.value?.heroSubtext || '초록고추 여러분 안녕~ 오늘은 어떤 이야기를 나눠볼까요?');
const heroBannerUrl = computed(() => cafe.value?.heroBannerUrl || null);
const heroBannerPosition = computed(() => cafe.value?.heroBannerPosition || 'center');
const footerText = computed(() => cafe.value?.footerText || null);
</script>

<template>
  <div class="p-8 space-y-6">
    <section class="grid grid-cols-1 lg:grid-cols-2 gap-4 lg:min-h-[220px]">
      <LiveBanner />

      <div
        class="relative rounded-2xl overflow-hidden border border-border h-full"
        :class="heroBannerUrl ? '' : 'bg-gradient-to-br from-violet-deep/30 via-violet/15 to-corn/15'"
      >
        <img
          v-if="heroBannerUrl"
          :src="heroBannerUrl"
          alt=""
          class="absolute inset-0 w-full h-full object-cover"
          :style="{ objectPosition: heroBannerPosition }"
        />
        <div
          class="relative h-full px-6 py-8 flex items-end justify-between gap-4 flex-wrap"
          :class="heroBannerUrl ? 'bg-gradient-to-r from-paper/85 via-paper/50 to-paper/10' : ''"
        >
          <div class="flex-1 min-w-0">
            <h1 class="text-2xl font-extrabold text-ink leading-tight drop-shadow-sm">{{ heroHeadline }}</h1>
            <p class="mt-1 text-sm text-ink/80 drop-shadow-sm">{{ heroSubtext }}</p>
          </div>
          <div class="flex items-center gap-6">
            <div class="text-right">
              <div class="text-xl font-extrabold text-ink tabular-nums">
                {{ stats ? stats.totalMembers.toLocaleString('ko-KR') : '—' }}
              </div>
              <div class="text-[11px] text-ink-muted mt-0.5">전체 회원</div>
            </div>
            <div class="text-right">
              <div class="text-xl font-extrabold text-violet tabular-nums">
                {{ stats ? stats.todayNewPosts.toLocaleString('ko-KR') : '—' }}
              </div>
              <div class="text-[11px] text-ink-muted mt-0.5">오늘 새 글</div>
            </div>
          </div>
        </div>
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

    <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="rounded-2xl border border-border bg-surface p-5">
        <div class="flex items-center gap-2 mb-3">
          <span class="text-lg">🔥</span>
          <h2 class="text-base font-bold text-ink">이번 주 베스트</h2>
          <span class="text-xs text-ink-muted">좋아요 많은 순</span>
        </div>
        <p v-if="loading" class="text-sm text-ink-muted py-2">불러오는 중...</p>
        <ol v-else-if="bestPosts.length > 0" class="space-y-0.5">
          <li v-for="(p, i) in bestPosts" :key="`${p.boardSlug}-${p.id}`">
            <RouterLink
              :to="`/${p.boardSlug}/${p.id}`"
              class="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-elevated transition-colors"
            >
              <span class="text-sm font-bold text-violet w-5 text-center shrink-0">{{ i + 1 }}</span>
              <span class="text-sm text-ink truncate flex-1">{{ p.title }}</span>
              <span class="hidden sm:inline text-xs text-ink-muted whitespace-nowrap">{{ p.boardLabel }}</span>
              <span class="text-xs text-cheek whitespace-nowrap shrink-0">♥ {{ p.likeCount }}</span>
            </RouterLink>
          </li>
        </ol>
        <p v-else class="text-sm text-ink-muted py-4 text-center">아직 글이 없어요.</p>
      </div>

      <div class="rounded-2xl border border-border bg-surface p-5">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <span class="text-lg">💬</span>
            <h2 class="text-base font-bold text-ink">자유게시판 최신</h2>
          </div>
          <RouterLink to="/free" class="text-xs text-ink-muted hover:text-ink">더보기 →</RouterLink>
        </div>
        <p v-if="loading" class="text-sm text-ink-muted py-2">불러오는 중...</p>
        <ul v-else-if="freeLatest.length > 0" class="space-y-0.5">
          <li v-for="p in freeLatest" :key="p.id">
            <RouterLink
              :to="`/free/${p.id}`"
              class="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-elevated transition-colors"
            >
              <span class="text-sm text-ink truncate flex-1">{{ p.title }}</span>
              <span class="text-xs text-ink-muted whitespace-nowrap shrink-0">{{ p.author }}</span>
              <span class="text-xs text-cheek whitespace-nowrap shrink-0">♥ {{ p.likeCount }}</span>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="text-sm text-ink-muted py-4 text-center">아직 글이 없어요.</p>
      </div>
    </section>

    <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="rounded-2xl border border-border bg-surface p-5">
        <div class="flex items-center gap-2 mb-3">
          <span class="text-lg">🌱</span>
          <h2 class="text-base font-bold text-ink">친구가 알려요</h2>
          <span class="text-xs text-ink-muted">4개 게시판 최신</span>
        </div>
        <div class="grid grid-cols-2 gap-2">
          <RouterLink
            v-for="b in friendBoards"
            :key="b.slug"
            :to="`/${b.slug}`"
            class="block rounded-xl p-3 bg-gradient-to-br border border-border hover:border-violet/40 transition-colors min-h-[80px]"
            :class="b.gradient"
          >
            <div class="flex items-center gap-1.5 text-xs font-bold text-ink mb-1">
              <span>{{ b.emoji }}</span>
              <span>{{ b.label }}</span>
            </div>
            <div class="text-[11px] text-ink-muted line-clamp-2 leading-snug">
              {{ b.latestTitle ?? '아직 새 글이 없어요' }}
            </div>
          </RouterLink>
        </div>
      </div>

      <div class="rounded-2xl border border-border bg-surface p-5">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <span class="text-lg">💌</span>
            <h2 class="text-base font-bold text-ink">오늘의 팬레터</h2>
          </div>
          <RouterLink to="/letters" class="text-xs text-ink-muted hover:text-ink">더보기 →</RouterLink>
        </div>
        <p v-if="loading" class="text-sm text-ink-muted py-2">불러오는 중...</p>
        <div v-else-if="letterLatest.length > 0" class="grid grid-cols-1 sm:grid-cols-2 gap-2">
          <RouterLink
            v-for="l in letterLatest"
            :key="l.id"
            :to="`/letters/${l.id}`"
            class="block rounded-xl p-3 bg-corn/10 border border-corn/20 hover:bg-corn/15 transition-colors"
          >
            <div class="text-[10px] text-ink-muted mb-1">To. 늉비에게</div>
            <div class="text-xs text-ink line-clamp-3 leading-snug">{{ l.preview }}</div>
            <div class="mt-2 text-[10px] text-ink-muted">From. {{ l.author }}</div>
          </RouterLink>
        </div>
        <p v-else class="text-sm text-ink-muted py-4 text-center">아직 팬레터가 없어요.</p>
      </div>
    </section>

    <footer
      v-if="footerText"
      class="mt-6 pt-5 border-t border-border text-center text-xs text-ink-muted"
    >
      {{ footerText }}
    </footer>
  </div>
</template>
