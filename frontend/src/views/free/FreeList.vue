<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchFreePosts } from '@/api/free';
import { deletePost } from '@/api/post';
import { useCurrentUser } from '@/composables/useCurrentUser';
import type { FreePostListItem } from '@/types/free';

const posts = ref<FreePostListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const categories = ['전체', '공지', '잡담', '질문', '후기', '정보'];
const activeCategory = ref('전체');

const sortOptions = [
  { value: 'recent', label: '최신순' },
  { value: 'popular', label: '인기순' },
  { value: 'comment', label: '댓글순' },
];
const activeSort = ref<'recent' | 'popular' | 'comment'>('recent');

const HOT_THRESHOLD = 10;

const sortedPosts = computed(() => {
  const list = [...posts.value];
  if (activeSort.value === 'popular') list.sort((a, b) => b.likeCount - a.likeCount);
  else if (activeSort.value === 'comment') list.sort((a, b) => b.commentCount - a.commentCount);
  else list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  return list;
});

onMounted(async () => {
  try {
    const res = await fetchFreePosts();
    posts.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

function relativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return '방금';
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  const d = Math.floor(h / 24);
  if (d < 7) return `${d}일 전`;
  const w = Math.floor(d / 7);
  if (w < 5) return `${w}주 전`;
  const dt = new Date(iso);
  return `${String(dt.getFullYear()).slice(2)}.${String(dt.getMonth() + 1).padStart(2, '0')}.${String(dt.getDate()).padStart(2, '0')}`;
}

function authorInitial(name: string): string {
  return name ? name.charAt(0) : '?';
}

const { currentUser, isOwner: isAdmin } = useCurrentUser();
function canDeletePost(author: string): boolean {
  return isAdmin.value || currentUser.value?.nickname === author;
}
async function removePost(id: number): Promise<void> {
  if (!confirm('이 게시글을 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deletePost(id);
    posts.value = posts.value.filter((x) => x.id !== id);
  } catch {
    alert('삭제에 실패했어요');
  }
}
</script>

<template>
  <div class="p-8">
    <nav class="text-xs text-ink-muted mb-3">
      <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
      <span class="mx-2">›</span>
      <span class="text-ink">자유게시판</span>
    </nav>

    <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
      <div>
        <h1 class="text-4xl font-extrabold text-ink leading-tight">자유게시판</h1>
        <p class="mt-2 text-sm text-ink-muted">초록고추들끼리 편하게 떠드는 공간이에요 💬</p>
      </div>
      <RouterLink
        :to="{ name: 'free-write' }"
        class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors whitespace-nowrap"
      >
        ✏ 글쓰기
      </RouterLink>
    </header>

    <div class="flex items-center justify-between gap-4 mb-5 flex-wrap">
      <div class="flex items-center gap-2 flex-wrap">
        <button
          v-for="cat in categories"
          :key="cat"
          class="px-4 py-1.5 rounded-full text-sm transition-colors"
          :class="
            activeCategory === cat
              ? 'bg-violet-deep/40 text-ink font-semibold'
              : 'bg-elevated text-ink-muted hover:text-ink'
          "
          @click="activeCategory = cat"
        >
          {{ cat }}
        </button>
      </div>
      <div class="flex items-center gap-1 text-xs">
        <button
          v-for="opt in sortOptions"
          :key="opt.value"
          class="px-2.5 py-1 rounded transition-colors"
          :class="
            activeSort === opt.value
              ? 'text-ink font-semibold'
              : 'text-ink-muted hover:text-ink'
          "
          @click="activeSort = opt.value as 'recent' | 'popular' | 'comment'"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else>
      <div class="rounded-2xl bg-surface border border-border overflow-hidden">
        <div class="grid grid-cols-[70px_1fr_140px_90px_90px] gap-3 px-5 py-3 text-[11px] text-ink-muted border-b border-border tracking-wide">
          <span>분류</span>
          <span>제목</span>
          <span>글쓴이</span>
          <span>작성시간</span>
          <span class="text-right">조회 / ♥</span>
        </div>
        <ul v-if="sortedPosts.length > 0" class="divide-y divide-border">
          <li v-for="p in sortedPosts" :key="p.id" class="relative group hover:bg-elevated transition-colors">
            <button
              v-if="canDeletePost(p.author)"
              type="button"
              class="absolute right-2 top-1/2 -translate-y-1/2 z-10 rounded-md bg-paper/90 px-2 py-1 text-xs font-semibold text-cheek border border-cheek/40 opacity-0 group-hover:opacity-100 hover:bg-cheek hover:text-paper transition-all"
              @click.prevent.stop="removePost(p.id)"
            >
              🗑 삭제
            </button>
            <RouterLink
              :to="{ name: 'free-detail', params: { id: p.id } }"
              class="grid grid-cols-[70px_1fr_140px_90px_90px] gap-3 px-5 py-3 items-center text-sm"
            >
              <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-violet/20 text-violet text-center">
                잡담
              </span>
              <div class="min-w-0 flex items-center gap-2">
                <span class="text-ink truncate">{{ p.title }}</span>
                <span v-if="p.commentCount > 0" class="text-[11px] text-corn shrink-0">[{{ p.commentCount }}]</span>
                <span
                  v-if="p.likeCount >= HOT_THRESHOLD"
                  class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-500 text-white shrink-0"
                >
                  HOT
                </span>
              </div>
              <div class="flex items-center gap-2 min-w-0">
                <span class="w-5 h-5 rounded-full bg-violet/30 flex items-center justify-center text-[10px] font-bold text-ink shrink-0">
                  {{ authorInitial(p.author) }}
                </span>
                <span class="text-xs text-ink-muted truncate">{{ p.author }}</span>
              </div>
              <span class="text-xs text-ink-muted whitespace-nowrap">{{ relativeTime(p.createdAt) }}</span>
              <div class="text-right text-[11px] text-ink-muted whitespace-nowrap leading-tight">
                <div>조회 {{ p.viewCount.toLocaleString() }}</div>
                <div class="text-cheek">♥ {{ p.likeCount }}</div>
              </div>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="px-5 py-10 text-sm text-ink-muted text-center">아직 글이 없어요. 첫 글을 남겨보세요!</p>
      </div>
    </template>
  </div>
</template>
