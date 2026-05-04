<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchNotices } from '@/api/notice';
import { fetchFreePosts } from '@/api/free';
import { fetchFanartList } from '@/api/fanart';
import { fetchClips } from '@/api/clip';
import { fetchPets } from '@/api/pet';
import { fetchSongs } from '@/api/song';
import { fetchCafeConfig } from '@/api/cafe';
import type { NoticeListItem } from '@/types/notice';
import type { FreePostListItem } from '@/types/free';
import type { FanartListItem } from '@/types/fanart';
import type { ClipListItem } from '@/types/clip';
import type { PetListItem } from '@/types/pet';
import type { SongRecommendation } from '@/types/song';
import type { CafeConfig } from '@/types/cafe';
import LiveBanner from '@/components/home/LiveBanner.vue';
import WidgetCard from '@/components/home/WidgetCard.vue';

const notices = ref<NoticeListItem[]>([]);
const freePosts = ref<FreePostListItem[]>([]);
const fanarts = ref<FanartListItem[]>([]);
const clips = ref<ClipListItem[]>([]);
const pets = ref<PetListItem[]>([]);
const topSongs = ref<SongRecommendation[]>([]);
const cafe = ref<CafeConfig | null>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    const [noticeRes, freeRes, fanartRes, clipRes, petRes, songRes, cafeRes] = await Promise.all([
      fetchNotices(),
      fetchFreePosts(),
      fetchFanartList(),
      fetchClips(),
      fetchPets(),
      fetchSongs(),
      fetchCafeConfig(),
    ]);
    notices.value = noticeRes.data.slice(0, 4);
    freePosts.value = freeRes.data.slice(0, 5);
    fanarts.value = fanartRes.data.slice(0, 6);
    clips.value = clipRes.data.slice(0, 3);
    pets.value = petRes.data.slice(0, 6);
    topSongs.value = songRes.data.slice(0, 3);
    cafe.value = cafeRes;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <header
      v-if="cafe?.heroBannerUrl"
      class="mb-6 -mx-8 -mt-8 aspect-[3/1] bg-elevated overflow-hidden relative"
    >
      <img
        :src="cafe.heroBannerUrl"
        :alt="cafe.heroHeadline"
        class="w-full h-full object-cover"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-paper to-transparent flex flex-col justify-end p-8">
        <h1 class="text-3xl font-bold text-pepper">{{ cafe.heroHeadline }}</h1>
        <p v-if="cafe.heroSubtext" class="mt-1 text-ink">{{ cafe.heroSubtext }}</p>
      </div>
    </header>
    <header v-else class="mb-6">
      <h1 class="text-3xl font-bold text-pepper">{{ cafe?.heroHeadline ?? '고추밭' }}</h1>
      <p class="mt-1 text-ink-muted">{{ cafe?.heroSubtext ?? '초록고추들을 위한 공간' }}</p>
    </header>

    <LiveBanner class="mb-6" />

    <p v-if="loading" class="text-ink-muted">최신 글을 불러오는 중...</p>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <WidgetCard title="공지사항" :view-all-route="{ name: 'notices' }">
        <ul class="divide-y divide-border">
          <li v-for="n in notices" :key="n.id">
            <RouterLink
              :to="{ name: 'notice-detail', params: { id: n.id } }"
              class="flex justify-between gap-2 py-2 text-sm hover:text-pepper"
            >
              <span class="truncate text-ink">{{ n.title }}</span>
              <span class="text-xs text-ink-muted whitespace-nowrap">
                {{ new Date(n.createdAt).toLocaleDateString('ko-KR') }}
              </span>
            </RouterLink>
          </li>
        </ul>
      </WidgetCard>

      <WidgetCard title="자유게시판" :view-all-route="{ name: 'free' }">
        <ul class="divide-y divide-border">
          <li v-for="p in freePosts" :key="p.id">
            <RouterLink
              :to="{ name: 'free-detail', params: { id: p.id } }"
              class="flex justify-between gap-2 py-2 text-sm hover:text-pepper"
            >
              <span class="truncate text-ink">
                {{ p.title }}
                <span v-if="p.commentCount > 0" class="text-corn text-xs ml-1">
                  [{{ p.commentCount }}]
                </span>
              </span>
              <span class="text-xs text-ink-muted whitespace-nowrap">♥ {{ p.likeCount }}</span>
            </RouterLink>
          </li>
        </ul>
      </WidgetCard>

      <WidgetCard
        class="md:col-span-2"
        title="팬아트 갤러리"
        :view-all-route="{ name: 'fanart' }"
      >
        <ul class="grid grid-cols-3 md:grid-cols-6 gap-2">
          <li v-for="f in fanarts" :key="f.id">
            <RouterLink
              :to="{ name: 'fanart-detail', params: { id: f.id } }"
              class="block aspect-square rounded-md overflow-hidden bg-elevated hover:opacity-80 transition-opacity"
            >
              <img :src="f.thumbnailUrl" :alt="f.title" class="w-full h-full object-cover" loading="lazy" />
            </RouterLink>
          </li>
        </ul>
      </WidgetCard>

      <WidgetCard title="영상/클립" :view-all-route="{ name: 'clips' }">
        <ul class="space-y-2">
          <li v-for="c in clips" :key="c.id">
            <RouterLink
              :to="{ name: 'clip-detail', params: { id: c.id } }"
              class="flex items-center gap-2 py-1 text-sm hover:text-pepper"
            >
              <span
                class="rounded-full px-2 py-0.5 text-xs shrink-0"
                :class="{
                  'bg-chzzk text-paper': c.source === 'chzzk',
                  'bg-corn text-paper': c.source === 'youtube',
                  'bg-elevated text-ink-muted': c.source === 'other',
                }"
              >
                {{ c.source.toUpperCase() }}
              </span>
              <span class="truncate text-ink">{{ c.title }}</span>
            </RouterLink>
          </li>
        </ul>
      </WidgetCard>

      <WidgetCard title="노래 TOP 3" :view-all-route="{ name: 'songs' }">
        <ol class="space-y-2">
          <li
            v-for="(s, i) in topSongs"
            :key="s.id"
            class="flex items-center gap-3 text-sm"
          >
            <span class="text-corn font-bold w-5 text-center shrink-0">{{ i + 1 }}</span>
            <a
              :href="s.link"
              target="_blank"
              rel="noopener noreferrer"
              class="truncate text-ink hover:text-pepper transition-colors flex-1"
            >
              {{ s.title }}
              <span class="text-xs text-ink-muted ml-1">— {{ s.artist }}</span>
            </a>
            <span class="text-xs text-ink-muted shrink-0">▲ {{ s.voteCount }}</span>
          </li>
        </ol>
      </WidgetCard>

      <WidgetCard
        class="md:col-span-2"
        title="반려동물 사진"
        :view-all-route="{ name: 'pets' }"
      >
        <ul class="grid grid-cols-3 md:grid-cols-6 gap-2">
          <li v-for="p in pets" :key="p.id">
            <RouterLink
              :to="{ name: 'pet-detail', params: { id: p.id } }"
              class="block aspect-square rounded-md overflow-hidden bg-elevated hover:opacity-80 transition-opacity"
            >
              <img :src="p.thumbnailUrl" :alt="p.title" class="w-full h-full object-cover" loading="lazy" />
            </RouterLink>
          </li>
        </ul>
      </WidgetCard>
    </div>

    <footer
      v-if="cafe?.footerText"
      class="mt-10 pt-6 border-t border-border text-center text-xs text-ink-muted"
    >
      {{ cafe.footerText }}
    </footer>
  </main>
</template>
