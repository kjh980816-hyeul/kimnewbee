<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { createFreePost } from '@/api/free';
import { uploadImage } from '@/api/upload';
import { isHttpStatus } from '@/api/error';

const router = useRouter();

const title = ref('');
const content = ref('');
const contentTextarea = ref<HTMLTextAreaElement | null>(null);
const submitting = ref(false);
const error = ref<string | null>(null);
const imageUploading = ref(false);
const imageError = ref<string | null>(null);

async function onImagePick(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  imageUploading.value = true;
  imageError.value = null;
  try {
    const uploaded = await uploadImage(file);
    insertImageMarkdown(uploaded.url);
  } catch (e) {
    if (isHttpStatus(e, 401)) {
      imageError.value = '로그인이 필요해요';
    } else if (isHttpStatus(e, 400)) {
      imageError.value = '이미지 형식/크기를 확인해주세요 (10MB, png/jpg/webp/gif)';
    } else {
      imageError.value = e instanceof Error ? e.message : '업로드에 실패했어요';
    }
  } finally {
    imageUploading.value = false;
    input.value = '';
  }
}

function insertImageMarkdown(url: string): void {
  const snippet = `\n![](${url})\n`;
  const el = contentTextarea.value;
  if (!el) {
    content.value = (content.value ?? '') + snippet;
    return;
  }
  const start = el.selectionStart ?? content.value.length;
  const end = el.selectionEnd ?? start;
  content.value = content.value.substring(0, start) + snippet + content.value.substring(end);
  requestAnimationFrame(() => {
    el.focus();
    const caret = start + snippet.length;
    el.setSelectionRange(caret, caret);
  });
}

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
        <div class="flex items-center justify-between mb-1">
          <label class="block text-sm text-ink-muted" for="post-content">내용</label>
          <label
            class="cursor-pointer text-xs rounded-md border border-border px-2.5 py-1 text-ink-muted hover:text-pepper hover:border-pepper"
            :class="{ 'opacity-50 pointer-events-none': imageUploading }"
          >
            {{ imageUploading ? '업로드 중...' : '📎 이미지 추가' }}
            <input type="file" accept="image/*" class="hidden" @change="onImagePick" />
          </label>
        </div>
        <textarea
          id="post-content"
          ref="contentTextarea"
          v-model="content"
          rows="14"
          placeholder="내용을 입력하세요. 이미지는 우측 상단 버튼으로 추가하면 본문에 자동으로 삽입돼요."
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper resize-y font-mono text-sm"
        />
        <p v-if="imageError" class="mt-1 text-xs text-cheek">{{ imageError }}</p>
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
