<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFanart, toggleFanartLike } from '@/api/fanart';
import { fetchComments, createComment, deleteComment } from '@/api/comment';
import type { Fanart } from '@/types/fanart';
import type { Comment } from '@/types/comment';

const route = useRoute();
const router = useRouter();

const fanart = ref<Fanart | null>(null);
const comments = ref<Comment[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const newCommentContent = ref('');

const fanartId = computed(() => Number(route.params['id']));
const sortedComments = computed(() =>
  [...comments.value].sort((a, b) => a.createdAt.localeCompare(b.createdAt)),
);

onMounted(async () => {
  if (Number.isNaN(fanartId.value)) {
    error.value = '잘못된 팬아트 번호예요';
    loading.value = false;
    return;
  }
  try {
    const [f, cs] = await Promise.all([
      fetchFanart(fanartId.value),
      fetchComments(fanartId.value),
    ]);
    fanart.value = f;
    comments.value = cs;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '팬아트를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onLike(): Promise<void> {
  if (!fanart.value) return;
  const res = await toggleFanartLike(fanart.value.id);
  fanart.value.likedByMe = res.liked;
  fanart.value.likeCount = res.likeCount;
}

async function onSubmitComment(): Promise<void> {
  if (!newCommentContent.value.trim()) return;
  const c = await createComment({
    postId: fanartId.value,
    parentId: null,
    content: newCommentContent.value,
  });
  comments.value.push(c);
  newCommentContent.value = '';
  if (fanart.value) fanart.value.commentCount += 1;
}

async function onDeleteComment(id: number): Promise<void> {
  await deleteComment(id);
  const c = comments.value.find((x) => x.id === id);
  if (c) {
    c.deleted = true;
    c.content = '삭제된 댓글이에요';
  }
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 갤러리로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="fanart">
      <article class="rounded-md bg-surface overflow-hidden">
        <img
          :src="fanart.imageUrl"
          :alt="fanart.title"
          class="w-full max-h-[80vh] object-contain bg-elevated"
        />
        <div class="p-6">
          <h1 class="text-xl font-bold text-ink">{{ fanart.title }}</h1>
          <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
            <span>{{ fanart.author }}</span>
            <span>·</span>
            <span>{{ new Date(fanart.createdAt).toLocaleDateString('ko-KR') }}</span>
            <span>·</span>
            <span>조회 {{ fanart.viewCount }}</span>
          </div>
          <p v-if="fanart.content" class="mt-4 whitespace-pre-wrap text-ink leading-relaxed">
            {{ fanart.content }}
          </p>
          <div class="mt-4 flex gap-2">
            <button
              type="button"
              class="rounded-md border border-border px-3 py-1.5 text-sm transition-colors"
              :class="fanart.likedByMe ? 'bg-cheek text-paper border-cheek' : 'text-ink-muted hover:text-cheek hover:border-cheek'"
              @click="onLike"
            >
              ♥ {{ fanart.likeCount }}
            </button>
          </div>
        </div>
      </article>

      <section class="mt-6 rounded-md bg-surface p-6">
        <h2 class="text-sm font-medium text-ink-muted mb-4">
          댓글 {{ fanart.commentCount }}
        </h2>

        <ul class="space-y-3 mb-6">
          <li
            v-for="c in sortedComments"
            :key="c.id"
            class="border border-border rounded-md p-3"
          >
            <div class="flex items-center justify-between gap-2">
              <span class="text-sm text-ink">{{ c.author }}</span>
              <span class="text-xs text-ink-muted">
                {{ new Date(c.createdAt).toLocaleDateString('ko-KR') }}
              </span>
            </div>
            <p class="mt-2 text-sm text-ink whitespace-pre-wrap">{{ c.content }}</p>
            <button
              v-if="!c.deleted"
              type="button"
              class="mt-2 text-xs text-ink-muted hover:text-cheek"
              @click="onDeleteComment(c.id)"
            >
              삭제
            </button>
          </li>
        </ul>

        <div class="flex gap-2">
          <input
            v-model="newCommentContent"
            type="text"
            placeholder="댓글을 입력하세요"
            class="flex-1 rounded-md bg-elevated border border-border px-3 py-2 text-sm text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
            @keyup.enter="onSubmitComment"
          />
          <button
            type="button"
            class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep"
            @click="onSubmitComment"
          >
            등록
          </button>
        </div>
      </section>
    </template>
  </main>
</template>
