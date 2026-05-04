<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchFanart, updateFanart } from '@/api/fanart';
import { isHttpStatus } from '@/api/error';
import { useImageUpload } from '@/composables/useImageUpload';

const route = useRoute();
const router = useRouter();

const postId = computed(() => Number(route.params['id']));

const title = ref('');
const imageUrl = ref('');
const content = ref('');
const loading = ref(true);
const { uploading, uploadError, pickAndUpload } = useImageUpload(imageUrl);
const submitting = ref(false);
const error = ref<string | null>(null);

const isImageUrlValid = computed(() => {
  if (!imageUrl.value) return false;
  try {
    const u = new URL(imageUrl.value);
    return u.protocol === 'https:' || u.protocol === 'http:';
  } catch {
    return false;
  }
});

onMounted(async () => {
  if (Number.isNaN(postId.value)) {
    error.value = '잘못된 게시글 번호예요';
    loading.value = false;
    return;
  }
  try {
    const fanart = await fetchFanart(postId.value);
    title.value = fanart.title;
    imageUrl.value = fanart.imageUrl;
    content.value = fanart.content;
  } catch (e) {
    error.value = e instanceof Error ? e.message : '팬아트를 불러올 수 없어요';
  } finally {
    loading.value = false;
  }
});

async function onSubmit(): Promise<void> {
  if (!title.value.trim() || !imageUrl.value.trim()) {
    error.value = '제목과 이미지 URL은 필수예요';
    return;
  }
  if (!isImageUrlValid.value) {
    error.value = '이미지 URL이 유효하지 않아요 (http(s)://)';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    const updated = await updateFanart(postId.value, {
      title: title.value,
      imageUrl: imageUrl.value,
      content: content.value,
    });
    await router.push({ name: 'fanart-detail', params: { id: updated.id } });
  } catch (e) {
    if (isHttpStatus(e, 403)) {
      error.value = '본인 글만 수정할 수 있어요';
    } else if (isHttpStatus(e, 401)) {
      error.value = '로그인이 필요해요';
    } else {
      error.value = e instanceof Error ? e.message : '수정에 실패했어요';
    }
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

    <h1 class="text-2xl font-bold text-pepper mb-6">팬아트 수정</h1>

    <p v-if="loading" class="text-ink-muted">불러오는 중...</p>

    <form v-else class="space-y-4" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="fa-title">제목</label>
        <input
          id="fa-title"
          v-model="title"
          type="text"
          maxlength="120"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="fa-image">이미지</label>
        <div class="flex gap-2 items-center">
          <input
            id="fa-image"
            v-model="imageUrl"
            type="url"
            placeholder="https://... 또는 우측 업로드"
            class="flex-1 rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
          />
          <label
            class="cursor-pointer rounded-md border border-border px-3 py-2 text-sm text-ink-muted hover:text-pepper hover:border-pepper"
            :class="{ 'opacity-50 pointer-events-none': uploading }"
          >
            {{ uploading ? '업로드 중...' : '파일 선택' }}
            <input type="file" accept="image/*" class="hidden" @change="pickAndUpload" />
          </label>
        </div>
        <p v-if="uploadError" class="mt-1 text-xs text-cheek">{{ uploadError }}</p>
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="fa-content">설명</label>
        <textarea
          id="fa-content"
          v-model="content"
          rows="6"
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
          {{ submitting ? '저장 중...' : '저장하기' }}
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
