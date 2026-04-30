<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFreePost, toggleFreePostLike } from '@/api/free';
import { fetchComments, createComment, deleteComment } from '@/api/comment';
import type { FreePost } from '@/types/free';
import type { Comment } from '@/types/comment';

const route = useRoute();
const router = useRouter();

const post = ref<FreePost | null>(null);
const comments = ref<Comment[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const newCommentContent = ref('');
const replyingTo = ref<number | null>(null);
const replyContent = ref('');

const topLevelComments = computed(() =>
  comments.value.filter((c) => c.parentId === null),
);
function repliesOf(parentId: number): Comment[] {
  return comments.value.filter((c) => c.parentId === parentId);
}

const postId = computed(() => Number(route.params['id']));

async function loadAll(): Promise<void> {
  if (Number.isNaN(postId.value)) {
    error.value = '잘못된 게시글 번호예요';
    loading.value = false;
    return;
  }
  try {
    const [p, cs] = await Promise.all([
      fetchFreePost(postId.value),
      fetchComments(postId.value),
    ]);
    post.value = p;
    comments.value = cs;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시글을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
}

async function onLike(): Promise<void> {
  if (!post.value) return;
  const res = await toggleFreePostLike(post.value.id);
  post.value.likedByMe = res.liked;
  post.value.likeCount = res.likeCount;
}

async function onSubmitComment(): Promise<void> {
  if (!newCommentContent.value.trim()) return;
  const c = await createComment({
    postId: postId.value,
    parentId: null,
    content: newCommentContent.value,
  });
  comments.value.push(c);
  newCommentContent.value = '';
  if (post.value) post.value.commentCount += 1;
}

async function onSubmitReply(parentId: number): Promise<void> {
  if (!replyContent.value.trim()) return;
  const c = await createComment({
    postId: postId.value,
    parentId,
    content: replyContent.value,
  });
  comments.value.push(c);
  replyContent.value = '';
  replyingTo.value = null;
  if (post.value) post.value.commentCount += 1;
}

async function onDeleteComment(id: number): Promise<void> {
  await deleteComment(id);
  const c = comments.value.find((x) => x.id === id);
  if (c) {
    c.deleted = true;
    c.content = '삭제된 댓글이에요';
  }
}

onMounted(loadAll);
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button
      type="button"
      class="mb-4 text-sm text-ink-muted hover:text-ink"
      @click="router.back()"
    >
      ← 목록으로
    </button>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>
    <p v-else-if="error" class="text-cheek">{{ error }}</p>
    <template v-else-if="post">
      <article class="rounded-md bg-surface p-6">
        <header class="mb-4 pb-4 border-b border-border">
          <h1 class="text-xl font-bold text-ink">{{ post.title }}</h1>
          <div class="mt-2 flex items-center gap-3 text-xs text-ink-muted">
            <span>{{ post.author }}</span>
            <span>·</span>
            <span>{{ new Date(post.createdAt).toLocaleDateString('ko-KR') }}</span>
            <span>·</span>
            <span>조회 {{ post.viewCount }}</span>
          </div>
        </header>
        <div class="whitespace-pre-wrap text-ink leading-relaxed">{{ post.content }}</div>

        <div class="mt-6 flex gap-2">
          <button
            type="button"
            class="rounded-md border border-border px-3 py-1.5 text-sm transition-colors"
            :class="post.likedByMe ? 'bg-cheek text-paper border-cheek' : 'text-ink-muted hover:text-cheek hover:border-cheek'"
            @click="onLike"
          >
            ♥ {{ post.likeCount }}
          </button>
        </div>
      </article>

      <section class="mt-6 rounded-md bg-surface p-6">
        <h2 class="text-sm font-medium text-ink-muted mb-4">
          댓글 {{ post.commentCount }}
        </h2>

        <ul class="space-y-4 mb-6">
          <li
            v-for="c in topLevelComments"
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
            <div v-if="!c.deleted" class="mt-2 flex gap-3 text-xs text-ink-muted">
              <button type="button" class="hover:text-pepper" @click="replyingTo = c.id">
                답글
              </button>
              <button type="button" class="hover:text-cheek" @click="onDeleteComment(c.id)">
                삭제
              </button>
            </div>

            <div
              v-if="replyingTo === c.id"
              class="mt-3 flex gap-2"
            >
              <input
                v-model="replyContent"
                type="text"
                placeholder="답글을 입력하세요"
                class="flex-1 rounded-md bg-elevated border border-border px-3 py-2 text-sm text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
                @keyup.enter="onSubmitReply(c.id)"
              />
              <button
                type="button"
                class="rounded-md bg-pepper px-3 py-2 text-sm text-paper hover:bg-pepper-deep"
                @click="onSubmitReply(c.id)"
              >
                등록
              </button>
            </div>

            <ul v-if="repliesOf(c.id).length > 0" class="mt-3 ml-6 space-y-2 border-l-2 border-border pl-3">
              <li v-for="r in repliesOf(c.id)" :key="r.id">
                <div class="flex items-center justify-between gap-2">
                  <span class="text-sm text-ink-muted">↳ {{ r.author }}</span>
                  <span class="text-xs text-ink-muted">
                    {{ new Date(r.createdAt).toLocaleDateString('ko-KR') }}
                  </span>
                </div>
                <p class="mt-1 text-sm text-ink whitespace-pre-wrap">{{ r.content }}</p>
                <button
                  v-if="!r.deleted"
                  type="button"
                  class="mt-1 text-xs text-ink-muted hover:text-cheek"
                  @click="onDeleteComment(r.id)"
                >
                  삭제
                </button>
              </li>
            </ul>
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
