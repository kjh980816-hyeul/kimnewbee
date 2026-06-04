<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { createBoardPost } from '@/api/boardPost';
import { uploadImage } from '@/api/upload';
import { useBoards } from '@/composables/useBoards';
import { errorMessage, isHttpStatus } from '@/api/error';

const route = useRoute();
const router = useRouter();
const slug = computed(() => String(route.params['slug']));

const { boards } = useBoards();
const board = computed(() => boards.value.find((b) => b.slug === slug.value) ?? null);
// 갤러리/영상형 게시판은 미디어 URL이 핵심이라 입력칸을 강조.
const mediaPrimary = computed(() => board.value?.layout === 'gallery' || board.value?.layout === 'video');

const title = ref('');
const content = ref('');
const mediaUrl = ref('');
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
    mediaUrl.value = uploaded.url;
  } catch (e) {
    if (isHttpStatus(e, 401)) imageError.value = '로그인이 필요해요';
    else if (isHttpStatus(e, 400)) imageError.value = '이미지 형식/크기를 확인해주세요 (10MB, png/jpg/webp/gif)';
    else imageError.value = errorMessage(e, '업로드에 실패했어요');
  } finally {
    imageUploading.value = false;
    input.value = '';
  }
}

async function onSubmit(): Promise<void> {
  if (!title.value.trim() || !content.value.trim()) {
    error.value = '제목과 내용을 모두 입력해주세요';
    return;
  }
  submitting.value = true;
  error.value = null;
  try {
    const post = await createBoardPost(slug.value, {
      title: title.value,
      content: content.value,
      mediaUrl: mediaUrl.value.trim() || undefined,
    });
    await router.push({ name: 'board-post-detail', params: { slug: slug.value, id: post.id } });
  } catch (e) {
    error.value = errorMessage(e, '게시글 작성에 실패했어요');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="min-h-screen bg-paper text-ink p-8">
    <button type="button" class="mb-4 text-sm text-ink-muted hover:text-ink" @click="router.back()">
      ← 취소
    </button>

    <h1 class="text-2xl font-bold text-ink mb-6" style="font-family: var(--font-serif)">{{ board?.name ?? '게시판' }} 글쓰기</h1>

    <form class="glass card-pad space-y-4 max-w-3xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="bp-title">제목</label>
        <input
          id="bp-title"
          v-model="title"
          type="text"
          maxlength="200"
          placeholder="제목을 입력하세요"
          class="field-input"
        />
      </div>

      <div>
        <div class="flex items-center justify-between mb-1">
          <label class="block text-sm text-ink-muted" for="bp-media">
            {{ mediaPrimary ? '이미지 / 영상 URL' : '이미지 / 영상 URL (선택)' }}
          </label>
          <label
            class="cursor-pointer text-xs rounded-md border border-border px-2.5 py-1 text-ink-muted hover:text-pepper hover:border-pepper"
            :class="{ 'opacity-50 pointer-events-none': imageUploading }"
          >
            {{ imageUploading ? '업로드 중...' : '📎 이미지 업로드' }}
            <input type="file" accept="image/*" class="hidden" @change="onImagePick" />
          </label>
        </div>
        <input
          id="bp-media"
          v-model="mediaUrl"
          type="text"
          placeholder="이미지를 업로드하거나 영상 링크를 붙여넣으세요"
          class="field-input"
        />
        <p v-if="imageError" class="mt-1 text-xs text-cheek">{{ imageError }}</p>
        <img
          v-if="mediaUrl"
          :src="mediaUrl"
          alt="미리보기"
          class="mt-2 max-h-48 rounded-md border border-border object-contain"
          @error="() => {}"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="bp-content">내용</label>
        <textarea
          id="bp-content"
          v-model="content"
          rows="12"
          placeholder="내용을 입력하세요"
          class="field-input resize-y text-sm"
        />
      </div>

      <p v-if="error" class="text-cheek text-sm">{{ error }}</p>

      <div class="flex gap-2">
        <button
          type="submit"
          :disabled="submitting"
          class="btn-primary disabled:opacity-50"
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
