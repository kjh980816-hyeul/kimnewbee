<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { fetchComments, createComment, deleteComment } from '@/api/comment';
import type { Comment } from '@/types/comment';

const props = defineProps<{
  postId: number;
}>();

const comments = ref<Comment[]>([]);
const newCommentContent = ref('');
const replyingTo = ref<number | null>(null);
const replyContent = ref('');
const loading = ref(true);
const error = ref<string | null>(null);

const topLevel = computed(() => comments.value.filter((c) => c.parentId === null));
function repliesOf(parentId: number): Comment[] {
  return comments.value.filter((c) => c.parentId === parentId);
}
const aliveCount = computed(() => comments.value.filter((c) => !c.deleted).length);

onMounted(async () => {
  try {
    comments.value = await fetchComments(props.postId);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '댓글을 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onSubmit(): Promise<void> {
  if (!newCommentContent.value.trim()) return;
  const c = await createComment({
    postId: props.postId,
    parentId: null,
    content: newCommentContent.value,
  });
  comments.value.push(c);
  newCommentContent.value = '';
}

async function onSubmitReply(parentId: number): Promise<void> {
  if (!replyContent.value.trim()) return;
  const c = await createComment({
    postId: props.postId,
    parentId,
    content: replyContent.value,
  });
  comments.value.push(c);
  replyContent.value = '';
  replyingTo.value = null;
}

async function onDelete(id: number): Promise<void> {
  await deleteComment(id);
  const c = comments.value.find((x) => x.id === id);
  if (c) {
    c.deleted = true;
    c.content = '삭제된 댓글이에요';
  }
}
</script>

<template>
  <section class="rounded-md bg-surface p-6">
    <h2 class="text-sm font-medium text-ink-muted mb-4">댓글 {{ aliveCount }}</h2>

    <p v-if="loading" class="text-ink-muted text-sm mb-4">댓글 불러오는 중...</p>
    <p v-else-if="error" class="text-cheek text-sm mb-4">{{ error }}</p>

    <ul class="space-y-3 mb-6">
      <li
        v-for="c in topLevel"
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
          <button type="button" class="hover:text-cheek" @click="onDelete(c.id)">삭제</button>
        </div>

        <div v-if="replyingTo === c.id" class="mt-3 flex gap-2">
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

        <ul
          v-if="repliesOf(c.id).length > 0"
          class="mt-3 ml-6 space-y-2 border-l-2 border-border pl-3"
        >
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
              @click="onDelete(r.id)"
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
        @keyup.enter="onSubmit"
      />
      <button
        type="button"
        class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep"
        @click="onSubmit"
      >
        등록
      </button>
    </div>
  </section>
</template>
