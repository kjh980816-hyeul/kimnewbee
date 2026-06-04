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

const heroHeadline = computed(() => cafe.value?.heroHeadline || '오늘도 늉비랑 고추밭에서 놀자!');
const heroSubtext = computed(() => cafe.value?.heroSubtext || '🌶️ 초록고추 여러분 안녕~');
const heroBannerUrl = computed(() => cafe.value?.heroBannerUrl || null);
const heroBannerPosition = computed(() => cafe.value?.heroBannerPosition || 'center');
const footerText = computed(() => cafe.value?.footerText || null);

// 표현용 헬퍼(데이터 가공 아님)
const CAT_TAG: Record<string, string> = { 잡담: 'green', 질문: 'cheek', 후기: 'green', 정보: 'corn', 공지: 'corn' };
function catTag(cat?: string | null): { label: string; cls: string } {
  const c = cat && cat.trim() ? cat : '잡담';
  return { label: c, cls: CAT_TAG[c] ?? 'mute' };
}
function relTime(iso: string): string {
  const m = Math.floor((Date.now() - new Date(iso).getTime()) / 60000);
  if (m < 1) return '방금';
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  return `${Math.floor(h / 24)}일 전`;
}
</script>

<template>
  <div class="p-8 flex flex-col gap-[22px]">
    <LiveBanner />

    <RouterLink
      v-if="topNotice"
      :to="`/notices/${topNotice.id}`"
      class="flex items-center gap-3 rounded-xl px-4 py-3 transition-colors"
      style="background: rgba(242, 212, 90, 0.08); border: 1px solid rgba(242, 212, 90, 0.22)"
    >
      <span class="tag corn shrink-0">📌 공지</span>
      <span class="text-sm text-ink truncate">{{ topNotice.title }}</span>
    </RouterLink>

    <!-- 환영 + 통계 -->
    <section
      class="glass card-pad welcome relative overflow-hidden"
      :style="heroBannerUrl ? { backgroundImage: `linear-gradient(90deg, rgba(15,15,16,.92), rgba(15,15,16,.45)), url(${heroBannerUrl})`, backgroundSize: 'cover', backgroundPosition: heroBannerPosition } : {}"
    >
      <div class="greet flex-1 min-w-0">
        <div class="k">{{ heroSubtext }}</div>
        <h2>{{ heroHeadline }}</h2>
      </div>
      <div class="stat">
        <div class="n" style="color: var(--green-bright)">
          {{ stats ? stats.totalMembers.toLocaleString('ko-KR') : '—' }}
        </div>
        <div class="l">초록고추</div>
      </div>
      <div class="stat">
        <div class="n" style="color: var(--corn)">
          {{ stats ? stats.todayNewPosts.toLocaleString('ko-KR') : '—' }}
        </div>
        <div class="l">오늘 새 글</div>
      </div>
    </section>

    <!-- 베스트 + 자유게시판 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-[22px]">
      <section class="glass card-pad">
        <div class="card-head">
          <h3>🔥 이번 주 베스트</h3>
          <span class="sub">좋아요순</span>
        </div>
        <p v-if="loading" class="text-sm py-2" style="color: var(--text-mute)">불러오는 중...</p>
        <template v-else-if="bestPosts.length > 0">
          <RouterLink
            v-for="(p, i) in bestPosts"
            :key="`${p.boardSlug}-${p.id}`"
            :to="`/${p.boardSlug}/${p.id}`"
            class="row"
          >
            <span class="rank" :class="i < 3 ? 'top' : 'norm'">{{ i + 1 }}</span>
            <div style="flex: 1; min-width: 0">
              <div class="ttl">{{ p.title }}</div>
              <div class="meta">{{ p.boardLabel }} · {{ p.author }} · ♥{{ p.likeCount }}</div>
            </div>
          </RouterLink>
        </template>
        <p v-else class="text-sm py-4 text-center" style="color: var(--text-mute)">아직 글이 없어요.</p>
      </section>

      <section class="glass card-pad">
        <div class="card-head">
          <h3>💬 자유게시판</h3>
          <span class="sub">실시간</span>
          <RouterLink to="/free" class="more">전체 ▸</RouterLink>
        </div>
        <p v-if="loading" class="text-sm py-2" style="color: var(--text-mute)">불러오는 중...</p>
        <template v-else-if="freeLatest.length > 0">
          <RouterLink v-for="p in freeLatest" :key="p.id" :to="`/free/${p.id}`" class="row">
            <span class="tag" :class="catTag(p.category).cls">{{ catTag(p.category).label }}</span>
            <div style="flex: 1; min-width: 0">
              <div class="ttl">
                {{ p.title }}
                <span v-if="p.commentCount > 0" style="color: var(--cheek); font-size: 11px; font-weight: 700">[{{ p.commentCount }}]</span>
              </div>
              <div class="meta">{{ p.author }} · {{ relTime(p.createdAt) }}</div>
            </div>
          </RouterLink>
        </template>
        <p v-else class="text-sm py-4 text-center" style="color: var(--text-mute)">아직 글이 없어요.</p>
      </section>
    </div>

    <!-- 갤러리: 팬아트 + 반려동물 -->
    <div class="grid grid-cols-1 lg:grid-cols-[1.3fr_1fr] gap-[22px]">
      <section class="glass card-pad">
        <div class="card-head">
          <h3>🎨 팬아트 갤러리</h3>
          <RouterLink to="/fanart" class="more">더 보기 ▸</RouterLink>
        </div>
        <div v-if="fanarts.length > 0" class="grid grid-cols-2 sm:grid-cols-4 gap-[9px]">
          <RouterLink
            v-for="a in fanarts.slice(0, 4)"
            :key="a.id"
            :to="`/fanart/${a.id}`"
            class="thumb"
            style="aspect-ratio: 1"
          >
            <img v-if="a.thumbnailUrl" :src="a.thumbnailUrl" :alt="a.title" />
            <div v-else class="lbl">{{ a.title }}</div>
            <div class="ov">
              <span style="font-weight: 700">@{{ a.author }}</span>
              <span>♥ {{ a.likeCount }}</span>
            </div>
          </RouterLink>
        </div>
        <p v-else class="text-sm py-4 text-center" style="color: var(--text-mute)">아직 팬아트가 없어요.</p>
      </section>

      <section class="glass card-pad">
        <div class="card-head">
          <h3>🐾 오늘의 반려동물</h3>
          <RouterLink to="/pets" class="more">더 보기 ▸</RouterLink>
        </div>
        <div v-if="pets.length > 0" class="grid grid-cols-2 gap-[9px]">
          <RouterLink v-for="pet in pets.slice(0, 2)" :key="pet.id" :to="`/pets/${pet.id}`" class="block">
            <div class="thumb" style="aspect-ratio: 4/3">
              <img v-if="pet.thumbnailUrl" :src="pet.thumbnailUrl" :alt="pet.title" />
              <div v-else class="lbl">{{ pet.title }}</div>
            </div>
            <div class="text-xs font-semibold text-ink mt-1.5 truncate">{{ pet.title }}</div>
            <div class="text-[11px] truncate" style="color: var(--text-mute)">@{{ pet.author }}</div>
          </RouterLink>
        </div>
        <p v-else class="text-sm py-4 text-center" style="color: var(--text-mute)">아직 사진이 없어요.</p>
      </section>
    </div>

    <!-- 팬레터 -->
    <section class="glass card-pad">
      <div class="card-head">
        <h3>💌 오늘의 팬레터</h3>
        <RouterLink to="/letters" class="more">전체 ▸</RouterLink>
      </div>
      <p v-if="loading" class="text-sm py-2" style="color: var(--text-mute)">불러오는 중...</p>
      <div v-else-if="letterLatest.length > 0" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <RouterLink
          v-for="l in letterLatest"
          :key="l.id"
          :to="`/letters/${l.id}`"
          class="block rounded-xl p-4"
          style="background: rgba(232, 155, 190, 0.08); border: 1px solid rgba(232, 155, 190, 0.2)"
        >
          <div class="text-[10px] mb-1" style="color: var(--text-mute)">To. 늉비에게</div>
          <div class="text-sm text-ink leading-snug line-clamp-3" style="font-family: var(--font-serif)">{{ l.preview }}</div>
          <div class="mt-2 text-[10px]" style="color: var(--text-mute)">From. {{ l.author }}</div>
        </RouterLink>
      </div>
      <p v-else class="text-sm py-4 text-center" style="color: var(--text-mute)">아직 팬레터가 없어요.</p>
    </section>

    <footer v-if="footerText" class="mt-2 pt-5 text-center text-xs" style="border-top: 1px solid var(--line); color: var(--text-mute)">
      {{ footerText }}
    </footer>
  </div>
</template>
