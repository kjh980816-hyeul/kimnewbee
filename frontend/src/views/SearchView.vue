<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { search } from '@/api/search';
import type { SearchHit, SearchResponse } from '@/types/search';

const route = useRoute();
const router = useRouter();

const result = ref<SearchResponse | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const localKeyword = ref(typeof route.query['q'] === 'string' ? route.query['q'] : '');
const activeType = ref<'all' | 'notice' | 'post'>('all');

const filteredHits = computed<SearchHit[]>(() => {
  if (!result.value) return [];
  if (activeType.value === 'all') return result.value.hits;
  return result.value.hits.filter(h => h.type === activeType.value);
});

const groupedHits = computed(() => {
  const groups: Record<string, SearchHit[]> = {};
  filteredHits.value.forEach(h => {
    const key = h.boardLabel;
    if (!groups[key]) groups[key] = [];
    groups[key].push(h);
  });
  return groups;
});

const noticeCount = computed(() => result.value?.hits.filter(h => h.type === 'notice').length ?? 0);
const postCount = computed(() => result.value?.hits.filter(h => h.type === 'post').length ?? 0);

watch(
  () => route.query['q'],
  (q) => {
    const keyword = typeof q === 'string' ? q : '';
    localKeyword.value = keyword;
    runSearch(keyword);
  },
  { immediate: true }
);

async function runSearch(q: string): Promise<void> {
  if (!q.trim()) {
    result.value = null;
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    result.value = await search(q.trim());
  } catch (e) {
    error.value = e instanceof Error ? e.message : '검색에 실패했어요';
  } finally {
    loading.value = false;
  }
}

function onSubmit(e: Event): void {
  e.preventDefault();
  const q = localKeyword.value.trim();
  if (!q) return;
  router.push({ path: '/search', query: { q } });
}

function detailPath(hit: SearchHit): string {
  if (hit.type === 'notice') return `/notices/${hit.id}`;
  return `/${hit.boardSlug}/${hit.id}`;
}

function relativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return '방금';
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  const d = Math.floor(h / 24);
  if (d < 7) return `${d}일 전`;
  const dt = new Date(iso);
  return `${String(dt.getFullYear()).slice(2)}.${String(dt.getMonth() + 1).padStart(2, '0')}.${String(dt.getDate()).padStart(2, '0')}`;
}

function highlight(text: string, q: string): string {
  if (!q) return text;
  const safe = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  return text.replace(new RegExp(safe, 'gi'), m => `<mark class="bg-corn/30 text-ink px-0.5 rounded">${m}</mark>`);
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">검색</span>
    </nav>

    <header class="mb-6">
      <h1 class="text-4xl font-extrabold text-ink leading-tight">검색 🔍</h1>
      <p class="mt-2 text-sm text-ink-muted">고추밭 전체에서 글과 공지를 찾아봐요</p>
    </header>

    <form class="max-w-2xl mb-6" @submit="onSubmit">
      <div class="relative">
        <span class="absolute left-4 top-1/2 -translate-y-1/2 text-ink-muted text-base">🔍</span>
        <input
          v-model="localKeyword"
          type="text"
          placeholder="찾고 싶은 단어를 입력하세요..."
          class="w-full pl-11 pr-3 py-3 rounded-full bg-surface border border-border text-sm text-ink placeholder:text-ink-muted/60 focus:outline-none focus:border-violet/50"
          autofocus
        />
      </div>
    </form>

    <template v-if="result">
      <div class="mb-5 flex items-center gap-3 flex-wrap">
        <span class="text-sm text-ink-muted">
          <span class="text-ink font-semibold">'{{ result.query }}'</span> 검색 결과 <span class="text-violet font-bold tabular-nums">{{ result.total }}</span>건
        </span>
        <div class="flex items-center gap-1 ml-auto">
          <button
            type="button"
            class="px-3 py-1 rounded-full text-xs transition-colors"
            :class="activeType === 'all' ? 'bg-violet-deep/40 text-ink font-semibold' : 'text-ink-muted hover:text-ink'"
            @click="activeType = 'all'"
          >전체 {{ result.total }}</button>
          <button
            type="button"
            class="px-3 py-1 rounded-full text-xs transition-colors"
            :class="activeType === 'notice' ? 'bg-violet-deep/40 text-ink font-semibold' : 'text-ink-muted hover:text-ink'"
            @click="activeType = 'notice'"
          >공지 {{ noticeCount }}</button>
          <button
            type="button"
            class="px-3 py-1 rounded-full text-xs transition-colors"
            :class="activeType === 'post' ? 'bg-violet-deep/40 text-ink font-semibold' : 'text-ink-muted hover:text-ink'"
            @click="activeType = 'post'"
          >게시글 {{ postCount }}</button>
        </div>
      </div>
    </template>

    <p v-if="loading" class="text-ink-muted">검색하는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <p
      v-else-if="result && result.total === 0"
      class="text-sm text-ink-muted py-12 text-center"
    >
      '{{ result.query }}'에 대한 결과가 없어요. 다른 단어로 검색해보세요.
    </p>
    <p
      v-else-if="result && filteredHits.length === 0"
      class="text-sm text-ink-muted py-12 text-center"
    >
      해당 분류에 결과가 없어요
    </p>
    <p
      v-else-if="!result"
      class="text-sm text-ink-muted py-12 text-center"
    >
      검색어를 입력해주세요
    </p>

    <div v-else-if="result" class="space-y-6">
      <section
        v-for="(hits, label) in groupedHits"
        :key="label"
      >
        <h2 class="text-xs font-bold text-violet uppercase tracking-wider mb-2 px-1">{{ label }}</h2>
        <ul class="rounded-2xl bg-surface border border-border overflow-hidden divide-y divide-border">
          <li v-for="hit in hits" :key="`${hit.type}-${hit.id}`">
            <RouterLink
              :to="detailPath(hit)"
              class="block px-5 py-4 hover:bg-elevated transition-colors"
            >
              <div class="flex items-baseline gap-2 mb-1">
                <span class="text-[10px] px-1.5 py-0.5 rounded font-semibold shrink-0"
                  :class="hit.type === 'notice' ? 'bg-corn/20 text-corn' : 'bg-violet/20 text-violet'"
                >
                  {{ hit.boardLabel }}
                </span>
                <h3 class="text-sm font-semibold text-ink truncate" v-html="highlight(hit.title, result.query)"></h3>
              </div>
              <p
                class="text-xs text-ink-muted line-clamp-2 leading-snug ml-1 mt-1"
                v-html="highlight(hit.snippet, result.query)"
              ></p>
              <div class="mt-2 flex items-center gap-2 text-[11px] text-ink-muted">
                <span>{{ hit.author }}</span>
                <span>·</span>
                <span>{{ relativeTime(hit.createdAt) }}</span>
              </div>
            </RouterLink>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>
