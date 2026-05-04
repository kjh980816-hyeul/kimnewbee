<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createOfflineReview } from '@/api/offline';
import { isHttpStatus } from '@/api/error';
import { useImageUpload } from '@/composables/useImageUpload';

const router = useRouter();

const title = ref('');
const location = ref('');
const meetupDate = ref('');
const imageUrl = ref('');
const content = ref('');
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

async function onSubmit(): Promise<void> {
  if (
    !title.value.trim() ||
    !location.value.trim() ||
    !meetupDate.value.trim() ||
    !imageUrl.value.trim() ||
    !content.value.trim()
  ) {
    error.value = '모든 필드를 입력해주세요';
    return;
  }
  if (!isImageUrlValid.value) {
    error.value = '이미지 URL이 유효하지 않아요';
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    const review = await createOfflineReview({
      title: title.value,
      location: location.value,
      meetupDate: meetupDate.value,
      imageUrl: imageUrl.value,
      content: content.value,
    });
    await router.push({ name: 'offline-detail', params: { id: review.id } });
  } catch (e) {
    if (isHttpStatus(e, 403)) {
      error.value = '옥수수 등급 이상만 작성할 수 있어요';
    } else {
      error.value = e instanceof Error ? e.message : '후기 작성에 실패했어요';
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

    <h1 class="text-2xl font-bold text-pepper mb-2">오프 후기 쓰기</h1>
    <p class="mb-6 text-sm text-ink-muted">🌽 옥수수 등급 이상만 작성 가능</p>

    <form class="space-y-4 max-w-2xl" @submit.prevent="onSubmit">
      <div>
        <label class="block text-sm text-ink-muted mb-1" for="off-title">제목</label>
        <input
          id="off-title"
          v-model="title"
          type="text"
          maxlength="120"
          placeholder="4월 서울 정모 후기"
          class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm text-ink-muted mb-1" for="off-location">장소</label>
          <input
            id="off-location"
            v-model="location"
            type="text"
            maxlength="60"
            placeholder="서울 홍대"
            class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink placeholder:text-ink-muted focus:outline-none focus:border-pepper"
          />
        </div>
        <div>
          <label class="block text-sm text-ink-muted mb-1" for="off-date">정모 일자</label>
          <input
            id="off-date"
            v-model="meetupDate"
            type="date"
            class="w-full rounded-md bg-surface border border-border px-3 py-2 text-ink focus:outline-none focus:border-pepper"
          />
        </div>
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="off-image">대표 이미지</label>
        <div class="flex gap-2 items-center">
          <input
            id="off-image"
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
        <div v-if="isImageUrlValid" class="mt-2 aspect-video w-64 overflow-hidden rounded-md bg-elevated">
          <img :src="imageUrl" alt="미리보기" class="w-full h-full object-cover" />
        </div>
      </div>

      <div>
        <label class="block text-sm text-ink-muted mb-1" for="off-content">후기 내용</label>
        <textarea
          id="off-content"
          v-model="content"
          rows="10"
          placeholder="모임 분위기, 인상 깊었던 순간 등"
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
