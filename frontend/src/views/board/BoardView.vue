<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { fetchBoardPosts } from '@/api/boardPost';
import { deletePost } from '@/api/post';
import { useBoards, tierRank } from '@/composables/useBoards';
import { useCurrentUser } from '@/composables/useCurrentUser';
import type { BoardPostListItem } from '@/types/boardPost';

const route = useRoute();
const slug = computed(() => String(route.params['slug']));

const { boards, loaded: boardsLoaded } = useBoards();
const board = computed(() => boards.value.find((b) => b.slug === slug.value) ?? null);

const { currentUser, isOwner } = useCurrentUser();
const canWrite = computed(() => {
  if (!board.value || !currentUser.value) return false;
  return tierRank(currentUser.value.tier) >= tierRank(board.value.writeTier);
});
function canDelete(author: string): boolean {
  return isOwner.value || currentUser.value?.nickname === author;
}

const posts = ref<BoardPostListItem[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const displayPosts = computed(() => {
  const list = [...posts.value];
  if (board.value?.layout === 'rank') {
    list.sort((a, b) => b.likeCount - a.likeCount);
  }
  return list;
});

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const res = await fetchBoardPosts(slug.value);
    posts.value = res.data;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시판을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
watch(slug, load);

async function removePost(id: number): Promise<void> {
  if (!confirm('이 게시글을 삭제할까요? 되돌릴 수 없어요.')) return;
  try {
    await deletePost(id);
    posts.value = posts.value.filter((p) => p.id !== id);
  } catch {
    alert('삭제에 실패했어요');
  }
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

const failedImages = ref<Set<number>>(new Set());
function markFailed(id: number): void {
  failedImages.value = new Set(failedImages.value).add(id);
}
function hasImage(p: BoardPostListItem): boolean {
  return !!p.mediaUrl && !failedImages.value.has(p.id);
}
</script>

<template>
  <div class="p-8">
    <p v-if="boardsLoaded && !board" class="text-ink-muted py-16 text-center">
      존재하지 않는 게시판이에요.
    </p>
    <template v-else>
      <nav class="text-xs text-ink-muted mb-3">
        <RouterLink to="/" class="hover:text-ink">🌶 고추밭</RouterLink>
        <span class="mx-2">›</span>
        <span class="text-ink">{{ board?.name ?? '게시판' }}</span>
      </nav>

      <header class="mb-6 flex items-end justify-between gap-4 flex-wrap">
        <h1 class="text-4xl font-extrabold text-ink leading-tight">{{ board?.name ?? '게시판' }}</h1>
        <RouterLink
          v-if="canWrite"
          :to="{ name: 'board-write', params: { slug } }"
          class="rounded-full bg-violet-deep px-5 py-2 text-sm font-semibold text-ink hover:bg-violet-deep/80 transition-colors whitespace-nowrap"
        >
          ✏ 글쓰기
        </RouterLink>
      </header>

      <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
      <p v-else-if="error" class="text-cheek">{{ error }}</p>
      <p v-else-if="displayPosts.length === 0" class="text-sm text-ink-muted py-12 text-center">
        아직 글이 없어요. 첫 글을 남겨보세요!
      </p>

      <template v-else>
        <!-- 갤러리형 -->
        <div v-if="board?.layout === 'gallery'" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
          <RouterLink
            v-for="p in displayPosts"
            :key="p.id"
            :to="{ name: 'board-post-detail', params: { slug, id: p.id } }"
            class="group relative rounded-xl overflow-hidden border border-border bg-surface aspect-square"
          >
            <img
              v-if="hasImage(p)"
              :src="p.mediaUrl!"
              :alt="p.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform"
              @error="markFailed(p.id)"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-3xl bg-elevated">🌶️</div>
            <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-2">
              <p class="text-xs text-white truncate">{{ p.title }}</p>
              <p class="text-[10px] text-white/70 truncate">{{ p.author }} · ♥ {{ p.likeCount }}</p>
            </div>
          </RouterLink>
        </div>

        <!-- 카드형 / 영상형 -->
        <div v-else-if="board?.layout === 'card' || board?.layout === 'video'" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="p in displayPosts"
            :key="p.id"
            class="relative group rounded-2xl border border-border bg-surface overflow-hidden hover:border-violet/40 transition-colors"
          >
            <button
              v-if="canDelete(p.author)"
              type="button"
              class="absolute right-2 top-2 z-10 rounded-md bg-paper/90 px-2 py-1 text-xs font-semibold text-cheek border border-cheek/40 opacity-0 group-hover:opacity-100 transition-all"
              @click="removePost(p.id)"
            >
              🗑 삭제
            </button>
            <RouterLink :to="{ name: 'board-post-detail', params: { slug, id: p.id } }" class="block">
              <div v-if="board?.layout === 'video'" class="aspect-video bg-elevated flex items-center justify-center relative">
                <img v-if="hasImage(p)" :src="p.mediaUrl!" :alt="p.title" class="w-full h-full object-cover" @error="markFailed(p.id)" />
                <span class="absolute text-4xl text-white drop-shadow">▶</span>
              </div>
              <img
                v-else-if="hasImage(p)"
                :src="p.mediaUrl!"
                :alt="p.title"
                class="w-full h-44 object-cover"
                @error="markFailed(p.id)"
              />
              <div class="p-4">
                <h2 class="font-bold text-ink truncate">{{ p.title }}</h2>
                <p class="mt-1 text-sm text-ink-muted line-clamp-2">{{ p.preview }}</p>
                <div class="mt-3 flex items-center gap-2 text-xs text-ink-muted">
                  <span>{{ p.author }}</span>
                  <span>·</span>
                  <span>{{ relativeTime(p.createdAt) }}</span>
                  <span class="ml-auto text-cheek">♥ {{ p.likeCount }}</span>
                  <span>💬 {{ p.commentCount }}</span>
                </div>
              </div>
            </RouterLink>
          </div>
        </div>

        <!-- 편지형 -->
        <div v-else-if="board?.layout === 'letter'" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <RouterLink
            v-for="p in displayPosts"
            :key="p.id"
            :to="{ name: 'board-post-detail', params: { slug, id: p.id } }"
            class="block rounded-2xl border border-violet/30 bg-violet/10 p-5 min-h-[180px] flex flex-col hover:scale-[1.02] transition-transform"
          >
            <h2 class="text-sm font-semibold text-ink mb-2">{{ p.title }}</h2>
            <p class="text-sm text-ink/90 line-clamp-4 flex-1">{{ p.preview }}</p>
            <div class="mt-3 pt-3 border-t border-dashed border-ink/15 text-[11px] text-ink-muted flex justify-between">
              <span>FROM. {{ p.author }}</span>
              <span>{{ relativeTime(p.createdAt) }}</span>
            </div>
          </RouterLink>
        </div>

        <!-- 투표형(랭킹) -->
        <ol v-else-if="board?.layout === 'rank'" class="rounded-2xl bg-surface border border-border overflow-hidden divide-y divide-border">
          <li
            v-for="(p, idx) in displayPosts"
            :key="p.id"
            class="relative group flex items-center gap-4 px-5 py-4 hover:bg-elevated transition-colors"
          >
            <div class="text-2xl font-extrabold w-8 text-center shrink-0 tabular-nums" :class="idx === 0 ? 'text-corn' : 'text-violet'">
              {{ idx + 1 }}
            </div>
            <RouterLink :to="{ name: 'board-post-detail', params: { slug, id: p.id } }" class="min-w-0 flex-1">
              <div class="font-bold text-ink truncate">{{ p.title }}</div>
              <div class="text-xs text-ink-muted truncate">{{ p.author }} · {{ p.preview }}</div>
            </RouterLink>
            <span class="text-cheek text-sm font-semibold shrink-0">♥ {{ p.likeCount }}</span>
            <button
              v-if="canDelete(p.author)"
              type="button"
              class="text-xs text-ink-muted hover:text-cheek shrink-0"
              @click="removePost(p.id)"
            >
              🗑
            </button>
          </li>
        </ol>

        <!-- 리스트형(기본) -->
        <div v-else class="rounded-2xl bg-surface border border-border overflow-hidden">
          <ul class="divide-y divide-border">
            <li v-for="p in displayPosts" :key="p.id" class="relative group hover:bg-elevated transition-colors">
              <button
                v-if="canDelete(p.author)"
                type="button"
                class="absolute right-2 top-1/2 -translate-y-1/2 z-10 rounded-md bg-paper/90 px-2 py-1 text-xs font-semibold text-cheek border border-cheek/40 opacity-0 group-hover:opacity-100 transition-all"
                @click.prevent.stop="removePost(p.id)"
              >
                🗑 삭제
              </button>
              <RouterLink
                :to="{ name: 'board-post-detail', params: { slug, id: p.id } }"
                class="flex items-center gap-3 px-5 py-3 text-sm"
              >
                <span class="text-ink truncate flex-1">{{ p.title }}</span>
                <span v-if="p.commentCount > 0" class="text-[11px] text-corn shrink-0">[{{ p.commentCount }}]</span>
                <span class="text-xs text-ink-muted shrink-0">{{ p.author }}</span>
                <span class="text-xs text-ink-muted shrink-0 w-16 text-right">{{ relativeTime(p.createdAt) }}</span>
                <span class="text-[11px] text-cheek shrink-0 w-12 text-right">♥ {{ p.likeCount }}</span>
              </RouterLink>
            </li>
          </ul>
        </div>
      </template>
    </template>
  </div>
</template>
