<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { createFreePost } from '@/api/free';

const router = useRouter();

const title = ref('');
const content = ref('');
const submitting = ref(false);
const error = ref<string | null>(null);

async function onSubmit(): Promise<void> {
  if (!title.value.trim() || !content.value.trim()) {
    error.value = '제목과 내용을 모두 입력해주세요';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    const post = await createFreePost({
      title: title.value,
      content: content.value,
    });
    await router.push({ name: 'free-detail', params: { id: post.id } });
  } catch (e) {
    error.value = e instanceof Error ? e.message : '게시글 작성에 실패했어요';
  } finally {
    submitting.value = false;
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
      ← 취소
    </button>

    <h1 class="text-2xl font-bold text-pepper mb-6">자유게시판 글쓰기</h1>

    <form class="space-y-4" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="post-title">제목</label>
        <input
          id="post-title"
          v-model="title"
          type="text"
          maxlength="120"
          placeholder="제목을 입력하세요"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="post-content">내용</label>
        <textarea
          id="post-content"
          v-model="content"
          rows="12"
          placeholder="내용을 입력하세요"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper resize-y"
        />
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>

      <div class="flex gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="rounded-md bg-pepper px-4 py-2 text-sm font-medium text-paper hover:bg-pepper-deep disabled:opacity-50"
        >
          {{ submitting ? '등록 중...' : '등록하기' }}
        </button>
        <button
          type="button"
          class="rounded-md border border-border px-4 py-2 text-sm text-ink-muted hover:text-ink"
          @click="router.back()"
        >
          취소
        </button>
      </div>
    </form>
  </main>
</template>
